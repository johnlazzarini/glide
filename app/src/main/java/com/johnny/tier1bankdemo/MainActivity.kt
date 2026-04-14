package com.johnny.tier1bankdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.johnny.tier1bankdemo.core.navigation.AppNavHost
import com.johnny.tier1bankdemo.core.theme.Tier1BankTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Tier1BankTheme {
                val navController = rememberNavController()
                AppNavHost(navController = navController)
            }
        }
    }
}
