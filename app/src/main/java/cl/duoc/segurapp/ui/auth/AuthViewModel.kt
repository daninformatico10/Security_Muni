package cl.duoc.segurapp.ui.auth

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.segurapp.data.CATALOGOS
import cl.duoc.segurapp.data.Opcion
import cl.duoc.segurapp.data.Repositorio
import cl.duoc.segurapp.data.Session
import cl.duoc.segurapp.data.Usuario
import kotlinx.coroutines.launch

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repositorio.get(app)

    var usuario by mutableStateOf<Usuario?>(null)
        private set
    var cargando by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    val sectores: List<Opcion> = CATALOGOS.sectores

    fun limpiarError() { error = null }

    fun login(correo: String, password: String, perfil: String, onOk: () -> Unit) {
        if (correo.isBlank() || password.isBlank()) {
            error = "Ingresa tu correo y contraseña"
            return
        }
        viewModelScope.launch {
            cargando = true
            repo.login(correo, password, perfil.lowercase())
                .onSuccess {
                    Session.usuario = it
                    usuario = it
                    error = null
                    onOk()
                }
                .onFailure { error = it.message }
            cargando = false
        }
    }

    fun registrar(
        nombre: String, correo: String, password: String, confirmacion: String,
        sectorId: Int?, onOk: () -> Unit
    ) {
        when {
            nombre.isBlank() || correo.isBlank() || password.isBlank() -> error = "Completa todos los campos"
            sectorId == null -> error = "Selecciona un sector"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches() -> error = "Correo no válido"
            password.length < 8 -> error = "La contraseña debe tener al menos 8 caracteres"
            password != confirmacion -> error = "Las contraseñas no coinciden"
            else -> viewModelScope.launch {
                cargando = true
                repo.registrar(nombre, correo, password, sectorId)
                    .onSuccess {
                        Session.usuario = it
                        usuario = it
                        error = null
                        onOk()
                    }
                    .onFailure { error = it.message }
                cargando = false
            }
        }
    }

    fun cerrarSesion() {
        Session.usuario = null
        usuario = null
        error = null
    }
}
