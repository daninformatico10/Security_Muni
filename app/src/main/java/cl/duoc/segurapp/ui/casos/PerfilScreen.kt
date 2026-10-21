package cl.duoc.segurapp.ui.casos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.segurapp.data.Usuario
import cl.duoc.segurapp.ui.AppHeader
import cl.duoc.segurapp.ui.BotonSecundario
import cl.duoc.segurapp.ui.theme.Fondo
import cl.duoc.segurapp.ui.theme.Texto
import cl.duoc.segurapp.ui.theme.TextoSec

@Composable
fun PerfilScreen(usuario: Usuario?, onLogout: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Fondo)) {
        AppHeader("Perfil", "Datos de tu cuenta")
        Column(Modifier.padding(24.dp)) {
            Text(usuario?.nombre ?: "", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Texto)
            Text(usuario?.correo ?: "", fontSize = 16.sp, color = TextoSec)
            Spacer(Modifier.height(16.dp))
            Text("Perfil: ${usuario?.rol?.replaceFirstChar { it.uppercase() } ?: ""}", fontSize = 17.sp, color = Texto)
            usuario?.sector?.let { Text("Sector: $it (referencial)", fontSize = 17.sp, color = Texto) }
            Spacer(Modifier.height(28.dp))
            BotonSecundario("Cerrar sesión", onLogout, Modifier.fillMaxWidth())
        }
    }
}
