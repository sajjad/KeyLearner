package com.example.keylearner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.keylearner.ui.navigation.AppNavigation
import com.example.keylearner.ui.theme.KeyLearnerTheme

/**
 * Main activity for the KeyLearner app
 *
 * This activity hosts the Compose navigation graph and manages the app's lifecycle.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KeyLearnerTheme {
                AppNavigation()
            }
        }
    }
}
