package com.carlossierrasequera.pokeapp.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.carlossierrasequera.pokeapp.data.AuthManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrenadoresScreen(auth: AuthManager, navigateToPokemon: () -> Unit, navigateToBatallas: () -> Unit, navigateToLogin: () -> Unit) {
    val entrenadoresList = remember { mutableStateListOf<Entrenador>() }
    val db = Firebase.firestore
    var showDialog by remember { mutableStateOf(false) } // Controla el AlertDialog

    LaunchedEffect(Unit) {
        db.collection("entrenadores").get().addOnSuccessListener { result ->
            entrenadoresList.clear()
            for (document in result) {
                entrenadoresList.add(
                    Entrenador(
                        document.getString("nombre") ?: "Desconocido",
                        document.getLong("edad")?.toInt() ?: 0,
                        document.getString("region") ?: "Desconocida"
                    )
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entrenadores", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menú")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(entrenadoresList) { entrenador ->
                    EntrenadorItem(entrenador)
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Menú") },
            text = { Text("Selecciona una opción") },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            showDialog = false
                            navigateToPokemon()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF90CAF9)), // Azul Claro
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ver Pokémons", color = Color.Black)
                    }
                    Button(
                        onClick = {
                            showDialog = false
                            navigateToBatallas()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA5D6A7)), // Verde Suave
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ver Batallas", color = Color.Black)
                    }
                    Button(
                        onClick = {
                            showDialog = false
                            auth.signOut()
                            navigateToLogin()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF9A9A)), // Rojo Suave
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar Sesión", color = Color.Black)
                    }
                    Spacer(modifier = Modifier.height(8.dp)) // Espaciado entre botones
                    Button(
                        onClick = { showDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBDBDBD)), // Gris Suave
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancelar", color = Color.Black)
                    }
                }
            }
        )
    }
}

@Composable
fun EntrenadorItem(entrenador: Entrenador) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = entrenador.nombre,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF37474F)
            )
            Text(
                text = "Edad: ${entrenador.edad} | Región: ${entrenador.region}",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}

data class Entrenador(val nombre: String, val edad: Int, val region: String)
