package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

class SchoolRepository(private val appDao: AppDao) {

    // Exposure of core flows
    val allClasses: Flow<List<SchoolClass>> = appDao.getAllClasses()
    val allStudents: Flow<List<Student>> = appDao.getAllStudents()
    val allTeachers: Flow<List<Teacher>> = appDao.getAllTeachers()
    val allParents: Flow<List<Parent>> = appDao.getAllParents()
    val allAttendance: Flow<List<Attendance>> = appDao.getAllAttendance()
    val allExcuses: Flow<List<Excuse>> = appDao.getAllExcuses()

    fun getStudentsByClass(classId: String): Flow<List<Student>> = appDao.getStudentsByClass(classId)
    fun getAttendanceForStudent(studentMatricule: String): Flow<List<Attendance>> = appDao.getAttendanceForStudent(studentMatricule)
    fun getAttendanceForClassAndDate(classId: String, date: String): Flow<List<Attendance>> = appDao.getAttendanceForClassAndDate(classId, date)
    fun getAttendanceByDate(date: String): Flow<List<Attendance>> = appDao.getAttendanceByDate(date)
    fun getExcusesForStudent(studentMatricule: String): Flow<List<Excuse>> = appDao.getExcusesForStudent(studentMatricule)
    fun getNotificationsForUser(recipientId: String): Flow<List<Notification>> = appDao.getNotificationsForUser(recipientId)

    // Suspend operations
    suspend fun getStudentByMatricule(matricule: String): Student? = appDao.getStudentByMatricule(matricule)
    suspend fun getTeacherById(id: String): Teacher? = appDao.getTeacherById(id)
    suspend fun getParentById(id: String): Parent? = appDao.getParentById(id)

    suspend fun insertClass(schoolClass: SchoolClass) = appDao.insertClass(schoolClass)
    suspend fun deleteClass(schoolClass: SchoolClass) = appDao.deleteClass(schoolClass)

    suspend fun insertStudent(student: Student) = appDao.insertStudent(student)
    suspend fun deleteStudent(student: Student) = appDao.deleteStudent(student)

    suspend fun insertTeacher(teacher: Teacher) = appDao.insertTeacher(teacher)
    suspend fun deleteTeacher(teacher: Teacher) = appDao.deleteTeacher(teacher)

    suspend fun insertParent(parent: Parent) = appDao.insertParent(parent)
    suspend fun deleteParent(parent: Parent) = appDao.deleteParent(parent)

    suspend fun saveAttendance(attendance: Attendance) = appDao.insertAttendance(attendance)
    suspend fun saveAttendanceList(attendances: List<Attendance>) = appDao.insertAttendanceList(attendances)
    suspend fun deleteAttendance(attendance: Attendance) = appDao.deleteAttendance(attendance)

    suspend fun submitExcuse(excuse: Excuse) = appDao.insertExcuse(excuse)
    suspend fun updateExcuseStatus(id: Int, status: String) = appDao.updateExcuseStatus(id, status)

    suspend fun addNotification(notification: Notification) = appDao.insertNotification(notification)
    suspend fun markNotificationAsRead(id: Int) = appDao.markNotificationAsRead(id)

