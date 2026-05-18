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
import com.ddlmouse.app.domain.TaskModule
import com.ddlmouse.app.domain.TaskOccurrence
import com.ddlmouse.app.domain.TaskStatus
import com.ddlmouse.app.data.DailySummaryRepository
import com.ddlmouse.app.data.PetRepository
import com.ddlmouse.app.data.TaskRepository
import java.time.LocalDate
import java.time.LocalDateTime
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

    val urgentOccurrences: List<TaskOccurrence>
        get() = occurrences
            .filter { it.status == TaskStatus.PENDING && it.deadline != null }
            .sortedBy { it.deadline }
            .take(3)
}

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

    fun createTask(title: String, module: TaskModule, deadlineText: String, difficulty: Difficulty?) {
        viewModelScope.launch {
            val now = LocalDateTime.now()
            val deadline = parseDeadline(deadlineText)
            taskRepository.createTask(
                title = title,
                module = module,
                deadline = deadline,
                difficulty = difficulty ?: DifficultyPolicy.recommend(module, deadline, now),
                reminderOverride = null
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
            if ('T' in clean) {
                LocalDateTime.parse(clean)
            } else {
                LocalDate.parse(clean).atTime(23, 0)
            }
        }.getOrNull()
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

