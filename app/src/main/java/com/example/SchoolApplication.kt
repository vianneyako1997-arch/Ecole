package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.SchoolRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SchoolApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { SchoolRepository(database.appDao()) }

    private val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        // Seed database with beautiful mockup data if it's empty
        applicationScope.launch {
            repository.seedDatabaseIfNeeded()
        }
    }
}
