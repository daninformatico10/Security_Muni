package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.CasosScreen
import com.example.myapplication.ui.LoginScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.LoginViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SegurAppApp()
            }
        }
    }
}

/**
 * Flujo principal de la aplicación SegurApp:
 * Si el usuario inicia sesión correctamente, navega al panel [CasosScreen].
 */
@Composable
fun SegurAppApp(
    loginViewModel: LoginViewModel = viewModel()
) {
    val loginState by loginViewModel.uiState.collectAsState()

    if (loginState.isLoginSuccessful) {
        CasosScreen()
    } else {
        LoginScreen(viewModel = loginViewModel)
    }
}
