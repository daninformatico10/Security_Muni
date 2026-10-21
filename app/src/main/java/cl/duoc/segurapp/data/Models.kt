package cl.duoc.segurapp.data

data class Opcion(val id: Int, val nombre: String)
data class Catalogos(val categorias: List<Opcion>, val sectores: List<Opcion>)

data class Usuario(
    val id: Int,
    val nombre: String,
    val correo: String,
    val rol: String,
    val sector: String?
)

data class Evento(val texto: String, val creadoEn: String)

data class Caso(
    val id: Int,
    val codigo: String,
    val titulo: String,
    val categoria: String,
    val sector: String,
    val descripcion: String,
    val prioridad: String,
    val estado: String,
    val responsable: String,
    val evidenciaSimulada: Boolean,
    val creadoEn: String,
    val historial: List<Evento>? = null
)

data class NuevoCasoRequest(
    val categoriaId: Int,
    val sectorId: Int,
    val descripcion: String,
    val prioridad: String,
    val evidenciaSimulada: Boolean
)

val ESTADOS = listOf("Registrado", "En revisión", "Derivado", "Seguimiento", "Cerrado")

/** Listas fijas de la app (ya no vienen de un servidor). */
val CATALOGOS = Catalogos(
    categorias = listOf(
        "Alumbrado público", "Ruidos molestos", "Sitio eriazo",
        "Seguridad vial", "Basura y escombros", "Otro"
    ).mapIndexed { i, n -> Opcion(i + 1, n) },
    sectores = listOf(
        "Sector Norte", "Sector Centro", "Sector Sur", "Sector Oriente", "Sector Poniente"
    ).mapIndexed { i, n -> Opcion(i + 1, n) }
)
