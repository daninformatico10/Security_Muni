package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.myapplication.data.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Estado inmutable de la interfaz de usuario para la pantalla de inicio de sesión de SegurApp.
 *
 * @property usernameInput Texto ingresado en el campo de usuario.
 * @property passwordInput Texto ingresado en el campo de contraseña.
 * @property usernameError Mensaje de error específico para el campo de usuario.
 * @property passwordError Mensaje de error específico para el campo de contraseña.
 * @property generalError Mensaje de error global cuando la autenticación falla.
 * @property isLoginSuccessful Indica si el inicio de sesión se completó con éxito.
 */
data class LoginUiState(
    val usernameInput: String = "",
    val passwordInput: String = "",
    val usernameError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isLoginSuccessful: Boolean = false,
)

/**
 * LoginViewModel gestiona el estado y la lógica de negocio de la pantalla de inicio de sesión
 * para la aplicación "SegurApp" de la Municipalidad de San Joaquín.
 *
 * Sigue el patrón MVVM, manteniendo la lógica de validación e inicio de sesión totalmente
 * desacoplada de la vista (Jetpack Compose UI).
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    // Persistencia de la sesión en almacenamiento local
    private val sessionManager = SessionManager(application)

    // Estado reactivo privado para modificación interna.
    // Si existe una sesión guardada, la app inicia directamente en el panel.
    private val _uiState = MutableStateFlow(
        LoginUiState(isLoginSuccessful = sessionManager.isLoggedIn())
    )
    // Estado reactivo público e inmutable consumido por la UI mediante StateFlow
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Credenciales predeterminadas para acceso a SegurApp
    private val defaultUsername = "admin"
    private val defaultPassword = "123"

    /**
     * Actualiza el valor del nombre de usuario ingresado y limpia errores previos.
     */
    fun onUsernameChanged(newUsername: String) {
        _uiState.update { currentState ->
            currentState.copy(
                usernameInput = newUsername,
                usernameError = null,
                generalError = null,
                isLoginSuccessful = false,
            )
        }
    }

    /**
     * Actualiza el valor de la contraseña ingresada y limpia errores previos.
     */
    fun onPasswordChanged(newPassword: String) {
        _uiState.update { currentState ->
            currentState.copy(
                passwordInput = newPassword,
                passwordError = null,
                generalError = null,
                isLoginSuccessful = false,
            )
        }
    }

    /**
     * Ejecuta la lógica centralizada de validación e inicio de sesión.
     *
     * Valida:
     * 1. Que los campos no estén vacíos.
     * 2. Que las credenciales coincidan con el usuario predeterminado ("admin" / "123").
     */
    fun login() {
        val currentUsername = _uiState.value.usernameInput.trim()
        val currentPassword = _uiState.value.passwordInput.trim()

        var hasValidationError = false
        var usernameErr: String? = null
        var passwordErr: String? = null

        // Validar campos vacíos
        if (currentUsername.isEmpty()) {
            usernameErr = "El nombre de usuario es obligatorio"
            hasValidationError = true
        }

        if (currentPassword.isEmpty()) {
            passwordErr = "La contraseña es obligatoria"
            hasValidationError = true
        }

        // Si existen errores de validación de campos vacíos, se emiten los errores específicos
        if (hasValidationError) {
            _uiState.update { currentState ->
                currentState.copy(
                    usernameError = usernameErr,
                    passwordError = passwordErr,
                    generalError = null,
                    isLoginSuccessful = false,
                )
            }
            return
        }

        // Validar credenciales contra el usuario predeterminado
        if ((currentUsername == defaultUsername) && (currentPassword == defaultPassword)) {
            // Persistir la sesión para que se mantenga al cerrar y reabrir la app
            sessionManager.saveSession(currentUsername)
            _uiState.update { currentState ->
                currentState.copy(
                    usernameError = null,
                    passwordError = null,
                    generalError = null,
                    isLoginSuccessful = true,
                )
            }
        } else {
            // Emite estado de error por credenciales incorrectas
            _uiState.update { currentState ->
                currentState.copy(
                    usernameError = if (currentUsername != defaultUsername) "Usuario no válido" else null,
                    passwordError = if (currentPassword != defaultPassword) "Contraseña no válida" else null,
                    generalError = "Credenciales incorrectas. Verifique los datos e intente nuevamente.",
                    isLoginSuccessful = false,
                )
            }
        }
    }

    /**
     * Cierra la sesión: elimina la sesión persistida y vuelve a la pantalla de login
     * con el formulario limpio.
     */
    fun logout() {
        sessionManager.clearSession()
        _uiState.value = LoginUiState()
    }

    /**
     * Reinicia el estado del formulario de login.
     */
    fun resetState() {
        _uiState.value = LoginUiState()
    }
}
