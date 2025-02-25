package com.carlossierrasequera.pokeapp.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
fun BatallasScreen(auth: AuthManager, navigateToPokemon: () -> Unit, navigateToEntrenadores: () -> Unit, navigateToLogin: () -> Unit) {
    val batallasList = remember { mutableStateListOf<Pair<String, Batalla>>() }
    val db = Firebase.firestore
    var showDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedBatalla by remember { mutableStateOf<Pair<String, Batalla>?>(null) }

    var entrenador1 by remember { mutableStateOf("") }
    var entrenador2 by remember { mutableStateOf("") }
    var ganador by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        db.collection("batallas").get().addOnSuccessListener { result ->
            batallasList.clear()
            for (document in result) {
                batallasList.add(
                    document.id to Batalla(
                        document.getString("entrenador1") ?: "Desconocido",
                        document.getString("entrenador2") ?: "Desconocido",
                        document.getString("ganador") ?: "Indefinido"
                    )
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Batallas", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
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
            Button(
                onClick = {
                    entrenador1 = ""
                    entrenador2 = ""
                    ganador = ""
                    showAddDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)),
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Añadir Batalla")
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(batallasList) { (id, batalla) ->
                    BatallaItem(batalla, onEdit = {
                        selectedBatalla = id to batalla
                        entrenador1 = batalla.entrenador1
                        entrenador2 = batalla.entrenador2
                        ganador = batalla.resultado
                        showEditDialog = true
                    }, onDelete = {
                        db.collection("batallas").document(id).delete().addOnSuccessListener {
                            batallasList.removeIf { it.first == id }
                        }
                    })
                }
            }
        }
    }

    if (showDialog) {
        MenuDialogBatallas(
            onDismiss = { showDialog = false },
            onNavigateToPokemon = { showDialog = false; navigateToPokemon() },
            onNavigateToEntrenadores = { showDialog = false; navigateToEntrenadores() },
            onSignOut = {
                showDialog = false
                auth.signOut()
                navigateToLogin()
            }
        )
    }

    if (showAddDialog) {
        BatallaDialog(
            title = "Añadir Batalla",
            entrenador1 = entrenador1,
            entrenador2 = entrenador2,
            resultado = ganador,
            onEntrenador1Change = { entrenador1 = it },
            onEntrenador2Change = { entrenador2 = it },
            onGanadorChange = { ganador = it },
            onConfirm = {
                if (entrenador1.isNotBlank() && entrenador2.isNotBlank() && ganador.isNotBlank()) {
                    val nuevaBatalla = Batalla(entrenador1, entrenador2, ganador)
                    db.collection("batallas").add(
                        hashMapOf(
                            "entrenador1" to nuevaBatalla.entrenador1,
                            "entrenador2" to nuevaBatalla.entrenador2,
                            "ganador" to nuevaBatalla.resultado
                        )
                    ).addOnSuccessListener { document ->
                        batallasList.add(document.id to nuevaBatalla)
                    }
                }
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    if (showEditDialog) {
        BatallaDialog(
            title = "Editar Batalla",
            entrenador1 = entrenador1,
            entrenador2 = entrenador2,
            resultado = ganador,
            onEntrenador1Change = { entrenador1 = it },
            onEntrenador2Change = { entrenador2 = it },
            onGanadorChange = { ganador = it },
            onConfirm = {
                selectedBatalla?.let { (id, _) ->
                    val updatedBatalla = Batalla(entrenador1, entrenador2, ganador)
                    db.collection("batallas").document(id).set(
                        hashMapOf(
                            "entrenador1" to updatedBatalla.entrenador1,
                            "entrenador2" to updatedBatalla.entrenador2,
                            "ganador" to updatedBatalla.resultado
                        )
                    ).addOnSuccessListener {
                        batallasList.replaceAll { if (it.first == id) id to updatedBatalla else it }
                    }
                }
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }
}


@Composable
fun BatallaItem(batalla: Batalla, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(text = "Entrenador 1: ${batalla.entrenador1}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "Entrenador 2: ${batalla.entrenador2}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "Ganador: ${batalla.resultado}", fontSize = 16.sp, color = Color.Gray)
            Row {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Blue) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red) }
            }
        }
    }
}

data class Batalla(val entrenador1: String, val entrenador2: String, val resultado: String)

@Composable
fun BatallaDialog(
    title: String,
    entrenador1: String,
    entrenador2: String,
    resultado: String,
    onEntrenador1Change: (String) -> Unit,
    onEntrenador2Change: (String) -> Unit,
    onGanadorChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = entrenador1, onValueChange = onEntrenador1Change, label = { Text("Entrenador 1") })
                OutlinedTextField(value = entrenador2, onValueChange = onEntrenador2Change, label = { Text("Entrenador 2") })
                OutlinedTextField(value = resultado, onValueChange = onGanadorChange, label = { Text("Ganador") })
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)) // Verde Claro
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBDBDBD)) // Gris Suave
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun MenuDialogBatallas(
    onDismiss: () -> Unit,
    onNavigateToPokemon: () -> Unit,
    onNavigateToEntrenadores: () -> Unit,
    onSignOut: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Menú") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onDismiss(); onNavigateToPokemon() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF90CAF9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver Pokémons", color = Color.Black)
                }
                Button(
                    onClick = { onDismiss(); onNavigateToEntrenadores() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA5D6A7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver Entrenadores", color = Color.Black)
                }
                Button(
                    onClick = { onDismiss(); onSignOut() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF9A9A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar Sesión", color = Color.Black)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBDBDBD)),
            ) {
                Text("Cancelar", color = Color.Black)
            }
        }
    )
}


