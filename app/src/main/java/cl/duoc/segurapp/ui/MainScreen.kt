package cl.duoc.segurapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cl.duoc.segurapp.ui.auth.AuthViewModel
import cl.duoc.segurapp.ui.casos.CasosScreen
import cl.duoc.segurapp.ui.casos.CasosViewModel
import cl.duoc.segurapp.ui.casos.NuevoCasoScreen
import cl.duoc.segurapp.ui.casos.PerfilScreen
import cl.duoc.segurapp.ui.theme.Navy

@Composable
fun MainScreen(
    auth: AuthViewModel,
    vm: CasosViewModel,
    onDetalle: (Int) -> Unit,
    onLogout: () -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val items = listOf(
        "Casos" to Icons.AutoMirrored.Filled.List,
        "Nuevo" to Icons.Outlined.AddCircleOutline,
        "Perfil" to Icons.Outlined.Person
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                items.forEachIndexed { i, (nombre, icono) ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i },
                        icon = { Icon(icono, contentDescription = nombre) },
                        label = { Text(nombre) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Navy,
                            selectedTextColor = Navy,
                            indicatorColor = Color(0xFFE8EDF2)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (tab) {
                0 -> CasosScreen(auth.usuario, vm, onDetalle)
                1 -> NuevoCasoScreen(vm, onGuardado = { tab = 0 })
                else -> PerfilScreen(auth.usuario, onLogout)
            }
        }
    }
}
