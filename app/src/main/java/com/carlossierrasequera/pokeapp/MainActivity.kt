package com.carlossierrasequera.pokeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.carlossierrasequera.pokeapp.data.AuthManager
import com.carlossierrasequera.pokeapp.navegacion.Navegacion

class MainActivity : ComponentActivity() {
    val auth = AuthManager(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Firebase.analytics
        setContent {
            Navegacion(auth)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        auth.signOut()
    }
}
