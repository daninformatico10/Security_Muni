package cl.duoc.segurapp.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios", indices = [Index(value = ["correo"], unique = true)])
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val correo: String,
    val hash: String,
    val salt: String,
    val rol: String,
    val sector: String?
)

@Entity(tableName = "casos")
data class CasoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String?,
    val categoria: String,
    val sector: String,
    val descripcion: String,
    val prioridad: String,
    val estado: String,
    val responsable: String,
    val evidenciaSimulada: Boolean,
    val creadoPor: Int?,
    val creadoEn: String
)

@Entity(
    tableName = "eventos",
    foreignKeys = [
        ForeignKey(
            entity = CasoEntity::class,
            parentColumns = ["id"],
            childColumns = ["casoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("casoId")]
)
data class EventoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val casoId: Int,
    val texto: String,
    val creadoEn: String
)
