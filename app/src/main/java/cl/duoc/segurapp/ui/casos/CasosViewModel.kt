package cl.duoc.segurapp.ui.casos

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.segurapp.data.CATALOGOS
import cl.duoc.segurapp.data.Caso
import cl.duoc.segurapp.data.NuevoCasoRequest
import cl.duoc.segurapp.data.Repositorio
import kotlinx.coroutines.launch

class CasosViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repositorio.get(app)

    var casos by mutableStateOf<List<Caso>>(emptyList())
        private set
    var detalle by mutableStateOf<Caso?>(null)
        private set
    var cargando by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    val catalogos = CATALOGOS

    fun limpiarError() { error = null }

    fun limpiar() {
        casos = emptyList()
        detalle = null
        error = null
    }

    fun cargarCasos() {
        viewModelScope.launch {
            cargando = true
            repo.casos()
                .onSuccess { casos = it; error = null }
                .onFailure { error = it.message }
            cargando = false
        }
    }

    fun cargarDetalle(id: Int) {
        detalle = null
        viewModelScope.launch {
            repo.caso(id)
                .onSuccess { detalle = it; error = null }
                .onFailure { error = it.message }
        }
    }

    fun crearCaso(req: NuevoCasoRequest, onOk: () -> Unit) {
        if (req.descripcion.isBlank()) {
            error = "Escribe una descripción breve"
            return
        }
        viewModelScope.launch {
            cargando = true
            repo.crearCaso(req)
                .onSuccess { error = null; onOk() }
                .onFailure { error = it.message }
            cargando = false
        }
    }

    fun agregarObservacion(id: Int, texto: String, onOk: () -> Unit) =
        accion(onOk) { repo.agregarObservacion(id, texto) }

    fun derivar(id: Int) = accion { repo.derivar(id) }

    fun avanzar(id: Int) = accion { repo.avanzar(id) }

    private fun accion(onOk: () -> Unit = {}, llamada: suspend () -> Result<Caso>) {
        viewModelScope.launch {
            llamada()
                .onSuccess { detalle = it; error = null; onOk() }
                .onFailure { error = it.message }
        }
    }
}
