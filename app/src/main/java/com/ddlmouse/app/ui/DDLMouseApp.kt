package com.ddlmouse.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ddlmouse.app.domain.DailySummary
import com.ddlmouse.app.domain.Difficulty
import com.ddlmouse.app.domain.DifficultyPolicy
import com.ddlmouse.app.domain.PetState
import com.ddlmouse.app.domain.ReminderPolicy
import com.ddlmouse.app.domain.ReminderPlan
import com.ddlmouse.app.domain.StoreCategory
import com.ddlmouse.app.domain.StoreItem
import com.ddlmouse.app.domain.TaskFormField
import com.ddlmouse.app.domain.TaskFormPolicy
import com.ddlmouse.app.domain.TaskHistoryGroup
import com.ddlmouse.app.domain.TaskModule
import com.ddlmouse.app.domain.TaskOccurrence
import com.ddlmouse.app.domain.TaskSectionSummary
import com.ddlmouse.app.domain.TaskStatus
import com.ddlmouse.app.domain.TaskTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun DDLMouseApp(viewModel: DdlMouseViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val summary = state.unshownSummary
    if (summary != null) {
        DailySummaryDialog(
            summary = summary,
            onDismiss = viewModel::dismissSummary
        )
    }

    Scaffold(
        floatingActionButton = {
            if (state.selectedTab == MainTab.TASKS) {
                FloatingActionButton(onClick = viewModel::showAddTaskDialog) {
                    Icon(Icons.Default.Add, contentDescription = "新增任务")
                }
            }
        },
        bottomBar = {
            BottomNavigation(
                selectedTab = state.selectedTab,
                onTabSelected = viewModel::selectTab
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (state.selectedTab) {
                MainTab.TASKS -> TaskScreen(
                    state = state,
                    onToggleSection = viewModel::toggleTaskSection,
                    onEdit = viewModel::showEditTaskDialog,
                    onComplete = viewModel::completeOccurrence
                )
                MainTab.PET -> PetScreen(
                    state = state,
                    onFeed = viewModel::feedPet,
                    onInteract = viewModel::interactWithPet,
                    onBuy = viewModel::buyItem
                )
                MainTab.HISTORY -> HistoryScreen(
                    state = state,
                    onArchive = viewModel::archiveOccurrence
                )
                MainTab.MINE -> MineScreen(
                    state = state,
                    onNotificationsChanged = viewModel::setNotificationsEnabled
                )
            }
        }
    }

    if (state.showAddTaskDialog) {
        AddTaskDialog(
            initialModule = state.selectedModule,
            onDismiss = viewModel::hideAddTaskDialog,
            onCreate = viewModel::createTask
        )
    }
    state.editingTemplate?.let { template ->
        EditTaskDialog(
            template = template,
            reminders = state.reminders.filter { it.templateId == template.id },
            onDismiss = viewModel::hideEditTaskDialog,
            onSave = viewModel::updateTask,
            onAddReminder = viewModel::addReminderToEditingTask,
            onDeleteReminder = viewModel::deleteReminder
        )
    }
}

@Composable
private fun BottomNavigation(selectedTab: MainTab, onTabSelected: (MainTab) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        MainTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = { Icon(tab.icon(), contentDescription = tab.label) },
                label = { Text(tab.label) }
            )
        }
    }
}

private fun MainTab.icon(): ImageVector = when (this) {
    MainTab.TASKS -> Icons.AutoMirrored.Filled.Assignment
    MainTab.PET -> Icons.Default.Pets
    MainTab.HISTORY -> Icons.Default.History
    MainTab.MINE -> Icons.Default.Person
}

@Composable
private fun TaskScreen(
    state: DdlMouseUiState,
    onToggleSection: (TaskModule) -> Unit,
    onEdit: (Long) -> Unit,
    onComplete: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Header("任务", "今天先拆最小的一步，积分会喂饱鼠鼠。")
        }
        item {
            TaskOverviewPanel(state)
        }
        item {
            UrgentPanel(state.urgentOccurrences)
        }
        items(state.sectionSummaries, key = { it.module.name }) { summary ->
            TaskModuleSection(
                summary = summary,
                occurrences = state.activeOccurrences.filter { it.module == summary.module },
                templates = state.templates.associateBy { it.id },
                collapsed = summary.module in state.collapsedModules,
                onToggle = { onToggleSection(summary.module) },
                onEdit = onEdit,
                onComplete = onComplete
            )
        }
        item {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}

