package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import com.example.myapplication.model.CasoSeguridad
import com.example.myapplication.model.CategoriaCaso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * Estado del formulario de registro de nuevos casos de seguridad.
 */
data class FormularioCasoUiState(
    val titulo: String = "",
    val descripcion: String = "",
    val categoriaSeleccionada: CategoriaCaso = CategoriaCaso.ROBO,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val fotoUri: String? = null,
    val mensajeError: String? = null,
    val registroExitoso: Boolean = false,
)

/**
 * CasosViewModel gestiona el estado centralizado de los reportes de seguridad
 * para la comuna de San Joaquín y la lógica de registro de nuevos casos.
 */
class CasosViewModel : ViewModel() {

    // Lista sintética inicial con 3 casos pre-cargados para pruebas de la interfaz
    private val casosIniciales = listOf(
        CasoSeguridad(
            id = "CASO-001",
            titulo = "Intento de robo en Av. Las Industrias",
            descripcion = "Individuo sospechoso intentó forzar la chapa de un vehículo estacionado frente al N° 4500.",
            categoria = CategoriaCaso.ROBO,
            latitud = -33.4831,
            longitud = -70.6272,
            fotoUri = "file:///evidencia_vehiculo.jpg",
        ),
        CasoSeguridad(
            id = "CASO-002",
            titulo = "Ruidos molestos y fiesta clandestina",
            descripcion = "Música a alto volumen y alteración del orden público en pasaje Los Olivos pasadas las 02:00 AM.",
            categoria = CategoriaCaso.RUIDOS_MOLESTOS,
            latitud = -33.4880,
            longitud = -70.6210,
            fotoUri = null,
        ),
        CasoSeguridad(
            id = "CASO-003",
            titulo = "Colisión leve entre dos vehículos",
            descripcion = "Choque por alcance en la intersección de Av. Santa Rosa con Salvador Allende. Sin heridos graves.",
            categoria = CategoriaCaso.ACCIDENTE,
            latitud = -33.4910,
            longitud = -70.6350,
            fotoUri = "file:///evidencia_choque.jpg",
        )
    )

    // Estado reactivo de la lista de casos
    private val _casos = MutableStateFlow(casosIniciales)
    val casos: StateFlow<List<CasoSeguridad>> = _casos.asStateFlow()

    // Estado reactivo del formulario de registro
    private val _formularioState = MutableStateFlow(FormularioCasoUiState())
    val formularioState: StateFlow<FormularioCasoUiState> = _formularioState.asStateFlow()

    /**
     * Agrega un nuevo [CasoSeguridad] a la lista centralizada de forma reactiva.
     */
    fun agregarCaso(nuevoCaso: CasoSeguridad) {
        _casos.update { listaActual ->
            listOf(nuevoCaso) + listaActual
        }
    }

    /**
     * Actualiza el título en el estado del formulario.
     */
    fun onTituloChanged(nuevoTitulo: String) {
        _formularioState.update { currentState ->
            currentState.copy(
                titulo = nuevoTitulo,
                mensajeError = null,
                registroExitoso = false,
            )
        }
    }

    /**
     * Actualiza la descripción en el estado del formulario.
     */
    fun onDescripcionChanged(nuevaDescripcion: String) {
        _formularioState.update { currentState ->
            currentState.copy(
                descripcion = nuevaDescripcion,
                mensajeError = null,
                registroExitoso = false,
            )
        }
    }

    /**
     * Actualiza la categoría seleccionada en el formulario.
     */
    fun onCategoriaSelected(nuevaCategoria: CategoriaCaso) {
        _formularioState.update { currentState ->
            currentState.copy(
                categoriaSeleccionada = nuevaCategoria,
                registroExitoso = false,
            )
        }
    }

    /**
     * Simula la captura de la ubicación GPS actual del dispositivo.
     */
    fun simularCapturaGPS() {
        // Coordenadas simuladas de la comuna de San Joaquín, Santiago
        val latitudSimulada = -33.4852
        val longitudSimulada = -70.6258

        _formularioState.update { currentState ->
            currentState.copy(
                latitud = latitudSimulada,
                longitud = longitudSimulada,
            )
        }
    }

    /**
     * Simula la captura de evidencia fotográfica desde la cámara.
     */
    fun simularCapturaCamara() {
        val uriSimulada = "file:///evidencia_${System.currentTimeMillis()}.jpg"

        _formularioState.update { currentState ->
            currentState.copy(
                fotoUri = uriSimulada,
            )
        }
    }

    /**
     * Valida los datos del formulario y registra un nuevo caso de seguridad.
     */
    fun guardarCasoDesdeFormulario(): Boolean {
        val state = _formularioState.value

        if (state.titulo.isBlank()) {
            _formularioState.update { it.copy(mensajeError = "El título del incidente es obligatorio") }
            return false
        }

        if (state.descripcion.isBlank()) {
            _formularioState.update { it.copy(mensajeError = "La descripción del incidente es obligatoria") }
            return false
        }

        val idNuevo = "CASO-" + UUID.randomUUID().toString().take(5).uppercase()

        val nuevoCaso = CasoSeguridad(
            id = idNuevo,
            titulo = state.titulo.trim(),
            descripcion = state.descripcion.trim(),
            categoria = state.categoriaSeleccionada,
            latitud = state.latitud,
            longitud = state.longitud,
            fotoUri = state.fotoUri
        )

        agregarCaso(nuevoCaso)

        // Reiniciar el formulario y marcar como exitoso
        _formularioState.value = FormularioCasoUiState(registroExitoso = true)
        return true
    }

    /**
     * Limpia los campos del formulario de registro.
     */

    fun cambiarEstadoCaso(id: String) {
        _casos.value = _casos.value.map { caso ->

            if (caso.id == id) {

                caso.copy(
                    estado = when (caso.estado) {
                        "Pendiente" -> "En evaluación"
                        "En evaluación" -> "Cerrado"
                        "Cerrado" -> "Pendiente"
                        else -> "Pendiente"
                    }
                )

            } else {
                caso
            }
        }
    }
    fun limpiarFormulario() {
        _formularioState.value = FormularioCasoUiState()
    }
}
