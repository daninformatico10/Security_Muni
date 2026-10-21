package cl.duoc.segurapp.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.segurapp.ui.auth.AuthViewModel
import cl.duoc.segurapp.ui.auth.LoginScreen
import cl.duoc.segurapp.ui.auth.RegisterScreen
import cl.duoc.segurapp.ui.casos.CasosViewModel
import cl.duoc.segurapp.ui.casos.DetalleScreen

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val auth: AuthViewModel = viewModel()
    val casos: CasosViewModel = viewModel()

    val irAMain = {
        nav.navigate("main") { popUpTo("login") { inclusive = true } }
        Unit
    }

    NavHost(navController = nav, startDestination = "login") {
        composable("login") {
            LoginScreen(auth, onRegistro = { nav.navigate("registro") }, onOk = irAMain)
        }
        composable("registro") {
            RegisterScreen(auth, onBack = { nav.popBackStack() }, onOk = irAMain)
        }
        composable("main") {
            MainScreen(
                auth = auth,
                vm = casos,
                onDetalle = { id -> nav.navigate("detalle/$id") },
                onLogout = {
                    casos.limpiar()
                    auth.cerrarSesion()
                    nav.navigate("login") { popUpTo("main") { inclusive = true } }
                }
            )
        }
        composable("detalle/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) { entrada ->
            DetalleScreen(
                id = entrada.arguments?.getInt("id") ?: 0,
                usuario = auth.usuario,
                vm = casos,
                onBack = { nav.popBackStack() }
            )
        }
    }
}
