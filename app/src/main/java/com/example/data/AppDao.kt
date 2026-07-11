package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Classes
    @Query("SELECT * FROM classes ORDER BY name ASC")
    fun getAllClasses(): Flow<List<SchoolClass>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(schoolClass: SchoolClass)

    @Delete
    suspend fun deleteClass(schoolClass: SchoolClass)

    // Students
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY name ASC")
    fun getStudentsByClass(classId: String): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE matricule = :matricule LIMIT 1")
    suspend fun getStudentByMatricule(matricule: String): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    // Teachers
    @Query("SELECT * FROM teachers ORDER BY name ASC")
    fun getAllTeachers(): Flow<List<Teacher>>

    @Query("SELECT * FROM teachers WHERE id = :id LIMIT 1")
    suspend fun getTeacherById(id: String): Teacher?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: Teacher)

    @Delete
    suspend fun deleteTeacher(teacher: Teacher)

    // Parents
    @Query("SELECT * FROM parents")
    fun getAllParents(): Flow<List<Parent>>

    @Query("SELECT * FROM parents WHERE id = :id LIMIT 1")
    suspend fun getParentById(id: String): Parent?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParent(parent: Parent)

    @Delete
    suspend fun deleteParent(parent: Parent)

    // Attendance
    @Query("SELECT * FROM attendance ORDER BY timestamp DESC")
    fun getAllAttendance(): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE studentMatricule = :studentMatricule ORDER BY date DESC")
    fun getAttendanceForStudent(studentMatricule: String): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE classId = :classId AND date = :date")
    fun getAttendanceForClassAndDate(classId: String, date: String): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceByDate(date: String): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceList(attendances: List<Attendance>)

    @Delete
    suspend fun deleteAttendance(attendance: Attendance)

    // Excuses
    @Query("SELECT * FROM excuses ORDER BY id DESC")
    fun getAllExcuses(): Flow<List<Excuse>>

    @Query("SELECT * FROM excuses WHERE studentMatricule = :studentMatricule ORDER BY date DESC")
    fun getExcusesForStudent(studentMatricule: String): Flow<List<Excuse>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExcuse(excuse: Excuse)

    @Query("UPDATE excuses SET status = :status WHERE id = :id")
    suspend fun updateExcuseStatus(id: Int, status: String)

    // Notifications
    @Query("SELECT * FROM notifications WHERE recipientId = :recipientId OR recipientId = 'all' ORDER BY timestamp DESC")
    fun getNotificationsForUser(recipientId: String): Flow<List<Notification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Int)
}
