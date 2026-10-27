package com.example.myapplication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.LoginUiState
import com.example.myapplication.viewmodel.LoginViewModel

/**
 * Pantalla contenedora con estado para el inicio de sesión de SegurApp.
 * Se conecta con [LoginViewModel] para consumir y reaccionar al estado reactivo del formulario.
 */
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel()
) {
    // Suscripción al estado reactivo del ViewModel usando StateFlow
    val uiState by viewModel.uiState.collectAsState()

    LoginContent(
        modifier = modifier,
        uiState = uiState,
        onUsernameChanged = viewModel::onUsernameChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onLoginClick = viewModel::login,
        onResetClick = viewModel::resetState
    )
}

/**
 * Componente stateless de la interfaz de usuario de Login.
 * Facilita el desacoplamiento, pruebas unitarias y renderizado en vistas previas (@Preview).
 */
@Composable
fun LoginContent(
    uiState: LoginUiState,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClick: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Encabezado institucional de la aplicación
            HeaderSection()

            Spacer(modifier = Modifier.height(24.dp))

            // Tarjeta principal contenedora del formulario de inicio de sesión
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Iniciar Sesión",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Ingrese sus credenciales para acceder al sistema",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    // -------------------------------------------------------------
                    // Campo 1: Nombre de Usuario
                    // -------------------------------------------------------------
                    OutlinedTextField(
                        value = uiState.usernameInput,
                        onValueChange = onUsernameChanged,
                        label = { Text("Usuario") },
                        placeholder = { Text("Ej: admin") },
                        singleLine = true,
                        isError = uiState.usernameError != null,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Ícono de usuario",
                                tint = if (uiState.usernameError != null) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (uiState.usernameError != null) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Error en usuario",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Retroalimentación visual de error por campo
                    if (uiState.usernameError != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = uiState.usernameError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // -------------------------------------------------------------
                    // Campo 2: Contraseña
                    // -------------------------------------------------------------
                    OutlinedTextField(
                        value = uiState.passwordInput,
                        onValueChange = onPasswordChanged,
                        label = { Text("Contraseña") },
                        placeholder = { Text("Ej: 123") },
                        singleLine = true,
                        isError = uiState.passwordError != null,
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Ícono de contraseña",
                                tint = if (uiState.passwordError != null) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (uiState.passwordError != null) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Error en contraseña",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Retroalimentación visual de error por campo
                    if (uiState.passwordError != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = uiState.passwordError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Mensaje de error general de autenticación si aplica
                    if (uiState.generalError != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Alerta general",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = uiState.generalError,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // Mensaje de éxito si las credenciales son correctas
                    if (uiState.isLoginSuccessful) {
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Éxito",
                                    tint = Color(0xFF2E7D32)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "¡Inicio de sesión exitoso!",
                                        color = Color(0xFF1B5E20),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Bienvenido a SegurApp San Joaquín",
                                        color = Color(0xFF2E7D32),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    // Botón de acción principal para Iniciar Sesión
                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "INGRESAR",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    // Botón secundario para limpiar o reiniciar el estado
                    if (uiState.isLoginSuccessful || uiState.generalError != null || uiState.usernameError != null || uiState.passwordError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onResetClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text(text = "Limpiar Formulario")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pie de página institucional
            Text(
                text = "SegurApp v1.0 • Sistema Comunal de Seguridad\nMunicipalidad de San Joaquín",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Sección superior con la marca e identidad visual de la Municipalidad de San Joaquín.
 */
@Composable
private fun HeaderSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(72.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Escudo SegurApp",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "SegurApp",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Municipalidad de San Joaquín",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

/**
 * Vista previa para Android Studio con estado inicial por defecto.
 */
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MyApplicationTheme {
        LoginContent(
            uiState = LoginUiState(),
            onUsernameChanged = {},
            onPasswordChanged = {},
            onLoginClick = {},
            onResetClick = {}
        )
    }
}

/**
 * Vista previa para Android Studio con estado de error de validaciones.
 */
@Preview(showBackground = true, name = "Estado con Errores")
@Composable
fun LoginScreenErrorPreview() {
    MyApplicationTheme {
        LoginContent(
            uiState = LoginUiState(
                usernameInput = "",
                passwordInput = "",
                usernameError = "El nombre de usuario es obligatorio",
                passwordError = "La contraseña es obligatoria",
                generalError = null
            ),
            onUsernameChanged = {},
            onPasswordChanged = {},
            onLoginClick = {},
            onResetClick = {}
        )
    }
}

/**
 * Vista previa para Android Studio con estado de inicio de sesión exitoso.
 */
@Preview(showBackground = true, name = "Estado Exitoso")
@Composable
fun LoginScreenSuccessPreview() {
    MyApplicationTheme {
        LoginContent(
            uiState = LoginUiState(
                usernameInput = "admin",
                passwordInput = "123",
                isLoginSuccessful = true
            ),
            onUsernameChanged = {},
            onPasswordChanged = {},
            onLoginClick = {},
            onResetClick = {}
        )
    }
}
