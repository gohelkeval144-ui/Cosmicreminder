package com.cosmic.reminder

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val Bg = Color(0xFFF5F7FB)
private val Surface = Color(0xFFFFFFFF)
private val Accent = Color(0xFF3157C8)
private val Mint = Color(0xFF17866D)
private val Muted = Color(0xFF667085)
private val Ink = Color(0xFF182230)
private val Border = Color(0xFFE1E6EF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
        }
        setContent { CosmicApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CosmicApp() {
    val context = LocalContext.current
    val store = remember { ReminderStore(context) }
    var reminders by remember { mutableStateOf(store.load()) }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("All") }
    var showAdd by remember { mutableStateOf(false) }

    fun update(next: List<Reminder>) {
        reminders = next.sortedWith(compareBy<Reminder> { it.done }.thenBy { it.dueAt })
        store.save(reminders)
    }

    val visible = reminders.filter {
        (it.title.contains(query, true) || it.note.contains(query, true)) &&
        when (filter) { "Today" -> sameDay(it.dueAt, System.currentTimeMillis()) && !it.done
            "Upcoming" -> it.dueAt > System.currentTimeMillis() && !it.done
            "Done" -> it.done
            else -> true }
    }
    val pending = reminders.count { !it.done }
    val today = reminders.count { !it.done && sameDay(it.dueAt, System.currentTimeMillis()) }

    MaterialTheme(colorScheme = lightColorScheme(
        background = Bg, surface = Surface, primary = Accent, secondary = Mint,
        onBackground = Ink, onSurface = Ink, surfaceVariant = Color(0xFFEEF2F8),
        outline = Border
    )) {
        Scaffold(containerColor = Bg, floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAdd = true }, containerColor = Accent, contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, null) }, text = { Text("New reminder", fontWeight = FontWeight.Bold) }
            )
        }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("COSMIC", color = Accent, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold)
                        Text("Your day, organised.", color = Ink, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                    Box(Modifier.size(48.dp).background(Surface, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.NotificationsActive, null, tint = Mint, modifier = Modifier.size(25.dp))
                    }
                }
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("TO DO", pending.toString(), Accent, Modifier.weight(1f))
                    StatCard("TODAY", today.toString(), Mint, Modifier.weight(1f))
                }
                Spacer(Modifier.height(18.dp))
                OutlinedTextField(
                    value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search your reminders…", color = Muted) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Muted) },
                    singleLine = true, shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent, unfocusedBorderColor = Border,
                        focusedContainerColor = Surface, unfocusedContainerColor = Surface
                    )
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Today", "Upcoming", "Done").forEach { tab ->
                        FilterChip(
                            selected = filter == tab, onClick = { filter = tab }, label = { Text(tab) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Accent, selectedLabelColor = Color.White,
                                labelColor = Muted
                            ), border = null
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                if (visible.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("✦", color = Accent, style = MaterialTheme.typography.displayMedium)
                            Text("No reminders yet", color = Ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                            Text("Tap New reminder to add your first one.", color = Muted)
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(visible, key = { it.id }) { item ->
                            ReminderCard(
                                item = item,
                                onDone = {
                                    val changed = item.copy(done = !item.done)
                                    if (changed.done) ReminderAlarm.cancel(context, item.id)
                                    else ReminderAlarm.schedule(context, changed)
                                    update(reminders.map { if (it.id == item.id) changed else it })
                                },
                                onDelete = {
                                    ReminderAlarm.cancel(context, item.id)
                                    update(reminders.filterNot { it.id == item.id })
                                }
                            )
                        }
                    }
                }
            }
        }
        if (showAdd) {
            AddReminderDialog(
                onDismiss = { showAdd = false },
                onSave = { title, note, category, priority, due ->
                    val item = Reminder(System.currentTimeMillis(), title, note, category, priority, due)
                    update(reminders + item)
                    ReminderAlarm.schedule(context, item)
                    showAdd = false
                }
            )
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(label, color = Muted, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(value, color = color, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun ReminderCard(item: Reminder, onDone: () -> Unit, onDelete: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(42.dp).background(if (item.done) Mint.copy(alpha = .18f) else Accent.copy(alpha = .16f), CircleShape)
                    .clickable { onDone() }, contentAlignment = Alignment.Center
            ) {
                Icon(if (item.done) Icons.Default.Check else Icons.Default.NotificationsActive,
                    null, tint = if (item.done) Mint else Accent)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, color = if (item.done) Muted else Ink, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    color = if (item.done) Muted else Ink)
                if (item.note.isNotBlank()) Text(item.note, color = Muted, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(5.dp))
                Text("${formatDate(item.dueAt)}  ·  ${item.category}  ·  ${item.priority}",
                    color = if (item.priority == "High") Color(0xFFFF9E9E) else Mint,
                    style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Muted) }
        }
    }
}

@Composable
private fun AddReminderDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Long) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Personal") }
    var priority by remember { mutableStateOf("Normal") }
    var due by remember { mutableLongStateOf(System.currentTimeMillis() + 60 * 60 * 1000L) }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New reminder", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(title, { title = it; error = false }, label = { Text("What do you need to do?") }, singleLine = true)
                OutlinedTextField(note, { note = it }, label = { Text("Notes (optional)") }, minLines = 2)
                TextButton(onClick = {
                    val c = Calendar.getInstance().apply { timeInMillis = due }
                    DatePickerDialog(context, { _, y, m, d ->
                        c.set(Calendar.YEAR, y); c.set(Calendar.MONTH, m); c.set(Calendar.DAY_OF_MONTH, d)
                        due = c.timeInMillis
                    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
                }) { Text("📅  ${SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(Date(due))}") }
                TextButton(onClick = {
                    val c = Calendar.getInstance().apply { timeInMillis = due }
                    TimePickerDialog(context, { _, h, m ->
                        c.set(Calendar.HOUR_OF_DAY, h); c.set(Calendar.MINUTE, m); c.set(Calendar.SECOND, 0)
                        due = c.timeInMillis
                    }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show()
                }) { Text("⏰  ${SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(due))}") }
                Text("Category", color = Muted)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    listOf("Personal", "Work", "Health").forEach {
                        FilterChip(category == it, { category = it }, label = { Text(it) })
                    }
                }
                Text("Priority", color = Muted)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    listOf("Low", "Normal", "High").forEach {
                        FilterChip(priority == it, { priority = it }, label = { Text(it) })
                    }
                }
                if (error) Text("Please enter a reminder title.", color = Color(0xFFFF9E9E))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isBlank()) error = true
                else onSave(title.trim(), note.trim(), category, priority, due)
            }) { Text("Save reminder") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        containerColor = Surface, titleContentColor = Ink, textContentColor = Ink
    )
}

private fun formatDate(time: Long): String =
    SimpleDateFormat("EEE, d MMM · h:mm a", Locale.getDefault()).format(Date(time))

private fun sameDay(a: Long, b: Long): Boolean {
    val ca = Calendar.getInstance().apply { timeInMillis = a }
    val cb = Calendar.getInstance().apply { timeInMillis = b }
    return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) &&
        ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR)
}
