package com.ddlmouse.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ddlmouse.app.data.settings.SettingsStore
import com.ddlmouse.app.domain.DailySummary
import com.ddlmouse.app.domain.Difficulty
import com.ddlmouse.app.domain.DifficultyPolicy
import com.ddlmouse.app.domain.PetLineScene
import com.ddlmouse.app.domain.PetState
import com.ddlmouse.app.domain.StoreItem
import com.ddlmouse.app.domain.TaskFormPolicy
import com.ddlmouse.app.domain.TaskModule
import com.ddlmouse.app.domain.TaskOccurrence
import com.ddlmouse.app.domain.TaskSectionPolicy
import com.ddlmouse.app.domain.TaskSectionSummary
import com.ddlmouse.app.domain.TaskStatus
import com.ddlmouse.app.data.DailySummaryRepository
import com.ddlmouse.app.data.PetRepository
import com.ddlmouse.app.data.TaskRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MainTab(val label: String) {
    TASKS("任务"),
    PET("宠物"),
    MINE("我的")
}

data class DdlMouseUiState(
    val selectedTab: MainTab = MainTab.TASKS,
    val selectedModule: TaskModule = TaskModule.DAILY,
    val occurrences: List<TaskOccurrence> = emptyList(),
    val petState: PetState = PetState(),
    val storeItems: List<StoreItem> = emptyList(),
    val unshownSummary: DailySummary? = null,
    val petLine: String = "鼠鼠在线，DDL 也在线。",
    val showAddTaskDialog: Boolean = false,
    val notificationsEnabled: Boolean = true
) {
    val filteredOccurrences: List<TaskOccurrence>
        get() = occurrences.filter { it.module == selectedModule }

    val sectionSummaries: List<TaskSectionSummary>
        get() = TaskSectionPolicy.summaries(occurrences)

    val todayTotal: Int
        get() = occurrences.count { it.module == TaskModule.DAILY }

    val todayCompleted: Int
        get() = occurrences.count { it.module == TaskModule.DAILY && it.status == TaskStatus.COMPLETED }

    val pendingPoints: Int
        get() = occurrences
            .filter { it.status == TaskStatus.PENDING }
            .sumOf { it.difficulty.points }

    val urgentOccurrences: List<TaskOccurrence>
        get() = occurrences
            .filter { it.status == TaskStatus.PENDING && it.deadline != null }
            .sortedBy { it.deadline }
            .take(3)
}

data class TaskFormInput(
    val title: String,
    val module: TaskModule,
    val deadlineText: String,
    val difficulty: Difficulty?,
    val note: String,
    val reminderEnabled: Boolean,
    val reminderTimeText: String,
    val timeBucket: String,
    val weeklyDaysText: String,
    val monthlyDayText: String,
    val projectStage: String
)

