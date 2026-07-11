package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Attendance
import com.example.data.Excuse
import com.example.data.Notification
import com.example.ui.SchoolViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentScreen(
    viewModel: SchoolViewModel,
    onLogout: () -> Unit
) {
    val parent by viewModel.loggedParent.collectAsState()
    val student by viewModel.parentStudent.collectAsState()
    val attendanceList by viewModel.parentStudentAttendance.collectAsState()
    val excusesList by viewModel.parentStudentExcuses.collectAsState()
    val notifications by viewModel.parentNotifications.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Live, 1: Historique, 2: Justificatifs
    var showExcuseDialog by remember { mutableStateOf(false) }

    // Grab today's date
    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    // Check if child has a marked attendance for today
    val todayAttendance = remember(attendanceList, todayDateStr) {
        attendanceList.find { it.date == todayDateStr }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Espace Parent",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Déconnexion",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            if (activeTab == 2) {
                ExtendedFloatingActionButton(
                    text = { Text("Justifier Absence", color = Color.White) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) },
                    onClick = { showExcuseDialog = true },
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Parent & Child Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                        }
                        Column {
                            Text(
                                text = parent?.name ?: "Parent d'élève",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Identifiant parent : ${parent?.id ?: ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }

                    Divider(color = Color.White.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Élève suivi : ${student?.name ?: "Chargement..."}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Classe : ${student?.classId?.replace("_", " ") ?: "..."} • Matricule: ${student?.matricule ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        
                        // Small avatar icon
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.tertiary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = student?.name?.firstOrNull()?.toString()?.uppercase() ?: "",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Tab Rows
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Aujourd'hui", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Historique", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.History, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("Justificatifs", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null) }
                )
            }

            // Tab contents
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeTab) {
                    0 -> LiveTodayTab(
                        todayAttendance = todayAttendance,
                        notifications = notifications,
                        todayDateStr = todayDateStr
                    )
                    1 -> HistoryTab(attendanceList = attendanceList)
                    2 -> ExcusesTab(excusesList = excusesList)
                }
            }
        }

        // Justification Dialog Form
        if (showExcuseDialog) {
            ExcuseSubmissionDialog(
                onDismiss = { showExcuseDialog = false },
                onSubmit = { reason, date ->
                    viewModel.submitExcuse(reason, date)
                    showExcuseDialog = false
                },
                todayDate = todayDateStr
            )
        }
    }
}

// ----------------------------------------------------
// Today Tab
// ----------------------------------------------------
@Composable
fun LiveTodayTab(
    todayAttendance: Attendance?,
    notifications: List<Notification>,
    todayDateStr: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Live status Card
        item {
            Text(
                text = "Statut de présence aujourd'hui",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            val statusColor = when (todayAttendance?.status) {
                "PRESENT" -> MaterialTheme.colorScheme.tertiary
                "ABSENT" -> MaterialTheme.colorScheme.error
                "LATE" -> Color(0xFFF59E0B)
                null -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.primary
            }

            val statusLabel = when (todayAttendance?.status) {
                "PRESENT" -> "PRÉSENT(E)"
                "ABSENT" -> "ABSENT(E)"
                "LATE" -> "EN RETARD"
                null -> "APPEL EN COURS..."
                else -> todayAttendance.status
            }

            val statusIcon = when (todayAttendance?.status) {
                "PRESENT" -> Icons.Default.CheckCircle
                "ABSENT" -> Icons.Default.Cancel
                "LATE" -> Icons.Default.HourglassEmpty
                null -> Icons.Default.HourglassTop
                else -> Icons.Default.Info
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(statusColor.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusColor
                        )
                        Text(
                            text = "Date : $todayDateStr",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        todayAttendance?.observation?.let { obs ->
                            if (obs.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Remarque Enseignant : \"$obs\"",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Notification Log Feed
        item {
            Text(
                text = "Notifications push en temps réel",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (notifications.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aucune notification push reçue.",
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            items(notifications) { notif ->
                val timeString = remember(notif.timestamp) {
                    SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(notif.timestamp))
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = notif.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = timeString,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = notif.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// History Tab
// ----------------------------------------------------
@Composable
fun HistoryTab(attendanceList: List<Attendance>) {
    if (attendanceList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EventNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "Aucun historique disponible.",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(attendanceList) { record ->
                val badgeColor = when (record.status) {
                    "PRESENT" -> MaterialTheme.colorScheme.tertiary
                    "ABSENT" -> MaterialTheme.colorScheme.error
                    "LATE" -> Color(0xFFF59E0B)
                    else -> MaterialTheme.colorScheme.primary
                }

                val label = when (record.status) {
                    "PRESENT" -> "Présent(e)"
                    "ABSENT" -> "Absent(e)"
                    "LATE" -> "En retard"
                    else -> record.status
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Date : ${record.date}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            record.observation?.let { obs ->
                                if (obs.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Obs : \"$obs\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// Excuses/Justificatifs Tab
// ----------------------------------------------------
@Composable
fun ExcusesTab(excusesList: List<Excuse>) {
    if (excusesList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Attachment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "Aucun justificatif d'absence soumis.",
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Cliquez sur '+' pour justifier une absence",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(excusesList) { excuse ->
                val statusColor = when (excuse.status) {
                    "APPROVED" -> MaterialTheme.colorScheme.tertiary
                    "PENDING" -> Color(0xFFF59E0B)
                    "REJECTED" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.secondary
                }

                val statusLabel = when (excuse.status) {
                    "APPROVED" -> "APPROUVÉ"
                    "PENDING" -> "EN ATTENTE"
                    "REJECTED" -> "REFUSÉ"
                    else -> excuse.status
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Date de l'absence : ${excuse.date}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Card(
                                colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = statusLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = "Raison fournie :",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Text(
                            text = excuse.reason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// Submit Excuse Dialog
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExcuseSubmissionDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit,
    todayDate: String
) {
    var dateInput by remember { mutableStateOf(todayDate) }
    var reasonDetails by remember { mutableStateOf("") }
    var selectedReasonChip by remember { mutableStateOf("Maladie 🤒") }

    val chips = listOf(
        "Maladie 🤒",
        "Raison familiale 🏠",
        "Panne de transport 🚌",
        "Rendez-vous médical 🏥",
        "Autre ✏️"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Justifier une absence",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Date input field
                OutlinedTextField(
                    value = dateInput,
                    onValueChange = { dateInput = it },
                    label = { Text("Date de l'absence (AAAA-MM-JJ)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Motif de l'absence :",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                // Grid/Row of quick-tap motif chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    chips.take(3).forEach { chip ->
                        val isSelected = selectedReasonChip == chip
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedReasonChip = chip },
                            label = { Text(chip, fontSize = 11.sp) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    chips.drop(3).forEach { chip ->
                        val isSelected = selectedReasonChip == chip
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedReasonChip = chip },
                            label = { Text(chip, fontSize = 11.sp) }
                        )
                    }
                }

                // Description field
                OutlinedTextField(
                    value = reasonDetails,
                    onValueChange = { reasonDetails = it },
                    label = { Text("Précisions (obligatoire)") },
                    placeholder = { Text("Détaillez la raison ici...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reasonDetails.isBlank()) return@Button
                    val fullReason = "$selectedReasonChip - $reasonDetails"
                    onSubmit(fullReason, dateInput)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                enabled = reasonDetails.isNotBlank()
            ) {
                Text("Soumettre", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
