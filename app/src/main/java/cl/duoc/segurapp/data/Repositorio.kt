package cl.duoc.segurapp.data

import android.content.Context
import android.util.Base64
import androidx.room.withTransaction
import cl.duoc.segurapp.data.local.AppDatabase
import cl.duoc.segurapp.data.local.CasoEntity
import cl.duoc.segurapp.data.local.EventoEntity
import cl.duoc.segurapp.data.local.UsuarioEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Error "esperado" con un mensaje que se muestra tal cual al usuario. */
class ErrorApp(mensaje: String) : Exception(mensaje)

/** Usuario que inició sesión (solo en memoria). */
object Session {
    @Volatile
    var usuario: Usuario? = null
}

/** Cifrado de contraseñas: PBKDF2 con sal aleatoria (nunca se guarda la clave en texto plano). */
private object Claves {
    private fun derivar(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, 10_000, 256)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }

    /** Devuelve (sal, hash) en Base64. */
    fun crear(password: String): Pair<String, String> {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        return Base64.encodeToString(salt, Base64.NO_WRAP) to
            Base64.encodeToString(derivar(password, salt), Base64.NO_WRAP)
    }

    fun verificar(password: String, saltB64: String, hashB64: String): Boolean {
        val salt = Base64.decode(saltB64, Base64.NO_WRAP)
        val esperado = Base64.decode(hashB64, Base64.NO_WRAP)
        return MessageDigest.isEqual(derivar(password, salt), esperado)
    }
}

private val FORMATO_BD = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
private fun ahora(): String = LocalDateTime.now().format(FORMATO_BD)
private fun String.capitalizar() = replaceFirstChar { it.uppercase() }

class Repositorio private constructor(private val db: AppDatabase) {
    private val usuarioDao = db.usuarios()
    private val casoDao = db.casos()
    private val mutex = Mutex()
    private var inicializado = false

    companion object {
        @Volatile
        private var instancia: Repositorio? = null

        fun get(context: Context): Repositorio = instancia ?: synchronized(this) {
            instancia ?: Repositorio(AppDatabase.get(context)).also { instancia = it }
        }
    }

    // ---------------- utilidades ----------------

