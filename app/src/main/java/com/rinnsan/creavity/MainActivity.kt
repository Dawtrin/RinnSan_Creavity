package com.rinnsan.creavity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rinnsan.creavity.core.theme.RinnSanCreavityTheme
import com.rinnsan.creavity.presentation.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RinnSanCreavityTheme {
                AppNavigation()
            }
        }
    }
}