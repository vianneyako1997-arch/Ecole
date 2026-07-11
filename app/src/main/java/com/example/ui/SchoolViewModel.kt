package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class UserRole {
    NONE, TEACHER, PARENT, ADMIN
}

class SchoolViewModel(private val repository: SchoolRepository) : ViewModel() {

    // Login and user states
    private val _currentUserRole = MutableStateFlow(UserRole.NONE)
    val currentUserRole: StateFlow<UserRole> = _currentUserRole.asStateFlow()

    private val _loggedTeacher = MutableStateFlow<Teacher?>(null)
    val loggedTeacher: StateFlow<Teacher?> = _loggedTeacher.asStateFlow()

    private val _loggedParent = MutableStateFlow<Parent?>(null)
    val loggedParent: StateFlow<Parent?> = _loggedParent.asStateFlow()

    // Screen and filter states
    private val _activeClassId = MutableStateFlow<String?>(null)
    val activeClassId: StateFlow<String?> = _activeClassId.asStateFlow()

    private val _activeDate = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    val activeDate: StateFlow<String> = _activeDate.asStateFlow()

    // Navigation and snackbar helper states
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    // Database core exposures
    val classes: StateFlow<List<SchoolClass>> = repository.allClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teachers: StateFlow<List<Teacher>> = repository.allTeachers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allParents: StateFlow<List<Parent>> = repository.allParents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudents: StateFlow<List<Student>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendance: StateFlow<List<Attendance>> = repository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExcuses: StateFlow<List<Excuse>> = repository.allExcuses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered lists driven by selected values
    val activeClassStudents: StateFlow<List<Student>> = _activeClassId
        .flatMapLatest { classId ->
            if (classId != null) repository.getStudentsByClass(classId)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // We store standard in-memory temporary status edits before "Validating" attendance
    private val _tempAttendanceMap = MutableStateFlow<Map<String, String>>(emptyMap()) // studentMatricule -> "PRESENT"/"ABSENT"/"LATE"
    val tempAttendanceMap: StateFlow<Map<String, String>> = _tempAttendanceMap.asStateFlow()

    private val _tempObservationMap = MutableStateFlow<Map<String, String>>(emptyMap()) // studentMatricule -> Observation Text
    val tempObservationMap: StateFlow<Map<String, String>> = _tempObservationMap.asStateFlow()

    // Real-time parent notification streams
    val parentNotifications: StateFlow<List<Notification>> = _loggedParent
        .flatMapLatest { parent ->
            if (parent != null) repository.getNotificationsForUser(parent.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Student associated with logged-in parent
    val parentStudent: StateFlow<Student?> = _loggedParent
        .flatMapLatest { parent ->
            if (parent != null) {
                flow<Student?> {
                    emit(repository.getStudentByMatricule(parent.studentMatricule))
                }
            } else flowOf<Student?>(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Attendance records for parent's student
    val parentStudentAttendance: StateFlow<List<Attendance>> = parentStudent
        .flatMapLatest { student ->
            if (student != null) repository.getAttendanceForStudent(student.matricule)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Excuses submitted for parent's student
    val parentStudentExcuses: StateFlow<List<Excuse>> = parentStudent
        .flatMapLatest { student ->
            if (student != null) repository.getExcusesForStudent(student.matricule)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Automatically fetch or verify attendance when activeClassId or activeDate changes
        viewModelScope.launch {
            combine(_activeClassId, _activeDate) { classId, date -> Pair(classId, date) }
                .collect { (classId, date) ->
                    if (classId != null) {
                        // Query database for existing attendance records on this date
                        val existingRecords = repository.getAttendanceForClassAndDate(classId, date).first()
                        val newTempMap = mutableMapOf<String, String>()
                        val newObsMap = mutableMapOf<String, String>()
                        
                        existingRecords.forEach { record ->
                            newTempMap[record.studentMatricule] = record.status
                            record.observation?.let { obs ->
                                newObsMap[record.studentMatricule] = obs
                            }
                        }
                        
                        // For students with no record, default to PRESENT
                        val studentsList = repository.getStudentsByClass(classId).first()
                        studentsList.forEach { student ->
                            if (!newTempMap.containsKey(student.matricule)) {
                                newTempMap[student.matricule] = "PRESENT"
                            }
                        }
                        
                        _tempAttendanceMap.value = newTempMap
                        _tempObservationMap.value = newObsMap
                    }
                }
        }
    }

    // ----------------------------------------------------
    // Authentication
    // ----------------------------------------------------
    fun login(username: String, password: String, role: UserRole, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (role) {
                UserRole.ADMIN -> {
                    if (username.lowercase() == "admin" && password == "admin") {
                        _currentUserRole.value = UserRole.ADMIN
                        onSuccess()
                    } else {
                        onError("Identifiants Administrateur invalides.")
                    }
                }
                UserRole.TEACHER -> {
                    val teacher = repository.getTeacherById(username)
                    if (teacher != null && teacher.password == password) {
                        _loggedTeacher.value = teacher
                        _currentUserRole.value = UserRole.TEACHER
                        _activeClassId.value = teacher.classId
                        onSuccess()
                    } else {
                        onError("Identifiants Enseignant invalides.")
                    }
                }
                UserRole.PARENT -> {
                    val parent = repository.getParentById(username)
                    if (parent != null && parent.password == password) {
                        _loggedParent.value = parent
                        _currentUserRole.value = UserRole.PARENT
                        onSuccess()
                    } else {
                        onError("Identifiants Parent invalides.")
                    }
                }
                UserRole.NONE -> onError("Veuillez sélectionner un espace.")
            }
        }
    }

    fun logout() {
        _currentUserRole.value = UserRole.NONE
        _loggedTeacher.value = null
        _loggedParent.value = null
        _activeClassId.value = null
        _tempAttendanceMap.value = emptyMap()
        _tempObservationMap.value = emptyMap()
    }

    // ----------------------------------------------------
    // Attendance Tracking (Teacher Space)
    // ----------------------------------------------------
    fun updateTempStatus(studentMatricule: String, status: String) {
        val current = _tempAttendanceMap.value.toMutableMap()
        current[studentMatricule] = status
        _tempAttendanceMap.value = current
    }

    fun updateTempObservation(studentMatricule: String, observation: String) {
        val current = _tempObservationMap.value.toMutableMap()
        current[studentMatricule] = observation
        _tempObservationMap.value = current
    }

    fun validateAttendance(onComplete: () -> Unit) {
        val classId = _activeClassId.value ?: return
        val date = _activeDate.value
        val attendanceMap = _tempAttendanceMap.value
        val observationMap = _tempObservationMap.value

        viewModelScope.launch {
            val studentList = repository.getStudentsByClass(classId).first()
            val recordsToSave = studentList.map { student ->
                val status = attendanceMap[student.matricule] ?: "PRESENT"
                val observation = observationMap[student.matricule]

                // Create attendance object
                Attendance(
                    studentMatricule = student.matricule,
                    classId = classId,
                    date = date,
                    status = status,
                    observation = observation
                )
            }

            // Save in database
            repository.saveAttendanceList(recordsToSave)
            _toastMessage.emit("Appel validé avec succès pour le $date !")

            // Push notifications to parents in real time
            recordsToSave.forEach { record ->
                val student = studentList.find { it.matricule == record.studentMatricule } ?: return@forEach
                
                // Find parent associated with this student
                val associatedParents = repository.allParents.first()
                val parent = associatedParents.find { it.studentMatricule == student.matricule }
                
                if (parent != null) {
                    val statusLabel = when (record.status) {
                        "PRESENT" -> "Présent(e)"
                        "ABSENT" -> "Absent(e)"
                        "LATE" -> "en Retard"
                        else -> record.status
                    }
                    
                    val timeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                    
                    val title = when (record.status) {
                        "PRESENT" -> "Présence confirmée ✅"
                        "ABSENT" -> "Absence signalée ⚠️"
                        "LATE" -> "Retard signalé ⏳"
                        else -> "Notification Présence"
                    }

                    val message = "L'appel a été effectué à $timeString. Votre enfant ${student.name} est marqué $statusLabel aujourd'hui."
                    
                    repository.addNotification(
                        Notification(
                            recipientId = parent.id,
                            title = title,
                            message = message
                        )
                    )
                }
            }
            onComplete()
        }
    }

    // ----------------------------------------------------
    // Parent Space
    // ----------------------------------------------------
    fun submitExcuse(reason: String, date: String) {
        val student = parentStudent.value ?: return
        viewModelScope.launch {
            val excuse = Excuse(
                studentMatricule = student.matricule,
                date = date,
                reason = reason,
                status = "PENDING"
            )
            repository.submitExcuse(excuse)
            _toastMessage.emit("Justificatif d'absence soumis pour examen.")
        }
    }

    // ----------------------------------------------------
    // Administration Space (CRUD & Approvals)
    // ----------------------------------------------------
    fun approveExcuse(excuse: Excuse) {
        viewModelScope.launch {
            repository.updateExcuseStatus(excuse.id, "APPROVED")
            
            // Automatically mark corresponding student attendance record on that day as EXCUSED or PRESENT
            val existingAttendanceList = repository.getAttendanceForStudent(excuse.studentMatricule).first()
            val targetAttendance = existingAttendanceList.find { it.date == excuse.date }
            if (targetAttendance != null) {
                // Remove or modify attendance record
                repository.saveAttendance(
                    targetAttendance.copy(status = "PRESENT", observation = "Justifié : ${excuse.reason}")
                )
            }
            
            // Notify parent
            val associatedParents = repository.allParents.first()
            val parent = associatedParents.find { it.studentMatricule == excuse.studentMatricule }
            if (parent != null) {
                repository.addNotification(
                    Notification(
                        recipientId = parent.id,
                        title = "Justificatif Accepté ✔️",
                        message = "L'absence de votre enfant pour la date du ${excuse.date} a été justifiée avec succès."
                    )
                )
            }
            _toastMessage.emit("Justificatif d'absence approuvé.")
        }
    }

    fun rejectExcuse(excuse: Excuse) {
        viewModelScope.launch {
            repository.updateExcuseStatus(excuse.id, "REJECTED")
            
            // Notify parent
            val associatedParents = repository.allParents.first()
            val parent = associatedParents.find { it.studentMatricule == excuse.studentMatricule }
            if (parent != null) {
                repository.addNotification(
                    Notification(
                        recipientId = parent.id,
                        title = "Justificatif Refusé ❌",
                        message = "Le justificatif d'absence pour la date du ${excuse.date} a été refusé par l'administration."
                    )
                )
            }
            _toastMessage.emit("Justificatif d'absence rejeté.")
        }
    }

    // Class CRUD
    fun addClass(id: String, name: String) {
        viewModelScope.launch {
            repository.insertClass(SchoolClass(id, name))
            _toastMessage.emit("Classe '$name' ajoutée avec succès.")
        }
    }

    fun removeClass(schoolClass: SchoolClass) {
        viewModelScope.launch {
            repository.deleteClass(schoolClass)
            _toastMessage.emit("Classe '${schoolClass.name}' supprimée.")
        }
    }

    // Student CRUD
    fun addStudent(matricule: String, name: String, classId: String) {
        viewModelScope.launch {
            val randomSeed = "avatar_${(1..12).random()}"
            repository.insertStudent(Student(matricule, name, classId, randomSeed))
            _toastMessage.emit("Élève '$name' ajouté avec succès.")
        }
    }

    fun removeStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            _toastMessage.emit("Élève '${student.name}' supprimé.")
        }
    }

    // Teacher CRUD
    fun addTeacher(id: String, name: String, classId: String?) {
        viewModelScope.launch {
            repository.insertTeacher(Teacher(id, name, "password", classId))
            _toastMessage.emit("Enseignant '$name' ajouté avec succès.")
        }
    }

    fun removeTeacher(teacher: Teacher) {
        viewModelScope.launch {
            repository.deleteTeacher(teacher)
            _toastMessage.emit("Enseignant '${teacher.name}' supprimé.")
        }
    }

    // Class selection change helper
    fun setActiveClass(classId: String?) {
        _activeClassId.value = classId
    }

    fun setActiveDate(date: String) {
        _activeDate.value = date
    }
}

// Factory to inject repository
class SchoolViewModelFactory(private val repository: SchoolRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SchoolViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SchoolViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
