package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.SchoolViewModel
import com.example.ui.SchoolViewModelFactory
import com.example.ui.UserRole
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ParentScreen
import com.example.ui.screens.TeacherScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Extract initialized database repository
        val repository = (application as SchoolApplication).repository

        // Instantiate single-source view model using custom Factory
        val viewModel: SchoolViewModel by viewModels {
            SchoolViewModelFactory(repository)
        }

        setContent {
            MyApplicationTheme {
                val userRole by viewModel.currentUserRole.collectAsState()

                // Reactive subscription to ViewModel Toast messages
                LaunchedEffect(Unit) {
                    viewModel.toastMessage.collect { msg ->
                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        Crossfade(targetState = userRole, label = "ScreenTransition") { role ->
                            when (role) {
                                UserRole.NONE -> LoginScreen(
                                    viewModel = viewModel,
                                    onLoginSuccess = {}
                                )
                                UserRole.TEACHER -> TeacherScreen(
                                    viewModel = viewModel,
                                    onLogout = { viewModel.logout() }
                                )
                                UserRole.PARENT -> ParentScreen(
                                    viewModel = viewModel,
                                    onLogout = { viewModel.logout() }
                                )
                                UserRole.ADMIN -> AdminScreen(
                                    viewModel = viewModel,
                                    onLogout = { viewModel.logout() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
