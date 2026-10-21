package cl.duoc.segurapp.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.segurapp.ui.AppHeader
import cl.duoc.segurapp.ui.BotonPrimario
import cl.duoc.segurapp.ui.CampoTexto
import cl.duoc.segurapp.ui.Selector
import cl.duoc.segurapp.ui.theme.Fondo
import cl.duoc.segurapp.ui.theme.Navy
import cl.duoc.segurapp.ui.theme.TextoSec

@Composable
fun RegisterScreen(auth: AuthViewModel, onBack: () -> Unit, onOk: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }
    var sector by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        auth.limpiarError()
    }

    Column(Modifier.fillMaxSize().background(Fondo)) {
        AppHeader("Crear cuenta", "Registro de nuevo usuario (datos ficticios)", onBack)
        Column(
            Modifier
                .weight(1f)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            CampoTexto("Nombre completo", nombre, { nombre = it }, placeholder = "María González")
            Spacer(Modifier.height(14.dp))
            CampoTexto("Correo", correo, { correo = it }, placeholder = "nombre@correo.cl", teclado = KeyboardType.Email)
            Spacer(Modifier.height(14.dp))
            Selector("Sector referencial", auth.sectores.map { it.nombre }, sector, { sector = it })
            Spacer(Modifier.height(14.dp))
            CampoTexto("Contraseña", clave, { clave = it }, clave = true)
            Text("Mínimo 8 caracteres.", color = TextoSec, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
            Spacer(Modifier.height(14.dp))
            CampoTexto("Confirmar contraseña", confirmacion, { confirmacion = it }, clave = true)

            auth.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp, modifier = Modifier.padding(top = 12.dp))
            }

            Spacer(Modifier.height(24.dp))
            if (auth.cargando) {
                CircularProgressIndicator(color = Navy, modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                BotonPrimario(
                    "Crear cuenta",
                    { auth.registrar(nombre, correo, clave, confirmacion, auth.sectores.getOrNull(sector)?.id, onOk) },
                    Modifier.fillMaxWidth()
                )
            }

            Text(
                text = androidx.compose.ui.text.buildAnnotatedString {
                    append("¿Ya tienes cuenta?  ")
                    pushStyle(
                        androidx.compose.ui.text.SpanStyle(
                            color = Navy, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline
                        )
                    )
                    append("Ingresar")
                    pop()
                },
                color = TextoSec,
                fontSize = 15.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 18.dp)
                    .clickable(onClick = onBack)
            )
        }
    }
}