@Composable
private fun TaskOverviewPanel(state: DdlMouseUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OverviewMetric(label = "今日", value = "${state.todayCompleted}/${state.todayTotal}")
            OverviewMetric(label = "待完成", value = "${state.activeOccurrences.count { it.status == TaskStatus.PENDING }}")
            OverviewMetric(label = "可得积分", value = "+${state.pendingPoints}")
        }
    }
}

@Composable
private fun OverviewMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Header(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UrgentPanel(urgent: List<TaskOccurrence>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("最近 DDL", fontWeight = FontWeight.SemiBold)
            }
            if (urgent.isEmpty()) {
                Text("暂时没有临近截止任务。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                urgent.forEach {
                    Text(
                        "• ${it.title}  ${it.deadline?.format(deadlineFormatter).orEmpty()}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ModuleChips(selected: TaskModule, onSelected: (TaskModule) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TaskModule.entries.forEach { module ->
            FilterChip(
                selected = selected == module,
                onClick = { onSelected(module) },
                label = { Text(module.label) }
            )
        }
    }
}

@Composable
private fun TaskModuleSection(
    summary: TaskSectionSummary,
    occurrences: List<TaskOccurrence>,
    templates: Map<Long, TaskTemplate>,
    collapsed: Boolean,
    onToggle: () -> Unit,
    onEdit: (Long) -> Unit,
    onComplete: (Long) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(summary.module.label, fontWeight = FontWeight.Bold)
                    Text(
                        "完成 ${summary.completedCount}/${summary.totalCount} · 待完成 ${summary.pendingCount} · 可得 +${summary.pendingPoints}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (summary.missedCount > 0) {
                    Text(
                        "逾期 ${summary.missedCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                IconButton(onClick = onToggle) {
                    Icon(
                        imageVector = if (collapsed) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                        contentDescription = if (collapsed) "展开" else "折叠"
                    )
                }
            }
            if (collapsed) {
                Text(
                    "已折叠，${summary.pendingCount} 个待完成",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (occurrences.isEmpty()) {
                Text(
                    "这一栏还没有任务",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                occurrences.forEachIndexed { index, occurrence ->
                    if (index > 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                    TaskListRow(
                        occurrence = occurrence,
                        template = templates[occurrence.templateId],
                        onEdit = { onEdit(occurrence.templateId) },
                        onComplete = { onComplete(occurrence.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskListRow(
    occurrence: TaskOccurrence,
    template: TaskTemplate?,
    onEdit: () -> Unit,
    onComplete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(statusColor(occurrence.status))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                occurrence.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = listOfNotNull(
                    occurrence.deadline?.let { "DDL ${it.format(deadlineFormatter)}" },
                    "${occurrence.difficulty.label} +${occurrence.difficulty.points}",
                    template?.projectStage?.let { "阶段 $it" },
                    template?.note?.takeIf { it.isNotBlank() },
                    template?.timeBucket?.let { "时段 $it" },
                    statusLabel(occurrence.status)
                ).joinToString("  ·  "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "编辑")
            }
            if (occurrence.status == TaskStatus.PENDING) {
                IconButton(onClick = onComplete) {
                    Icon(Icons.Default.Check, contentDescription = "完成")
                }
            }
        }
    }
}

private fun statusLabel(status: TaskStatus): String = when (status) {
    TaskStatus.PENDING -> "进行中"
    TaskStatus.COMPLETED -> "已完成"
    TaskStatus.MISSED -> "未完成"
    TaskStatus.ARCHIVED -> "已归档"
}

@Composable
private fun statusColor(status: TaskStatus): Color = when (status) {
    TaskStatus.PENDING -> MaterialTheme.colorScheme.primary
    TaskStatus.COMPLETED -> Color(0xFF2E7D32)
    TaskStatus.MISSED -> MaterialTheme.colorScheme.error
    TaskStatus.ARCHIVED -> MaterialTheme.colorScheme.outline
}

@Composable
private fun PetScreen(
    state: DdlMouseUiState,
    onFeed: () -> Unit,
    onInteract: () -> Unit,
    onBuy: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Header("宠物", "积分变成看得见的小奖励。")
        }
        item {
            PetHero(petState = state.petState, line = state.petLine, onInteract = onInteract)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onFeed, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Restaurant, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("喂食 -8")
                }
                Button(onClick = onInteract, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Pets, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("互动")
                }
            }
        }
        item {
            Text("商店", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        items(state.storeItems, key = { it.id }) { item ->
            StoreItemRow(item = item, petPoints = state.petState.points, onBuy = { onBuy(item.id) })
        }
        item { Spacer(modifier = Modifier.height(88.dp)) }
    }
}

@Composable
private fun PetHero(petState: PetState, line: String, onInteract: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MouseAvatar(petState)
            Text(
                text = "“$line”",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("积分 ${petState.points}", fontWeight = FontWeight.SemiBold)
                Text("Lv.${petState.level}", fontWeight = FontWeight.SemiBold)
                Text(petState.equippedDress, fontWeight = FontWeight.SemiBold)
            }
            StatBar(label = "心情", value = petState.mood / 100f)
            StatBar(label = "饱食", value = petState.fullness / 100f)
            TextButton(onClick = onInteract) {
                Text("戳一下鼠鼠")
            }
        }
    }
}

@Composable
private fun MouseAvatar(petState: PetState) {
    Box(
        modifier = Modifier
            .size(176.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(118.dp)
                .clip(CircleShape)
                .background(Color(0xFFF3D8B8))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 36.dp, top = 28.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFE9B98F))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 36.dp, top = 28.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFE9B98F))
        )
        Text(
            text = if (petState.mood >= 60) "^ ᴗ ^" else "•︵•",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatBar(label: String, value: Float) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        LinearProgressIndicator(
            progress = { value.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StoreItemRow(item: StoreItem, petPoints: Int, onBuy: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(item.title, fontWeight = FontWeight.SemiBold)
                Text(
                    "${item.category.label} · ${item.price} 分",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onBuy,
                enabled = !item.purchased && petPoints >= item.price
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (item.purchased) "已解锁" else "兑换")
            }
        }
    }
}

@Composable
private fun HistoryScreen(state: DdlMouseUiState, onArchive: (Long) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Header("历史", "完成、逾期和归档记录集中放在这里。")
        }
        items(state.historyGroups, key = { it.status.name }) { group ->
            HistoryGroupSection(group = group, onArchive = onArchive)
        }
        item { Spacer(modifier = Modifier.height(88.dp)) }
    }
}