    private suspend fun <T> operacion(bloque: suspend () -> T): Result<T> = withContext(Dispatchers.IO) {
        try {
            prepararDatos()
            Result.success(bloque())
        } catch (e: CancellationException) {
            throw e
        } catch (e: ErrorApp) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(Exception("Error en la base de datos local"))
        }
    }

    private fun usuarioActual(): Usuario =
        Session.usuario ?: throw ErrorApp("Sesión inválida, vuelve a ingresar")

    private fun soloPersonal(u: Usuario) {
        if (u.rol == "residente") throw ErrorApp("No tienes permiso para esta acción")
    }

    private fun UsuarioEntity.aUsuario() = Usuario(id, nombre, correo, rol, sector)

    private fun CasoEntity.aCaso(historial: List<Evento>? = null) = Caso(
        id = id,
        codigo = String.format(Locale.ROOT, "CASO-%04d", id),
        titulo = titulo ?: categoria,
        categoria = categoria,
        sector = sector,
        descripcion = descripcion,
        prioridad = prioridad,
        estado = estado,
        responsable = responsable,
        evidenciaSimulada = evidenciaSimulada,
        creadoEn = creadoEn,
        historial = historial
    )

    private suspend fun buscarCaso(id: Int, u: Usuario): CasoEntity {
        val c = casoDao.porId(id) ?: throw ErrorApp("Caso no encontrado")
        if (u.rol == "residente" && c.creadoPor != u.id) throw ErrorApp("Caso no encontrado")
        return c
    }

    private suspend fun detalle(id: Int, u: Usuario): Caso {
        val c = buscarCaso(id, u)
        val historial = casoDao.eventos(id).map { Evento(it.texto, it.creadoEn) }
        return c.aCaso(historial)
    }

    // ---------------- datos de ejemplo (primer uso) ----------------

    private suspend fun prepararDatos() = mutex.withLock {
        if (inicializado) return@withLock
        if (usuarioDao.contar() == 0) sembrar()
        inicializado = true
    }

    private suspend fun sembrar() {
        fun demo(nombre: String, correo: String, rol: String, sector: String): UsuarioEntity {
            val (salt, hash) = Claves.crear("demo1234")
            return UsuarioEntity(nombre = nombre, correo = correo, hash = hash, salt = salt, rol = rol, sector = sector)
        }
        usuarioDao.insertar(demo("Operador Demo", "operador@demo.cl", "operador", "Sector Norte"))
        val residente = usuarioDao.insertar(demo("Residente Demo", "residente@demo.cl", "residente", "Sector Norte")).toInt()
        usuarioDao.insertar(demo("Supervisor Demo", "supervisor@demo.cl", "supervisor", "Sector Centro"))
        usuarioDao.insertar(demo("Administrador Demo", "administrador@demo.cl", "administrador", "Sector Centro"))

        fun caso(
            id: Int, titulo: String, categoria: String, sector: String, descripcion: String,
            prioridad: String, estado: String, responsable: String, evidencia: Boolean, fecha: String
        ) = CasoEntity(
            id = id, titulo = titulo, categoria = categoria, sector = sector, descripcion = descripcion,
            prioridad = prioridad, estado = estado, responsable = responsable,
            evidenciaSimulada = evidencia, creadoPor = residente, creadoEn = fecha
        )
        casoDao.insertar(caso(9, "Escombros en vía pública", "Basura y escombros", "Sector Norte",
            "Escombros acumulados en la vereda (ejemplo).", "Media", "Seguimiento",
            "Unidad de Seguimiento (sim.)", false, "2026-10-10 12:15:00"))
        casoDao.insertar(caso(10, "Sitio eriazo sin cierre", "Sitio eriazo", "Sector Sur",
            "Sitio sin cierre perimetral (ejemplo).", "Baja", "Cerrado",
            "Unidad de Seguimiento (sim.)", false, "2026-10-12 21:45:00"))
        casoDao.insertar(caso(11, "Ruidos molestos", "Ruidos molestos", "Sector Centro",
            "Ruidos en horario nocturno (ejemplo).", "Media", "En revisión",
            "Operador de turno (sim.)", false, "2026-10-13 18:10:00"))
        casoDao.insertar(caso(12, "Alumbrado público deficiente", "Alumbrado público", "Sector Norte",
            "Luminarias apagadas en tramo de calle (ejemplo).", "Alta", "Derivado",
            "Unidad de Seguimiento (sim.)", true, "2026-10-14 09:30:00"))

        fun ev(c: Int, t: String, f: String) = EventoEntity(casoId = c, texto = t, creadoEn = f)
        casoDao.insertarEventos(
            listOf(
                ev(9, "Caso registrado", "2026-10-10 12:15:00"),
                ev(9, "Revisado por Operador", "2026-10-10 13:00:00"),
                ev(9, "Derivado a Unidad de Seguimiento", "2026-10-10 15:20:00"),
                ev(9, "Estado cambiado a Seguimiento", "2026-10-11 09:00:00"),
                ev(10, "Caso registrado", "2026-10-12 21:45:00"),
                ev(10, "Estado cambiado a Cerrado", "2026-10-13 10:30:00"),
                ev(11, "Caso registrado", "2026-10-13 18:10:00"),
                ev(11, "Revisado por Operador", "2026-10-13 18:40:00"),
                ev(12, "Caso registrado", "2026-10-14 09:30:00"),
                ev(12, "Revisado por Operador", "2026-10-14 09:50:00"),
                ev(12, "Derivado a Unidad de Seguimiento", "2026-10-14 11:05:00")
            )
        )
    }

    // ---------------- autenticación ----------------

    suspend fun login(correo: String, password: String, perfil: String): Result<Usuario> = operacion {
        val u = usuarioDao.porCorreo(correo.trim().lowercase())
        if (u == null || !Claves.verificar(password, u.salt, u.hash)) {
            throw ErrorApp("Correo o contraseña incorrectos")
        }
        if (u.rol != perfil) throw ErrorApp("Este usuario no tiene el perfil seleccionado")
        u.aUsuario()
    }

    suspend fun registrar(nombre: String, correo: String, password: String, sectorId: Int): Result<Usuario> = operacion {
        val sector = CATALOGOS.sectores.firstOrNull { it.id == sectorId }
            ?: throw ErrorApp("Sector no válido")
        val correoLimpio = correo.trim().lowercase()
        if (usuarioDao.porCorreo(correoLimpio) != null) throw ErrorApp("Ya existe una cuenta con ese correo")

        val (salt, hash) = Claves.crear(password)
        val entidad = UsuarioEntity(
            nombre = nombre.trim(), correo = correoLimpio, hash = hash, salt = salt,
            rol = "residente", sector = sector.nombre
        )
        val id = usuarioDao.insertar(entidad).toInt()
        entidad.copy(id = id).aUsuario()
    }

    // ---------------- casos ----------------

    suspend fun casos(): Result<List<Caso>> = operacion {
        val u = usuarioActual()
        val lista = if (u.rol == "residente") casoDao.deUsuario(u.id) else casoDao.todos()
        lista.map { it.aCaso() }
    }

    suspend fun caso(id: Int): Result<Caso> = operacion { detalle(id, usuarioActual()) }

    suspend fun crearCaso(req: NuevoCasoRequest): Result<Caso> = operacion {
        val u = usuarioActual()
        val categoria = CATALOGOS.categorias.firstOrNull { it.id == req.categoriaId }
            ?: throw ErrorApp("Categoría no válida")
        val sector = CATALOGOS.sectores.firstOrNull { it.id == req.sectorId }
            ?: throw ErrorApp("Sector no válido")
        if (req.descripcion.isBlank()) throw ErrorApp("Escribe una descripción breve")

        val fecha = ahora()
        val id = db.withTransaction {
            val nuevo = casoDao.insertar(
                CasoEntity(
                    titulo = null,
                    categoria = categoria.nombre,
                    sector = sector.nombre,
                    descripcion = req.descripcion.trim().take(500),
                    prioridad = req.prioridad,
                    estado = "Registrado",
                    responsable = "Sin asignar (sim.)",
                    evidenciaSimulada = req.evidenciaSimulada,
                    creadoPor = u.id,
                    creadoEn = fecha
                )
            ).toInt()
            casoDao.insertarEvento(EventoEntity(casoId = nuevo, texto = "Caso registrado", creadoEn = fecha))
            nuevo
        }
        detalle(id, u)
    }

    suspend fun agregarObservacion(id: Int, texto: String): Result<Caso> = operacion {
        val u = usuarioActual()
        val limpio = texto.trim()
        if (limpio.isEmpty()) throw ErrorApp("Escribe una observación")
        buscarCaso(id, u)
        casoDao.insertarEvento(EventoEntity(casoId = id, texto = "Observación: $limpio".take(300), creadoEn = ahora()))
        detalle(id, u)
    }

    suspend fun derivar(id: Int): Result<Caso> = operacion {
        val u = usuarioActual()
        soloPersonal(u)
        val c = buscarCaso(id, u)
        if (ESTADOS.indexOf(c.estado) >= ESTADOS.indexOf("Derivado")) throw ErrorApp("El caso ya fue derivado")
        db.withTransaction {
            casoDao.actualizar(c.copy(estado = "Derivado", responsable = "Unidad de Seguimiento (sim.)"))
            casoDao.insertarEvento(EventoEntity(casoId = id, texto = "Derivado a Unidad de Seguimiento", creadoEn = ahora()))
        }
        detalle(id, u)
    }

    suspend fun avanzar(id: Int): Result<Caso> = operacion {
        val u = usuarioActual()
        soloPersonal(u)
        val c = buscarCaso(id, u)
        val siguiente = ESTADOS.getOrNull(ESTADOS.indexOf(c.estado) + 1)
            ?: throw ErrorApp("El caso ya está cerrado")
        val texto = when (siguiente) {
            "En revisión" -> "Revisado por ${u.rol.capitalizar()}"
            "Derivado" -> "Derivado a Unidad de Seguimiento"
            else -> "Estado cambiado a $siguiente"
        }
        db.withTransaction {
            casoDao.actualizar(
                c.copy(
                    estado = siguiente,
                    responsable = if (siguiente == "Derivado") "Unidad de Seguimiento (sim.)" else c.responsable
                )
            )
            casoDao.insertarEvento(EventoEntity(casoId = id, texto = texto, creadoEn = ahora()))
        }
        detalle(id, u)
    }
}
