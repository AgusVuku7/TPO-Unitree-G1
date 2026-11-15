package com.uade.vigia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.uade.vigia.data.SimulationRepositoryImpl
import com.uade.vigia.data.local.VigiaDatabase
import com.uade.vigia.ui.VigiaViewModel
import com.uade.vigia.ui.components.AlertsScreen
import com.uade.vigia.ui.components.ConnectionScreen
import com.uade.vigia.ui.components.DashboardScreen
import com.uade.vigia.ui.components.HistoryScreen
import com.uade.vigia.ui.theme.VigiaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inicialización de Room Database
        val database = VigiaDatabase.getDatabase(applicationContext)

        // 2. Inicialización del Repositorio de Simulación
        val repository = SimulationRepositoryImpl(lifecycleScope)

        // 3. Creación del ViewModel con Factory manual
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(VigiaViewModel::class.java)) {
                    return VigiaViewModel(repository, database.missionDao()) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
        val viewModel = ViewModelProvider(this, factory)[VigiaViewModel::class.java]

        // 4. Renderizado Compose con NavHost
        setContent {
            VigiaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "connection"
                    ) {
                        composable("connection") {
                            ConnectionScreen(
                                viewModel = viewModel,
                                onConnectClicked = {
                                    navController.navigate("main") {
                                        popUpTo("connection") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                        composable("main") {
                            MainScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

enum class NavigationTab(val label: String) {
    PANEL("Panel"),
    ALERTAS("Alertas"),
    HISTORIAL("Historial")
}

@Composable
fun MainScreen(viewModel: VigiaViewModel) {
    var selectedTab by remember { mutableStateOf(NavigationTab.PANEL) }
    val uiState by viewModel.uiState.collectAsState()
    val alertCount = uiState.telemetry.activeAlerts.size

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == NavigationTab.PANEL,
                    onClick = { selectedTab = NavigationTab.PANEL },
                    label = { Text(NavigationTab.PANEL.label) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Panel") }
                )
                NavigationBarItem(
                    selected = selectedTab == NavigationTab.ALERTAS,
                    onClick = { selectedTab = NavigationTab.ALERTAS },
                    label = { Text(NavigationTab.ALERTAS.label) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (alertCount > 0) {
                                    Badge { Text(alertCount.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Alertas")
                        }
                    }
                )
                NavigationBarItem(
                    selected = selectedTab == NavigationTab.HISTORIAL,
                    onClick = { selectedTab = NavigationTab.HISTORIAL },
                    label = { Text(NavigationTab.HISTORIAL.label) },
                    icon = { Icon(Icons.Default.History, contentDescription = "Historial") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                NavigationTab.PANEL -> DashboardScreen(viewModel = viewModel)
                NavigationTab.ALERTAS -> AlertsScreen(viewModel = viewModel)
                NavigationTab.HISTORIAL -> HistoryScreen(viewModel = viewModel)
            }
        }
    }
}
