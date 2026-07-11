package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.Student
import com.example.ui.SchoolViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherScreen(
    viewModel: SchoolViewModel,
    onLogout: () -> Unit
) {
    val teacher by viewModel.loggedTeacher.collectAsState()
    val activeClassId by viewModel.activeClassId.collectAsState()
    val activeDate by viewModel.activeDate.collectAsState()
    val students by viewModel.activeClassStudents.collectAsState()
    
    val tempAttendance by viewModel.tempAttendanceMap.collectAsState()
    val tempObservations by viewModel.tempObservationMap.collectAsState()

    var showConfirmationDialog by remember { mutableStateOf(false) }
    var expandedObservationStudentId by remember { mutableStateOf<String?>(null) }

    // Calendar strip generator: past 5 school days + today
    val dateStrip = remember {
        val list = mutableListOf<String>()
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        
        // Let's generate last 6 school days
        var addedCount = 0
        while (addedCount < 6) {
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek != Calendar.SUNDAY && dayOfWeek != Calendar.SATURDAY) {
                list.add(format.format(cal.time))
                addedCount++
            }
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        list.reverse()
        list
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Appel Journalier",
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Welcome Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column {
                        Text(
                            text = teacher?.name ?: "Enseignant",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Classe assignée : ${activeClassId?.replace("_", " ") ?: "Aucune"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Calendar / Date strip Selection
            Text(
                text = "Sélectionner la date d'appel",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.primary
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dateStrip) { dateString ->
                    val isSelected = activeDate == dateString
                    val parsedDate = remember(dateString) {
                        try {
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
                        } catch (e: Exception) {
                            Date()
                        }
                    }
                    val dayLabel = remember(parsedDate) {
                        SimpleDateFormat("EEE", Locale.FRANCE).format(parsedDate).uppercase()
                    }
                    val dateLabel = remember(parsedDate) {
                        SimpleDateFormat("dd MMM", Locale.FRANCE).format(parsedDate)
                    }

                    Card(
                        modifier = Modifier
                            .width(84.dp)
                            .clickable { viewModel.setActiveDate(dateString) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dateLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Student Roster
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Liste des élèves (${students.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Date : $activeDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
            }

            if (students.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "Aucun élève trouvé dans cette classe.",
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(students) { student ->
                        val currentStatus = tempAttendance[student.matricule] ?: "PRESENT"
                        val currentObs = tempObservations[student.matricule] ?: ""
                        val isObsExpanded = expandedObservationStudentId == student.matricule

                        StudentAttendanceRow(
                            student = student,
                            status = currentStatus,
                            observation = currentObs,
                            isObsExpanded = isObsExpanded,
                            onStatusChange = { status ->
                                viewModel.updateTempStatus(student.matricule, status)
                            },
                            onObservationChange = { obs ->
                                viewModel.updateTempObservation(student.matricule, obs)
                            },
                            onToggleObs = {
                                expandedObservationStudentId = if (isObsExpanded) null else student.matricule
                            }
                        )
                    }
                }
            }
        }

        // Floating Footer Validation Banner
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Timer helper showing fast validation
                    Column(modifier = Modifier.weight(1.2f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Moins de 2 min",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        Text(
                            text = "Envoie une alerte push immédiate aux parents",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = { showConfirmationDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Valider l'appel",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Confirmation Dialog
        if (showConfirmationDialog) {
            val absentCount = tempAttendance.values.count { it == "ABSENT" }
            val lateCount = tempAttendance.values.count { it == "LATE" }
            val presentCount = tempAttendance.values.count { it == "PRESENT" }

            AlertDialog(
                onDismissRequest = { showConfirmationDialog = false },
                title = { Text("Confirmer l'appel ?", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Voulez-vous enregistrer l'appel pour le $activeDate ?")
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("Résumé de l'appel :", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("• Présents : $presentCount", style = MaterialTheme.typography.bodyMedium)
                                Text("• Absents : $absentCount", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                                Text("• En retard : $lateCount", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFF59E0B))
                            }
                        }
                        
                        Text(
                            "Cette action notifiera instantanément les parents concernés par SMS/Notification push.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.validateAttendance {
                                showConfirmationDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text("Confirmer", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmationDialog = false }) {
                        Text("Modifier")
                    }
                }
            )
        }
    }
}

@Composable
fun StudentAttendanceRow(
    student: Student,
    status: String,
    observation: String,
    isObsExpanded: Boolean,
    onStatusChange: (String) -> Unit,
    onObservationChange: (String) -> Unit,
    onToggleObs: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Initials Placeholder
                val initials = student.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2).uppercase()
                
                // Color based on initials to keep them colorful
                val seedColor = remember(student.matricule) {
                    val colorsList = listOf(
                        Color(0xFF1E3A8A), Color(0xFF10B981), Color(0xFF3B82F6),
                        Color(0xFFF59E0B), Color(0xFF8B5CF6), Color(0xFFEC4899),
                        Color(0xFF06B6D4), Color(0xFFEF4444)
                    )
                    colorsList[Math.abs(student.matricule.hashCode()) % colorsList.size]
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(seedColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = seedColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Matricule: ${student.matricule}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                IconButton(onClick = onToggleObs) {
                    Icon(
                        imageVector = if (isObsExpanded) Icons.Default.ChatBubble else Icons.Default.ChatBubbleOutline,
                        contentDescription = "Observation",
                        tint = if (observation.isNotEmpty()) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Segmented Attendance Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AttendanceToggleBtn(
                    label = "Présent",
                    activeColor = MaterialTheme.colorScheme.tertiary,
                    isActive = status == "PRESENT",
                    modifier = Modifier.weight(1f),
                    onClick = { onStatusChange("PRESENT") }
                )
                AttendanceToggleBtn(
                    label = "Absent",
                    activeColor = MaterialTheme.colorScheme.error,
                    isActive = status == "ABSENT",
                    modifier = Modifier.weight(1f),
                    onClick = { onStatusChange("ABSENT") }
                )
                AttendanceToggleBtn(
                    label = "Retard",
                    activeColor = Color(0xFFF59E0B), // Warm orange
                    isActive = status == "LATE",
                    modifier = Modifier.weight(1f),
                    onClick = { onStatusChange("LATE") }
                )
            }

            // Expandable Observation Tray
            AnimatedVisibility(
                visible = isObsExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    
                    Text(
                        text = "Observation comportementale / participation :",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    OutlinedTextField(
                        value = observation,
                        onValueChange = onObservationChange,
                        placeholder = { Text("Ex: Devoir non fait, bavardages...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Quick-Insert Chips row
                    val chips = listOf(
                        "Félicitations 🌟",
                        "Très actif 💬",
                        "Bavardages ⚠️",
                        "Devoir non fait 📝"
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(chips) { chip ->
                            Card(
                                modifier = Modifier.clickable {
                                    val prefix = if (observation.isEmpty()) "" else "$observation, "
                                    // Strip emoji if we want, or append directly
                                    onObservationChange(prefix + chip)
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(
                                    text = chip,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceToggleBtn(
    label: String,
    activeColor: Color,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor = if (isActive) activeColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    val contentColor = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = modifier
            .height(38.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 2.dp else 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp
                ),
                color = contentColor
            )
        }
    }
}
