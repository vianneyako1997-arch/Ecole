package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "classes")
data class SchoolClass(
    @PrimaryKey val id: String,
    val name: String
)

@Entity(tableName = "students")
data class Student(
    @PrimaryKey val matricule: String,
    val name: String,
    val classId: String,
    val avatarSeed: String = "avatar_1"
)

@Entity(tableName = "teachers")
data class Teacher(
    @PrimaryKey val id: String,
    val name: String,
    val password: String,
    val classId: String? = null
)

@Entity(tableName = "parents")
data class Parent(
    @PrimaryKey val id: String,
    val name: String,
    val password: String,
    val studentMatricule: String
)

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentMatricule: String,
    val classId: String,
    val date: String, // Format: YYYY-MM-DD
    val status: String, // "PRESENT", "ABSENT", "LATE"
    val observation: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "excuses")
data class Excuse(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentMatricule: String,
    val date: String, // Format: YYYY-MM-DD
    val reason: String,
    val status: String = "PENDING" // "PENDING", "APPROVED", "REJECTED"
)

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recipientId: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
