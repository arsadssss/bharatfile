package com.bharatfile.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.bharatfile.app.navigation.BharatFileNavGraph
import com.bharatfile.app.theme.BharatFileTheme
import com.bharatfile.app.ui.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by homeViewModel.isDarkMode.collectAsState()

            BharatFileTheme(darkTheme = isDarkMode) {
                BharatFileNavGraph(homeViewModel = homeViewModel)
            }
        }
    }
}