    // Automatic Database Seeding
    suspend fun seedDatabaseIfNeeded() {
        // Only seed if classes are empty
        val existingClasses = appDao.getAllClasses().firstOrNull() ?: emptyList()
        if (existingClasses.isNotEmpty()) {
            Log.d("SchoolRepository", "Database already seeded. Skipping.")
            return
        }

        Log.d("SchoolRepository", "Seeding database with rich mockup school dataset...")

        // 1. Classes
        val classes = listOf(
            SchoolClass("6EME_A", "6ème A (Collège)"),
            SchoolClass("3EME_B", "3ème B (Collège)"),
            SchoolClass("TERM_C", "Terminale C (Lycée)")
        )
        classes.forEach { appDao.insertClass(it) }

        // 2. Teachers
        val teachers = listOf(
            Teacher("prof1", "M. Amadou Touré", "password", "6EME_A"),
            Teacher("prof2", "Mme. Sophie Dubois", "password", "3EME_B"),
            Teacher("prof3", "M. Koffi Soro", "password", "TERM_C")
        )
        teachers.forEach { appDao.insertTeacher(it) }

        // 3. Students
        val students = listOf(
            // 6EME_A
            Student("MAT001", "Amadou Diallo", "6EME_A", "avatar_1"),
            Student("MAT002", "Chantal Traoré", "6EME_A", "avatar_2"),
            Student("MAT003", "Jean Koffi", "6EME_A", "avatar_3"),
            Student("MAT004", "Fatoumata Sylla", "6EME_A", "avatar_4"),
            // 3EME_B
            Student("MAT005", "Vianney Ako", "3EME_B", "avatar_5"),
            Student("MAT006", "Mariam Koné", "3EME_B", "avatar_6"),
            Student("MAT007", "Lucas Bernard", "3EME_B", "avatar_7"),
            // TERM_C
            Student("MAT008", "Awa Ndiaye", "TERM_C", "avatar_8"),
            Student("MAT009", "Koffi Mensah", "TERM_C", "avatar_9")
        )
        students.forEach { appDao.insertStudent(it) }

        // 4. Parents
        val parents = listOf(
            Parent("parent1", "M. Ibrahima Diallo", "password", "MAT001"),
            Parent("parent2", "Mme. Alice Ako", "password", "MAT005"),
            Parent("parent3", "M. Charles Bernard", "password", "MAT007")
        )
        parents.forEach { appDao.insertParent(it) }

        // 5. Seed historical attendance to make charts and stats look beautiful immediately!
        // We will seed past 7 days (excluding weekends)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // Generate some realistic histories
        val historicalAttendances = mutableListOf<Attendance>()
        
        // Let's create history for the last 7 days
        for (i in 1..8) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            
            // Skip Sunday (1) and Saturday (7)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                continue
            }
            
            val dateStr = dateFormat.format(calendar.time)
            
            // For each student, seed record
            students.forEach { student ->
                // Generate realistic random distribution: 85% present, 10% absent, 5% late
                val rand = Math.random()
                val status = when {
                    rand < 0.85 -> "PRESENT"
                    rand < 0.95 -> "ABSENT"
                    else -> "LATE"
                }
                
                val observation = if (status == "ABSENT") {
                    null
                } else if (status == "LATE") {
                    "En retard de 10 minutes"
                } else {
                    // occasional observation
                    val obsRand = Math.random()
                    when {
                        obsRand < 0.05 -> "Très actif et attentif"
                        obsRand < 0.10 -> "Devoir non rendu"
                        obsRand < 0.15 -> "Excellent comportement"
                        else -> null
                    }
                }
                
                historicalAttendances.add(
                    Attendance(
                        studentMatricule = student.matricule,
                        classId = student.classId,
                        date = dateStr,
                        status = status,
                        observation = observation,
                        timestamp = calendar.timeInMillis
                    )
                )
            }
        }
        appDao.insertAttendanceList(historicalAttendances)

        // Seed some excuses
        val excuses = listOf(
            Excuse(
                studentMatricule = "MAT001",
                date = dateFormat.format(Date(System.currentTimeMillis() - 2 * 24 * 3600 * 1000)),
                reason = "Consultation médicale chez le pédiatre. Certificat fourni.",
                status = "APPROVED"
            ),
            Excuse(
                studentMatricule = "MAT005",
                date = dateFormat.format(Date(System.currentTimeMillis() - 3 * 24 * 3600 * 1000)),
                reason = "Indisposition familiale majeure.",
                status = "PENDING"
            )
        )
        excuses.forEach { appDao.insertExcuse(it) }

        // Seed some initial notifications
        val notifications = listOf(
            Notification(
                recipientId = "parent1",
                title = "Appel validé",
                message = "Amadou Diallo a été marqué Présent aujourd'hui.",
                timestamp = System.currentTimeMillis() - 1000 * 3600
            ),
            Notification(
                recipientId = "parent2",
                title = "Notification de Retard",
                message = "Vianney Ako a été marqué en Retard aujourd'hui.",
                timestamp = System.currentTimeMillis() - 1000 * 3600 * 4
            ),
            Notification(
                recipientId = "all",
                title = "Bienvenue sur Présence École",
                message = "La plateforme de suivi de présence en temps réel est maintenant opérationnelle !",
                timestamp = System.currentTimeMillis() - 1000 * 3600 * 24
            )
        )
        notifications.forEach { appDao.insertNotification(it) }

        Log.d("SchoolRepository", "Database seeded successfully.")
    }
}
