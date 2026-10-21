package cl.duoc.segurapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.duoc.segurapp.ui.AppNav
import cl.duoc.segurapp.ui.theme.SegurAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SegurAppTheme {
                AppNav()
            }
        }
    }
}
