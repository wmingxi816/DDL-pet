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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilterChip
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
import com.ddlmouse.app.domain.PetState
import com.ddlmouse.app.domain.StoreCategory
import com.ddlmouse.app.domain.StoreItem
import com.ddlmouse.app.domain.TaskModule
import com.ddlmouse.app.domain.TaskOccurrence
import com.ddlmouse.app.domain.TaskStatus
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
                    onModuleSelected = viewModel::selectModule,
                    onComplete = viewModel::completeOccurrence
                )
                MainTab.PET -> PetScreen(
                    state = state,
                    onFeed = viewModel::feedPet,
                    onInteract = viewModel::interactWithPet,
                    onBuy = viewModel::buyItem
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
    MainTab.MINE -> Icons.Default.Person
}

@Composable
private fun TaskScreen(
    state: DdlMouseUiState,
    onModuleSelected: (TaskModule) -> Unit,
    onComplete: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Header("任务", "今天先拆最小的一步，积分会喂饱鼠鼠。")
        }
        item {
            UrgentPanel(state.urgentOccurrences)
        }
        item {
            ModuleChips(
                selected = state.selectedModule,
                onSelected = onModuleSelected
            )
        }
        items(state.filteredOccurrences, key = { it.id }) { occurrence ->
            TaskCard(occurrence = occurrence, onComplete = { onComplete(occurrence.id) })
        }
        item {
            Spacer(modifier = Modifier.height(88.dp))
        }
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
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
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
private fun TaskCard(occurrence: TaskOccurrence, onComplete: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(occurrence.title, fontWeight = FontWeight.SemiBold)
                Text(
                    text = listOfNotNull(
                        occurrence.module.label,
                        occurrence.deadline?.let { "DDL ${it.format(deadlineFormatter)}" },
                        "${occurrence.difficulty.label} +${occurrence.difficulty.points}"
                    ).joinToString("  ·  "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatusChip(occurrence.status)
            }
            if (occurrence.status == TaskStatus.PENDING) {
                IconButton(onClick = onComplete) {
                    Icon(Icons.Default.Check, contentDescription = "完成")
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: TaskStatus) {
    val label = when (status) {
        TaskStatus.PENDING -> "进行中"
        TaskStatus.COMPLETED -> "已完成"
        TaskStatus.MISSED -> "未完成"
    }
    AssistChip(onClick = {}, label = { Text(label) })
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
    onCreate: (String, TaskModule, String, Difficulty?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var module by remember { mutableStateOf(initialModule) }
    var deadline by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf<Difficulty?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新增任务") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("任务名称") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("DDL 日期，可空") },
                    placeholder = { Text("2026-05-20 或 2026-05-20T21:00") },
                    singleLine = true
                )
                ModuleChips(selected = module, onSelected = { module = it })
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Difficulty.entries.forEach { option ->
                        FilterChip(
                            selected = difficulty == option,
                            onClick = { difficulty = option },
                            label = { Text("${option.label} +${option.points}") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(title, module, deadline, difficulty) },
                enabled = title.isNotBlank()
            ) {
                Text("创建")
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
