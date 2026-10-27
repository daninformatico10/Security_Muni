package com.example.myapplication.model

/**
 * Enumeración que define las categorías de incidentes de seguridad comunal.
 */
enum class CategoriaCaso(val tituloMostrar: String) {
    ROBO("Robo / Asalto"),
    PERRO_PERDIDO("Mascota Perdida"),
    RUIDOS_MOLESTOS("Ruidos Molestos"),
    ACCIDENTE("Accidente de Tránsito"),
    OTRO("Otro Incidente")
}

/**
 * Modelo de datos que representa un caso o reporte de seguridad comunal en San Joaquín.
 *
 * @property id Identificador único del caso.
 * @property titulo Título breve o asunto del incidente.
 * @property descripcion Explicación detallada de lo sucedido.
 * @property categoria Categoría o tipificación del incidente ([CategoriaCaso]).
 * @property latitud Coordenada de latitud GPS (opcional).
 * @property longitud Coordenada de longitud GPS (opcional).
 * @property fotoUri URI o ruta de la evidencia fotográfica (opcional).
 */
data class CasoSeguridad(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val categoria: CategoriaCaso,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val fotoUri: String? = null,
)
