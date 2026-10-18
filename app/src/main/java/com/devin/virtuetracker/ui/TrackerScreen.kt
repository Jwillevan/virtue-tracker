package com.devin.virtuetracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.devin.virtuetracker.TrackerViewModel
import com.devin.virtuetracker.data.TrackedItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(viewModel: TrackerViewModel) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var editing by remember { mutableStateOf<TrackedItem?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }
    val activeItems = viewModel.items.filter { it.active }
    val completed = viewModel.completedCount()
    val total = activeItems.size
    val progress = if (total == 0) 0f else completed.toFloat() / total
    val items = if (selectedTab == 0) activeItems else viewModel.items

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$completed / $total") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Jump to today") }, onClick = {
                            viewModel.goToToday(); showMenu = false
                        })
                        if (selectedTab == 1) {
                            DropdownMenuItem(text = { Text("Reset to Franklin's 13 virtues") }, onClick = {
                                confirmReset = true; showMenu = false
                            })
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.size(56.dp)
                ) { Icon(Icons.Default.Add, contentDescription = "Add task") }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
            )
            DateHeader(
                date = viewModel.selectedDate,
                isToday = viewModel.isToday,
                onPrev = viewModel::goToPreviousDay,
                onNext = viewModel::goToNextDay,
                onToday = viewModel::goToToday
            )
            TabRow(selectedTabIndex = selectedTab) {
                listOf("Overview", "Tasks").forEachIndexed { index, label ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(label) })
                }
            }
            if (items.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f).padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(if (selectedTab == 0) "No active tasks. Manage tasks on the Tasks tab."
                         else "No tasks yet. Tap + to add one, or restore the default virtues from the menu.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        ItemRow(item, viewModel.isCompleted(item.id),
                            onSelect = if (selectedTab == 1) ({ editing = item }) else null,
                            onToggle = if (selectedTab == 0) ({ viewModel.toggle(item.id) }) else null)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        ItemEditorDialog(
            item = null,
            completed = false,
            date = viewModel.selectedDate,
            onConfirm = { name, description, subItems, active, checked ->
                viewModel.addItem(name, description, subItems, active, checked)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
    editing?.let { item ->
        ItemEditorDialog(
            item = item,
            completed = viewModel.isCompleted(item.id),
            date = viewModel.selectedDate,
            onConfirm = { name, description, subItems, active, checked ->
                viewModel.updateItem(item.id, name, description, subItems, active, checked)
                editing = null
            },
            onDelete = { viewModel.deleteItem(item.id); editing = null },
            onDismiss = { editing = null }
        )
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset list?") },
            text = { Text("This replaces your current list with Benjamin Franklin's 13 virtues. Your daily check-off history is kept.") },
            confirmButton = { TextButton(onClick = { viewModel.resetToDefaultVirtues(); confirmReset = false }) { Text("Reset") } },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun DateHeader(
    date: LocalDate,
    isToday: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous day")
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isToday) "Today" else date.format(formatter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (isToday) {
                Text(
                    text = date.format(formatter),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        if (isToday) {
            IconButton(onClick = onNext, enabled = false) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Next day",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f)
                )
            }
        } else {
            IconButton(onClick = onNext) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next day")
            }
        }
    }
    if (!isToday) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            TextButton(onClick = onToday) {
                Icon(Icons.Default.Today, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Jump to today")
            }
        }
    }
}

@Composable
private fun ItemRow(item: TrackedItem, checked: Boolean, onSelect: (() -> Unit)?, onToggle: (() -> Unit)?) {
    Card(
        modifier = Modifier.fillMaxWidth().then(if (onSelect != null) Modifier.clickable(onClick = onSelect) else Modifier),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.padding(end = 12.dp).size(48.dp).then(
                    if (onToggle != null) Modifier.toggleable(
                        value = checked,
                        role = Role.Checkbox,
                        onValueChange = { onToggle() }
                    ) else Modifier
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (checked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (onToggle != null) {
                        if (checked) "Mark ${item.name} incomplete" else "Mark ${item.name} complete"
                    } else if (checked) "Completed" else "Not completed",
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None)
                if (item.description.isNotBlank()) {
                    Text(item.description, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
                item.subItems.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                if (!item.active) Text("Inactive", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun ItemEditorDialog(
    item: TrackedItem?,
    completed: Boolean,
    date: LocalDate,
    onConfirm: (String, String, List<String>, Boolean, Boolean) -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by rememberSaveable(item?.id) { mutableStateOf(item?.name ?: "") }
    var description by rememberSaveable(item?.id) { mutableStateOf(item?.description ?: "") }
    var subItems by rememberSaveable(item?.id) { mutableStateOf(item?.subItems?.joinToString("\n") ?: "") }
    var active by rememberSaveable(item?.id) { mutableStateOf(item?.active ?: true) }
    var checked by rememberSaveable(item?.id) { mutableStateOf(completed) }
    var confirmDelete by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Add task" else "Edit task") },
        text = {
            Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(description, { description = it }, label = { Text("Description (optional)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(subItems, { subItems = it }, label = { Text("Sub-items (one per line)") }, minLines = 2, maxLines = 5, modifier = Modifier.fillMaxWidth())
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (active) "Active" else "Inactive", Modifier.weight(1f))
                    Switch(active, { active = it }, Modifier.semantics { contentDescription = "Task active" })
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked, { checked = it }, Modifier.semantics { contentDescription = "Task completed" })
                    Text("Completed on $date", Modifier.weight(1f))
                }
                if (onDelete != null) {
                    TextButton(onClick = { confirmDelete = true }) { Text("Delete task", color = MaterialTheme.colorScheme.error) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, description, subItems.lines(), active, checked) }, enabled = name.isNotBlank()) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
    if (confirmDelete && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete task?") },
            text = { Text("Delete this task and its completion history?") },
            confirmButton = { TextButton(onClick = onDelete) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}
