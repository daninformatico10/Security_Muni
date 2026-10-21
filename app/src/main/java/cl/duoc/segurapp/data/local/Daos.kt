package cl.duoc.segurapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertar(usuario: UsuarioEntity): Long

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun porCorreo(correo: String): UsuarioEntity?

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int
}

@Dao
interface CasoDao {
    @Insert
    suspend fun insertar(caso: CasoEntity): Long

    @Update
    suspend fun actualizar(caso: CasoEntity)

    @Query("SELECT * FROM casos ORDER BY creadoEn DESC, id DESC")
    suspend fun todos(): List<CasoEntity>

    @Query("SELECT * FROM casos WHERE creadoPor = :usuarioId ORDER BY creadoEn DESC, id DESC")
    suspend fun deUsuario(usuarioId: Int): List<CasoEntity>

    @Query("SELECT * FROM casos WHERE id = :id LIMIT 1")
    suspend fun porId(id: Int): CasoEntity?

    @Insert
    suspend fun insertarEvento(evento: EventoEntity)

    @Insert
    suspend fun insertarEventos(eventos: List<EventoEntity>)

    @Query("SELECT * FROM eventos WHERE casoId = :casoId ORDER BY creadoEn DESC, id DESC")
    suspend fun eventos(casoId: Int): List<EventoEntity>
}
