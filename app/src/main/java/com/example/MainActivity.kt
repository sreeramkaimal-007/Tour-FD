package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MonetizationHubScreen
import com.example.ui.screens.TourDetailScreen
import com.example.ui.screens.WelcomeIntroScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.TourViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: TourViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val screen by viewModel.currentScreen.collectAsState()
                    Crossfade(targetState = screen, label = "ScreenTransition") { currentScreen ->
                        when (currentScreen) {
                            is AppScreen.WelcomeIntro -> WelcomeIntroScreen(viewModel = viewModel)
                            is AppScreen.Auth -> AuthScreen(
                                isRegisterInitial = currentScreen.isRegisterMode,
                                viewModel = viewModel
                            )
                            is AppScreen.TourList -> HomeScreen(viewModel = viewModel)
                            is AppScreen.TourDetail -> TourDetailScreen(tourId = currentScreen.tourId, viewModel = viewModel)
                            is AppScreen.MonetizationHub -> MonetizationHubScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
