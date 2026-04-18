package edu.nd.cnguyen8.hwapp.five

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import edu.nd.cnguyen8.hwapp.five.screens.game.GameScreen
import edu.nd.cnguyen8.hwapp.five.screens.home.HomeScreen
import edu.nd.cnguyen8.hwapp.five.screens.login.LoginScreen
import edu.nd.cnguyen8.hwapp.five.screens.profile.ProfileScreen
import edu.nd.cnguyen8.hwapp.five.ui.theme.HWStarterRepoTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HWStarterRepoTheme {
                val startScreen = if (FirebaseAuth.getInstance().currentUser != null) {
                    "home"
                } else {
                    "login"
                }

                var currentScreen by remember { mutableStateOf(startScreen) }

                when (currentScreen) {
                    "login" -> LoginScreen(
                        onLoginSuccess = { currentScreen = "home" }
                    )
                    "home" -> HomeScreen(
                        onProfileClick = { currentScreen = "profile" },
                        onGameClick = { currentScreen = "game" },
                        onLogout = { currentScreen = "login" }
                    )
                    "profile" -> ProfileScreen(
                        onBack = { currentScreen = "home" }
                    )
                    "game" -> GameScreen(
                        onBack = { currentScreen = "home" }
                    )
                }
            }
        }
    }
}