class DdlMouseViewModel(
    private val taskRepository: TaskRepository,
    private val petRepository: PetRepository,
    private val dailySummaryRepository: DailySummaryRepository,
    private val settingsStore: SettingsStore
) : ViewModel() {
    private val _state = MutableStateFlow(DdlMouseUiState())
    val state: StateFlow<DdlMouseUiState> = _state

    init {
        viewModelScope.launch {
            taskRepository.initializeForOpen(LocalDateTime.now())
        }
        viewModelScope.launch {
            taskRepository.observeOccurrences().collect { occurrences ->
                _state.update { it.copy(occurrences = occurrences) }
            }
        }
        viewModelScope.launch {
            petRepository.observePetState().collect { petState ->
                _state.update { it.copy(petState = petState) }
            }
        }
        viewModelScope.launch {
            petRepository.observeStoreItems().collect { items ->
                _state.update { it.copy(storeItems = items) }
            }
        }
        viewModelScope.launch {
            dailySummaryRepository.observeUnshownSummary().collect { summary ->
                _state.update { it.copy(unshownSummary = summary) }
            }
        }
        viewModelScope.launch {
            settingsStore.notificationsEnabled.collect { enabled ->
                _state.update { it.copy(notificationsEnabled = enabled) }
            }
        }
    }

    fun selectTab(tab: MainTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    fun selectModule(module: TaskModule) {
        _state.update { it.copy(selectedModule = module) }
    }

    fun showAddTaskDialog() {
        _state.update { it.copy(showAddTaskDialog = true) }
    }

    fun hideAddTaskDialog() {
        _state.update { it.copy(showAddTaskDialog = false) }
    }

    fun createTask(input: TaskFormInput) {
        viewModelScope.launch {
            val now = LocalDateTime.now()
            val deadline = parseDeadline(input.deadlineText)
            val reminderMinute = parseMinuteOfDay(input.reminderTimeText)
            val reminderOverride = deadline?.let { reminderOverrideFor(it, reminderMinute) }
            taskRepository.createTask(
                title = input.title,
                module = input.module,
                deadline = deadline,
                difficulty = input.difficulty ?: DifficultyPolicy.recommend(input.module, deadline, now),
                reminderOverride = reminderOverride,
                note = input.note,
                repeatMode = TaskFormPolicy.repeatModeFor(input.module),
                reminderEnabled = input.reminderEnabled,
                preferredReminderMinuteOfDay = reminderMinute,
                timeBucket = input.timeBucket,
                weeklyDays = parseWeeklyDays(input.weeklyDaysText),
                monthlyDay = parseMonthlyDay(input.monthlyDayText),
                projectStage = input.projectStage
            )
            _state.update {
                it.copy(
                    showAddTaskDialog = false,
                    petLine = petRepository.randomLine(PetLineScene.REMINDER)
                )
            }
        }
    }

    fun completeOccurrence(occurrenceId: Long) {
        viewModelScope.launch {
            taskRepository.completeOccurrence(occurrenceId, LocalDateTime.now())
            _state.update { it.copy(petLine = petRepository.randomLine(PetLineScene.COMPLETE)) }
        }
    }

    fun feedPet() {
        viewModelScope.launch {
            petRepository.feed()
            _state.update { it.copy(petLine = petRepository.randomLine(PetLineScene.FEED)) }
        }
    }

    fun interactWithPet() {
        _state.update { it.copy(petLine = petRepository.randomLine(PetLineScene.INTERACT)) }
    }

    fun buyItem(itemId: String) {
        viewModelScope.launch {
            petRepository.buy(itemId)
            _state.update { it.copy(petLine = petRepository.randomLine(PetLineScene.PURCHASE)) }
        }
    }

    fun dismissSummary() {
        val summary = state.value.unshownSummary ?: return
        viewModelScope.launch {
            dailySummaryRepository.markShown(summary, LocalDateTime.now())
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsStore.setNotificationsEnabled(enabled)
        }
    }

    private fun parseDeadline(text: String): LocalDateTime? {
        val clean = text.trim()
        if (clean.isEmpty()) return null
        return runCatching {
            when {
                'T' in clean -> LocalDateTime.parse(clean)
                ' ' in clean -> LocalDateTime.parse(clean.replace(' ', 'T'))
                else -> LocalDate.parse(clean).atTime(23, 0)
            }
        }.getOrNull()
    }

    private fun parseMinuteOfDay(text: String): Int? {
        val clean = text.trim()
        if (clean.isEmpty()) return null
        return runCatching {
            val time = LocalTime.parse(clean, DateTimeFormatter.ofPattern("H:mm"))
            time.hour * 60 + time.minute
        }.getOrNull()
    }

    private fun reminderOverrideFor(deadline: LocalDateTime, minuteOfDay: Int?): LocalDateTime? {
        if (minuteOfDay == null) return null
        val candidate = deadline.toLocalDate().atTime(minuteOfDay / 60, minuteOfDay % 60)
        return if (candidate.isBefore(deadline)) candidate else deadline.minusHours(1)
    }

    private fun parseWeeklyDays(text: String): Set<Int> {
        if (text.isBlank()) return emptySet()
        val normalized = text
            .replace("周", "")
            .replace("星期", "")
            .replace("礼拜", "")
            .replace("日", "7")
            .replace("天", "7")
        val chineseDayMap = mapOf(
            '一' to 1,
            '二' to 2,
            '三' to 3,
            '四' to 4,
            '五' to 5,
            '六' to 6,
            '七' to 7
        )
        val directDays = normalized.mapNotNull { chineseDayMap[it] }
        val tokenDays = normalized
            .split(',', '，', '、', ' ')
            .mapNotNull { it.trim().toIntOrNull() }
        return (directDays + tokenDays).filter { it in 1..7 }.toSet()
    }

    private fun parseMonthlyDay(text: String): Int? {
        return text.trim().toIntOrNull()?.takeIf { it in 1..31 }
    }

    class Factory(
        private val taskRepository: TaskRepository,
        private val petRepository: PetRepository,
        private val dailySummaryRepository: DailySummaryRepository,
        private val settingsStore: SettingsStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DdlMouseViewModel(
                taskRepository,
                petRepository,
                dailySummaryRepository,
                settingsStore
            ) as T
        }
    }
}
