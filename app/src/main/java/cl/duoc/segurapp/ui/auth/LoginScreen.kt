package cl.duoc.segurapp.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.segurapp.ui.BotonPrimario
import cl.duoc.segurapp.ui.BotonSecundario
import cl.duoc.segurapp.ui.CampoTexto
import cl.duoc.segurapp.ui.EtiquetaCampo
import cl.duoc.segurapp.ui.OpcionPastilla
import cl.duoc.segurapp.ui.theme.Fondo
import cl.duoc.segurapp.ui.theme.Gris
import cl.duoc.segurapp.ui.theme.Navy
import cl.duoc.segurapp.ui.theme.TextoSec

private val PERFILES = listOf("Operador", "Residente", "Supervisor", "Administrador")

@Composable
fun LoginScreen(auth: AuthViewModel, onRegistro: () -> Unit, onOk: () -> Unit) {
    var perfil by remember { mutableIntStateOf(0) }
    var correo by remember { mutableStateOf("operador@demo.cl") }
    var clave by remember { mutableStateOf("demo1234") }

    LaunchedEffect(Unit) { auth.limpiarError() }

    Column(
        Modifier
            .fillMaxSize()
            .background(Fondo)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        Icon(Icons.Outlined.VerifiedUser, contentDescription = null, tint = Navy, modifier = Modifier.size(72.dp))
        Text("SegurApp", color = Navy, fontSize = 36.sp, fontWeight = FontWeight.Bold)
        Text(
            "Gestión y seguimiento de situaciones de seguridad comunal · San Joaquín",
            color = TextoSec, fontSize = 15.sp, textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
        )

        Column(Modifier.fillMaxWidth()) {
            EtiquetaCampo("Perfil")
            PERFILES.chunked(2).forEachIndexed { fila, par ->
                Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    par.forEachIndexed { col, nombre ->
                        val i = fila * 2 + col
                        OpcionPastilla(nombre, perfil == i, {
                            perfil = i
                            // Comodidad para la demo: rellena el correo del perfil elegido.
                            if (correo.endsWith("@demo.cl")) correo = "${nombre.lowercase()}@demo.cl"
                        }, Modifier.weight(1f))
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            CampoTexto("Correo", correo, { correo = it }, teclado = KeyboardType.Email)
            Spacer(Modifier.height(14.dp))
            CampoTexto("Contraseña", clave, { clave = it }, clave = true)
        }

        auth.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp, modifier = Modifier.padding(top = 12.dp))
        }

        Spacer(Modifier.height(20.dp))
        if (auth.cargando) {
            CircularProgressIndicator(color = Navy)
        } else {
            BotonPrimario("Ingresar", { auth.login(correo, clave, PERFILES[perfil], onOk) }, Modifier.fillMaxWidth())
        }

        Row(
            Modifier.fillMaxWidth().padding(vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HorizontalDivider(Modifier.weight(1f), color = Gris)
            Text("¿Eres nuevo?", color = TextoSec, fontSize = 13.sp)
            HorizontalDivider(Modifier.weight(1f), color = Gris)
        }

        BotonSecundario(
            "Crear cuenta", onRegistro, Modifier.fillMaxWidth(),
            icono = { Icon(Icons.Outlined.PersonAdd, contentDescription = null) }
        )

        Spacer(Modifier.height(28.dp))
        Text("MVP académico · Todos los datos son ficticios", color = TextoSec, fontSize = 12.sp)
    }
}
