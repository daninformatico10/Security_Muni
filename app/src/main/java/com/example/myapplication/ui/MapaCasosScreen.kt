package com.example.myapplication.ui

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.CasoSeguridad
import com.example.myapplication.model.CategoriaCaso
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.Locale

/**
 * Coordenadas del centro geográfico de la Comuna de San Joaquín, Santiago, Chile.
 */
val COORDENADAS_SAN_JOAQUIN = LatLng(-33.4852, -70.6258)

/**
 * Componente visual interactivo con Google Maps para visualizar los casos de seguridad
 * en la Comuna de San Joaquín y seleccionar ubicaciones mediante un clic prolongado.
 *
 * @param casos Lista reactiva de casos comunitarios.
 * @param onCoordenadasSeleccionadas Callback ejecutado al mantener presionado sobre el mapa (onMapLongClick).
 */
@Composable
fun MapaCasos(
    casos: List<CasoSeguridad>,
    modifier: Modifier = Modifier,
    onCoordenadasSeleccionadas: (LatLng) -> Unit = {},
) {
    // Configuración de la cámara inicial centrada en San Joaquín
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(COORDENADAS_SAN_JOAQUIN, 14f)
    }

    // Estado local para guardar las coordenadas seleccionadas mediante clic largo
    var coordenadaSeleccionada by remember { mutableStateOf<LatLng?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        // Mapa interactivo de Google Maps
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = false,
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                compassEnabled = true,
            ),
            onMapLongClick = { latLng ->
                Log.d("SegurApp", "Coordenadas seleccionadas en el mapa: ${latLng.latitude}, ${latLng.longitude}")
                coordenadaSeleccionada = latLng
                onCoordenadasSeleccionadas(latLng)
            },
        ) {
            // Iteración sobre la lista reactiva de casos para dibujar marcadores en el mapa
            for (caso in casos) {
                val lat = caso.latitud
                val lng = caso.longitud

                if ((lat != null) && (lng != null)) {
                    val posicionCaso = LatLng(lat, lng)

                    Marker(
                        state = MarkerState(position = posicionCaso),
                        title = caso.titulo,
                        snippet = "Categoría: ${caso.categoria.tituloMostrar}",
                    )
                }
            }

            // Marcador temporal de la ubicación seleccionada mediante clic largo
            coordenadaSeleccionada?.let { latLng ->
                Marker(
                    state = MarkerState(position = latLng),
                    title = "Ubicación Seleccionada para Nuevo Incidente",
                    snippet = "Coordenadas capturadas: ${String.format(Locale.getDefault(), "%.4f", latLng.latitude)}, ${String.format(Locale.getDefault(), "%.4f", latLng.longitude)}",
                )
            }
        }

        // Banner flotante superior con instrucciones para el usuario
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Mapa San Joaquín",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(6.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Mapa Comunal de San Joaquín",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Mantén presionado el mapa para seleccionar coordenadas de un incidente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Indicador inferior cuando se capturan coordenadas
        coordenadaSeleccionada?.let { latLng ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Coordenadas seleccionadas: ${String.format(Locale.getDefault(), "%.4f", latLng.latitude)}, ${String.format(Locale.getDefault(), "%.4f", latLng.longitude)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MapaCasosPreview() {
    MyApplicationTheme {
        MapaCasos(
            casos = listOf(
                CasoSeguridad(
                    id = "CASO-001",
                    titulo = "Asalto en Av. Las Industrias",
                    descripcion = "Incidente reportado cerca del paradero.",
                    categoria = CategoriaCaso.ROBO,
                    latitud = -33.4831,
                    longitud = -70.6272,
                ),
            ),
        )
    }
}
