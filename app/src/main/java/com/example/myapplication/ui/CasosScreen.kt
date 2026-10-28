package com.example.myapplication.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.model.CasoSeguridad
import com.example.myapplication.model.CategoriaCaso
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.CasosViewModel
import com.example.myapplication.viewmodel.FormularioCasoUiState
import java.util.Locale

/**
 * Pantalla principal contenedora para la gestión de incidentes comunitarios de SegurApp.
 * Permite alternar entre el [DashboardCasos], el [FormularioRegistroCaso] y el mapa comunal interactivo [MapaCasos].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CasosScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {},
    viewModel: CasosViewModel = viewModel()
) {
    val casos by viewModel.casos.collectAsState()
    val formularioState by viewModel.formularioState.collectAsState()

    // Control de pestaña seleccionada: 0 -> Lista de Casos, 1 -> Registrar Incidente, 2 -> Mapa
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Control del diálogo de confirmación para cerrar sesión
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Desea cerrar su sesión en SegurApp?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.limpiarFormulario()
                        onLogout()
                    }
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Security Muni - San Joaquín",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Panel Comunal de Seguridad",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 0) {
                FloatingActionButton(
                    onClick = { selectedTabIndex = 1 },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Nuevo Caso"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Nuevo Caso", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Pestañas de navegación superior (Lista, Registrar, Mapa)
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Casos (${casos.size})") },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Registrar") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("Mapa") },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
                )
            }

            // Contenido de la pestaña activa
            when (selectedTabIndex) {
                0 -> DashboardCasos(casos = casos)
                1 -> FormularioRegistroCaso(
                    formState = formularioState,
                    onTituloChanged = viewModel::onTituloChanged,
                    onDescripcionChanged = viewModel::onDescripcionChanged,
                    onCategoriaSelected = viewModel::onCategoriaSelected,
                    onCapturarGpsClick = {
                        Log.d("SegurApp", "Boton GPS presionado")
                        viewModel.simularCapturaGPS()
                    },
                    onCapturarCamaraClick = {
                        Log.d("SegurApp", "Boton Camara presionado")
                        viewModel.simularCapturaCamara()
                    },
                    onGuardarCasoClick = {
                        if (viewModel.guardarCasoDesdeFormulario()) {
                            selectedTabIndex = 0
                        }
                    },
                    onLimpiarClick = viewModel::limpiarFormulario
                )
                2 -> MapaCasos(
                    casos = casos,
                    onCoordenadasSeleccionadas = { latLng ->
                        Log.d("SegurApp", "Coordenadas seleccionadas desde Mapa: $latLng")
                        // Aquí se pueden capturar las coordenadas para precargar el formulario
                    }
                )
            }
        }
    }
}

/**
 * 1. DashboardCasos: Muestra la lista de casos comunitarios utilizando un LazyColumn.
 */
@Composable
fun DashboardCasos(
    casos: List<CasoSeguridad>,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (casos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay casos registrados actualmente en San Joaquín.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Reportes Recientes en la Comuna",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(casos, key = { it.id }) { caso ->
                    TarjetaCasoItem(caso = caso)
                }

                item {
                    Spacer(modifier = Modifier.height(64.dp))
                }
            }
        }
    }
}

/**
 * Tarjeta individual para mostrar el detalle ordenado de un caso de seguridad.
 */