@Composable
private fun HistoryGroupSection(group: TaskHistoryGroup, onArchive: (Long) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(statusLabel(group.status), fontWeight = FontWeight.Bold)
                Text(
                    "${group.occurrences.size} 条",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (group.occurrences.isEmpty()) {
                Text(
                    "暂无记录",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                group.occurrences.forEachIndexed { index, occurrence ->
                    if (index > 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                    HistoryRow(occurrence = occurrence, onArchive = onArchive)
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(occurrence: TaskOccurrence, onArchive: (Long) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(statusColor(occurrence.status))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                occurrence.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                listOfNotNull(
                    occurrence.module.label,
                    occurrence.deadline?.let { "DDL ${it.format(deadlineFormatter)}" },
                    occurrence.completedAt?.let { "完成 ${it.format(deadlineFormatter)}" },
                    "${occurrence.difficulty.label} ${occurrence.difficulty.points}分"
                ).joinToString("  ·  "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (occurrence.status != TaskStatus.ARCHIVED) {
            IconButton(onClick = { onArchive(occurrence.id) }) {
                Icon(Icons.Default.Archive, contentDescription = "归档")
            }
        }
    }
}

@Composable
private fun MineScreen(
    state: DdlMouseUiState,
    onNotificationsChanged: (Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Header("我的", "设置、统计和后续 PK 入口。")
        }
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("当前统计", fontWeight = FontWeight.SemiBold)
                    Text("总积分 ${state.petState.points} · 等级 Lv.${state.petState.level}")
                    Text("已解锁装扮 ${state.petState.unlockedDresses.size} · 表情 ${state.petState.unlockedExpressions.size}")
                }
            }
        }
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("DDL 提醒", fontWeight = FontWeight.SemiBold)
                        Text("创建任务时自动生成，可后续扩展为多提醒编辑。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = state.notificationsEnabled, onCheckedChange = onNotificationsChanged)
                }
            }
        }
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("附近 PK", fontWeight = FontWeight.SemiBold)
                    Text("入口已预留：真实定位、匹配和账号系统会在后续版本单独接入。")
                }
            }
        }
        item { Spacer(modifier = Modifier.height(88.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTaskDialog(
    initialModule: TaskModule,
    onDismiss: () -> Unit,
    onCreate: (TaskFormInput) -> Unit
) {
    TaskFormDialog(
        dialogTitle = "新增任务",
        confirmText = "创建",
        initialModule = initialModule,
        initialTemplate = null,
        onDismiss = onDismiss,
        onSubmit = onCreate
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTaskDialog(
    template: TaskTemplate,
    reminders: List<ReminderPlan>,
    onDismiss: () -> Unit,
    onSave: (TaskFormInput) -> Unit,
    onAddReminder: (String) -> Unit,
    onDeleteReminder: (Long) -> Unit
) {
    TaskFormDialog(
        dialogTitle = "编辑任务",
        confirmText = "保存",
        initialModule = template.module,
        initialTemplate = template,
        reminders = reminders,
        onDismiss = onDismiss,
        onSubmit = onSave,
        onAddReminder = onAddReminder,
        onDeleteReminder = onDeleteReminder
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskFormDialog(
    dialogTitle: String,
    confirmText: String,
    initialModule: TaskModule,
    initialTemplate: TaskTemplate?,
    reminders: List<ReminderPlan> = emptyList(),
    onDismiss: () -> Unit,
    onSubmit: (TaskFormInput) -> Unit,
    onAddReminder: (String) -> Unit = {},
    onDeleteReminder: (Long) -> Unit = {}
) {
    val formKey = initialTemplate?.id ?: initialModule.name
    var title by remember(formKey) { mutableStateOf(initialTemplate?.title.orEmpty()) }
    var module by remember(formKey) { mutableStateOf(initialTemplate?.module ?: initialModule) }
    var deadline by remember(formKey) { mutableStateOf(formatDeadlineInput(initialTemplate?.deadline)) }
    var difficulty by remember(formKey) { mutableStateOf(initialTemplate?.difficulty) }
    var note by remember(formKey) { mutableStateOf(initialTemplate?.note.orEmpty()) }
    var reminderEnabled by remember(formKey) { mutableStateOf(initialTemplate?.reminderEnabled ?: true) }
    var reminderTime by remember(formKey) {
        mutableStateOf(formatMinuteOfDay(initialTemplate?.preferredReminderMinuteOfDay))
    }
    var timeBucket by remember(formKey) { mutableStateOf(initialTemplate?.timeBucket.orEmpty()) }
    var weeklyDays by remember(formKey) {
        mutableStateOf(initialTemplate?.weeklyDays?.sorted()?.joinToString(",").orEmpty())
    }
    var monthlyDay by remember(formKey) { mutableStateOf(initialTemplate?.monthlyDay?.toString().orEmpty()) }
    var projectStage by remember(formKey) { mutableStateOf(initialTemplate?.projectStage.orEmpty()) }
    var newReminder by remember(formKey) { mutableStateOf("") }
    val fields = TaskFormPolicy.fieldsFor(module)
    val parsedDeadline = parseDeadlinePreview(deadline)
    val recommendedDifficulty = DifficultyPolicy.recommend(module, parsedDeadline, LocalDateTime.now())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogTitle) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("任务名称") },
                    singleLine = true
                )
                ModuleChips(selected = module, onSelected = { module = it })
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Difficulty.entries.forEach { option ->
                        FilterChip(
                            selected = (difficulty ?: recommendedDifficulty) == option,
                            onClick = { difficulty = option },
                            label = { Text("${option.label} +${option.points}") }
                        )
                    }
                }
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(TaskFormField.NOTE.label) },
                    minLines = 2,
                    maxLines = 3
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(TaskFormField.REMINDER_ENABLED.label, fontWeight = FontWeight.SemiBold)
                    Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                }
                if (fields.contains(TaskFormField.TIME_BUCKET)) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = timeBucket,
                        onValueChange = { timeBucket = it },
                        label = { Text(TaskFormField.TIME_BUCKET.label) },
                        placeholder = { Text("早 / 中 / 晚 / 睡前") },
                        singleLine = true
                    )
                }
                if (fields.contains(TaskFormField.WEEKLY_DAYS)) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = weeklyDays,
                        onValueChange = { weeklyDays = it },
                        label = { Text(TaskFormField.WEEKLY_DAYS.label) },
                        placeholder = { Text("1,3,5 或 一三五") },
                        singleLine = true
                    )
                }
                if (fields.contains(TaskFormField.MONTHLY_DAY)) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = monthlyDay,
                        onValueChange = { monthlyDay = it },
                        label = { Text(TaskFormField.MONTHLY_DAY.label) },
                        placeholder = { Text("1-31") },
                        singleLine = true
                    )
                }
                if (fields.contains(TaskFormField.DEADLINE_OPTIONAL) ||
                    fields.contains(TaskFormField.DEADLINE_REQUIRED) ||
                    fields.contains(TaskFormField.WEEKLY_DEADLINE_TIME) ||
                    fields.contains(TaskFormField.MONTHLY_DEADLINE_TIME)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = deadline,
                        onValueChange = { deadline = it },
                        label = {
                            Text(
                                if (fields.contains(TaskFormField.DEADLINE_REQUIRED)) {
                                    TaskFormField.DEADLINE_REQUIRED.label
                                } else {
                                    "本期 DDL，可空"
                                }
                            )
                        },
                        placeholder = { Text("2026-05-20 或 2026-05-20T21:00") },
                        singleLine = true
                    )
                }
                if (fields.contains(TaskFormField.PROJECT_STAGE)) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = projectStage,
                        onValueChange = { projectStage = it },
                        label = { Text(TaskFormField.PROJECT_STAGE.label) },
                        placeholder = { Text("资料 / 初稿 / 修改 / 提交") },
                        singleLine = true
                    )
                }
                if (reminderEnabled &&
                    (fields.contains(TaskFormField.DAILY_REMINDER_TIME) || fields.contains(TaskFormField.REMINDER_PREVIEW))
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = reminderTime,
                        onValueChange = { reminderTime = it },
                        label = {
                            Text(
                                if (fields.contains(TaskFormField.DAILY_REMINDER_TIME)) {
                                    TaskFormField.DAILY_REMINDER_TIME.label
                                } else {
                                    "手动提醒时间"
                                }
                            )
                        },
                        placeholder = { Text("09:00") },
                        singleLine = true
                    )
                }
                if (fields.contains(TaskFormField.REMINDER_PREVIEW)) {
                    Text(
                        reminderPreview(parsedDeadline),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (initialTemplate != null) {
                    ReminderEditor(
                        reminders = reminders,
                        newReminder = newReminder,
                        onNewReminderChanged = { newReminder = it },
                        onAddReminder = {
                            onAddReminder(newReminder)
                            newReminder = ""
                        },
                        onDeleteReminder = onDeleteReminder
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        TaskFormInput(
                            title = title,
                            module = module,
                            deadlineText = deadline,
                            difficulty = difficulty,
                            note = note,
                            reminderEnabled = reminderEnabled,
                            reminderTimeText = reminderTime,
                            timeBucket = timeBucket,
                            weeklyDaysText = weeklyDays,
                            monthlyDayText = monthlyDay,
                            projectStage = projectStage
                        )
                    )
                },
                enabled = title.isNotBlank()
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun ReminderEditor(
    reminders: List<ReminderPlan>,
    newReminder: String,
    onNewReminderChanged: (String) -> Unit,
    onAddReminder: () -> Unit,
    onDeleteReminder: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("提醒点", fontWeight = FontWeight.SemiBold)
        if (reminders.isEmpty()) {
            Text("还没有单独提醒点。", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            reminders.forEach { reminder ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        reminder.triggerAt.format(deadlineFormatter),
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = { onDeleteReminder(reminder.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除提醒")
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = newReminder,
                onValueChange = onNewReminderChanged,
                label = { Text("新增提醒") },
                placeholder = { Text("09:00 或 2026-05-20T09:00") },
                singleLine = true
            )
            Button(onClick = onAddReminder, enabled = newReminder.isNotBlank()) {
                Text("添加")
            }
        }
    }
}

private val formInputFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

private fun formatDeadlineInput(value: LocalDateTime?): String {
    return value?.format(formInputFormatter).orEmpty()
}

private fun formatMinuteOfDay(value: Int?): String {
    if (value == null) return ""
    return "%02d:%02d".format(value / 60, value % 60)
}

private fun parseDeadlinePreview(text: String): LocalDateTime? {
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

private fun reminderPreview(deadline: LocalDateTime?): String {
    if (deadline == null) return "提醒预览：填写 DDL 后生成"
    val reminders = ReminderPolicy.defaultReminderTimes(LocalDateTime.now(), deadline)
    if (reminders.isEmpty()) return "提醒预览：临近 DDL 时提醒"
    return "提醒预览：" + reminders
        .take(3)
        .joinToString(" / ") { it.format(deadlineFormatter) }
}

@Composable
private fun DailySummaryDialog(summary: DailySummary, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("昨天总结") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryBlock("完成", summary.completedTitles.ifEmpty { listOf("暂无完成任务") })
                SummaryBlock("未完成", summary.missedTitles.ifEmpty { listOf("暂无未完成任务") })
                SummaryBlock("DDL 提醒", summary.upcomingDeadlineTitles.ifEmpty { listOf("暂无临近 DDL") })
                Text("得分 +${summary.pointsEarned}，扣分 -${summary.pointsLost}")
                Text("“${summary.petLine}”", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("知道了")
            }
        }
    )
}

@Composable
private fun SummaryBlock(title: String, rows: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold)
        rows.forEach { Text("• $it") }
    }
}

private val deadlineFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")