@Composable
fun TarjetaCasoItem(
    caso: CasoSeguridad,
    modifier: Modifier = Modifier
) {
    val iconoCategoria = obtenerIconoPorCategoria(caso.categoria)
    val colorBadge = obtenerColorPorCategoria(caso.categoria)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Fila superior: ID y Badge de Categoría con ícono asignado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = caso.id,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colorBadge.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = iconoCategoria,
                            contentDescription = null,
                            tint = colorBadge,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = caso.categoria.tituloMostrar,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorBadge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Título del caso
            Text(
                text = caso.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Descripción detallada
            Text(
                text = caso.descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Fila inferior con etiquetas de GPS y Foto si aplican
            if (caso.latitud != null || caso.fotoUri != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (caso.latitud != null && caso.longitud != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "GPS registrado",
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "GPS: ${String.format(Locale.getDefault(), "%.4f", caso.latitud)}, ${String.format(Locale.getDefault(), "%.4f", caso.longitud)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    if (caso.fotoUri != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Foto adjunta",
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Foto Adjunta",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2. FormularioRegistroCaso: Vista para registrar un nuevo incidente comunitario.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FormularioRegistroCaso(
    formState: FormularioCasoUiState,
    onTituloChanged: (String) -> Unit,
    onDescripcionChanged: (String) -> Unit,
    onCategoriaSelected: (CategoriaCaso) -> Unit,
    onCapturarGpsClick: () -> Unit,
    onCapturarCamaraClick: () -> Unit,
    onGuardarCasoClick: () -> Unit,
    onLimpiarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Registrar Nuevo Incidente",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Complete la información del incidente para alertar al equipo de seguridad de San Joaquín.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Campo: Título
                OutlinedTextField(
                    value = formState.titulo,
                    onValueChange = onTituloChanged,
                    label = { Text("Título del incidente") },
                    placeholder = { Text("Ej: Robo de accesorio de auto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Campo: Descripción
                OutlinedTextField(
                    value = formState.descripcion,
                    onValueChange = onDescripcionChanged,
                    label = { Text("Descripción detallada") },
                    placeholder = { Text("Describa lo sucedido, fecha, hora y detalles relevantes") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Selector de Categoría
                Text(
                    text = "Seleccione la categoría:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoriaCaso.entries.forEach { categoria ->
                        val estaSeleccionada = formState.categoriaSeleccionada == categoria
                        val colorCategoria = obtenerColorPorCategoria(categoria)

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (estaSeleccionada) colorCategoria else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (estaSeleccionada) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable { onCategoriaSelected(categoria) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = obtenerIconoPorCategoria(categoria),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = categoria.tituloMostrar,
                                    fontSize = 12.sp,
                                    fontWeight = if (estaSeleccionada) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Botones requeridos: Cámara y GPS
                Text(
                    text = "Evidencia y Geolocalización:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón 1: Capturar Evidencia (Cámara)
                    Button(
                        onClick = onCapturarCamaraClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Cámara",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Capturar Evidencia (Cámara)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Botón 2: Registrar Ubicación (GPS)
                    Button(
                        onClick = onCapturarGpsClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "GPS",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Registrar Ubicación (GPS)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Indicadores de datos capturados
                if (formState.latitud != null || formState.fotoUri != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (formState.latitud != null && formState.longitud != null) {
                            Text(
                                text = "📍 GPS registrado: Lat ${formState.latitud}, Lng ${formState.longitud}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (formState.fotoUri != null) {
                            Text(
                                text = "📷 Evidencia fotográfica adjuntada",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }

                // Mensaje de error si la validación falla
                if (formState.mensajeError != null) {
                    Text(
                        text = formState.mensajeError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Mensaje de éxito al registrar
                if (formState.registroExitoso) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Éxito",
                            tint = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "¡Caso registrado con éxito en San Joaquín!",
                            color = Color(0xFF2E7D32),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón principal de guardado del incidente
                Button(
                    onClick = onGuardarCasoClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "REGISTRAR INCIDENTE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                OutlinedButton(
                    onClick = onLimpiarClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Limpiar Campos")
                }
            }
        }
    }
}

/**
 * Función auxiliar para asignar íconos representativos según la categoría.
 */
private fun obtenerIconoPorCategoria(categoria: CategoriaCaso): ImageVector {
    return when (categoria) {
        CategoriaCaso.ROBO -> Icons.Default.Warning
        CategoriaCaso.PERRO_PERDIDO -> Icons.Default.Search
        CategoriaCaso.RUIDOS_MOLESTOS -> Icons.Default.Notifications
        CategoriaCaso.ACCIDENTE -> Icons.Default.LocationOn
        CategoriaCaso.OTRO -> Icons.Default.Info
    }
}

/**
 * Función auxiliar para asignar colores representativos según la categoría.
 */
private fun obtenerColorPorCategoria(categoria: CategoriaCaso): Color {
    return when (categoria) {
        CategoriaCaso.ROBO -> Color(0xFFD32F2F)            // Rojo alerta
        CategoriaCaso.PERRO_PERDIDO -> Color(0xFFE65100)   // Naranja
        CategoriaCaso.RUIDOS_MOLESTOS -> Color(0xFF7B1FA2) // Púrpura
        CategoriaCaso.ACCIDENTE -> Color(0xFFC2185B)       // Magenta
        CategoriaCaso.OTRO -> Color(0xFF1976D2)            // Azul informativo
    }
}

// -----------------------------------------------------------------------------
// Vistas Previas (@Preview)
// -----------------------------------------------------------------------------

@Preview(showBackground = true, name = "Dashboard Casos - Lista")
@Composable
fun DashboardCasosPreview() {
    MyApplicationTheme {
        DashboardCasos(
            casos = listOf(
                CasoSeguridad(
                    id = "CASO-001",
                    titulo = "Robo en vía pública",
                    descripcion = "Sustracción de pertenencias a transeúnte en Av. Las Industrias.",
                    categoria = CategoriaCaso.ROBO,
                    latitud = -33.4831,
                    longitud = -70.6272,
                    fotoUri = "foto.jpg"
                ),
                CasoSeguridad(
                    id = "CASO-002",
                    titulo = "Perro extraviado en plaza",
                    descripcion = "Mascota vista rondando sin collar cerca del parque central.",
                    categoria = CategoriaCaso.PERRO_PERDIDO
                )
            )
        )
    }
}

@Preview(showBackground = true, name = "Formulario Registro Caso")
@Composable
fun FormularioRegistroCasoPreview() {
    MyApplicationTheme {
        FormularioRegistroCaso(
            formState = FormularioCasoUiState(
                titulo = "Foco de ruidos molestos",
                descripcion = "Música a alto volumen durante la madrugada."
            ),
            onTituloChanged = {},
            onDescripcionChanged = {},
            onCategoriaSelected = {},
            onCapturarGpsClick = {},
            onCapturarCamaraClick = {},
            onGuardarCasoClick = {},
            onLimpiarClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Pantalla Completa Casos")
@Composable
fun CasosScreenPreview() {
    MyApplicationTheme {
        CasosScreen()
    }
}
