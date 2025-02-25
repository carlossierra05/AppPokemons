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
fun EntrenadoresScreen(auth: AuthManager, navigateToPokemon: () -> Unit, navigateToBatallas: () -> Unit, navigateToLogin: () -> Unit) {
    val entrenadoresList = remember { mutableStateListOf<Pair<String, Entrenador>>() }
    val db = Firebase.firestore
    var showDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedEntrenador by remember { mutableStateOf<Pair<String, Entrenador>?>(null) }

    var nombre by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        db.collection("entrenadores").get().addOnSuccessListener { result ->
            entrenadoresList.clear()
            for (document in result) {
                entrenadoresList.add(
                    document.id to Entrenador(
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
            Button(
                onClick = {
                    nombre = ""
                    edad = ""
                    region = ""
                    showAddDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)),
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Añadir Entrenador")
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(entrenadoresList) { (id, entrenador) ->
                    EntrenadorItem(entrenador, onEdit = {
                        selectedEntrenador = id to entrenador
                        nombre = entrenador.nombre
                        edad = entrenador.edad.toString()
                        region = entrenador.region
                        showEditDialog = true
                    }, onDelete = {
                        db.collection("entrenadores").document(id).delete().addOnSuccessListener {
                            entrenadoresList.removeIf { it.first == id }
                        }
                    })
                }
            }
        }
    }

    if (showDialog) {
        MenuDialog(
            onDismiss = { showDialog = false },
            onNavigateToPokemon = { showDialog = false; navigateToPokemon() },
            onNavigateToBatallas = { showDialog = false; navigateToBatallas() }, // CORRECTO
            onSignOut = {
                showDialog = false
                auth.signOut()
                navigateToLogin()
            }
        )

    }

    if (showAddDialog) {
        EntrenadorDialog(
            title = "Añadir Entrenador",
            nombre = nombre,
            edad = edad,
            region = region,
            onNombreChange = { nombre = it },
            onEdadChange = { edad = it.filter { it.isDigit() } },
            onRegionChange = { region = it },
            onConfirm = {
                if (nombre.isNotBlank() && edad.isNotBlank() && region.isNotBlank()) {
                    val nuevoEntrenador = Entrenador(nombre, edad.toInt(), region)
                    db.collection("entrenadores").add(
                        hashMapOf(
                            "nombre" to nuevoEntrenador.nombre,
                            "edad" to nuevoEntrenador.edad,
                            "region" to nuevoEntrenador.region
                        )
                    ).addOnSuccessListener { document ->
                        entrenadoresList.add(document.id to nuevoEntrenador)
                    }
                }
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    if (showEditDialog) {
        EntrenadorDialog(
            title = "Editar Entrenador",
            nombre = nombre,
            edad = edad,
            region = region,
            onNombreChange = { nombre = it },
            onEdadChange = { edad = it.filter { it.isDigit() } },
            onRegionChange = { region = it },
            onConfirm = {
                selectedEntrenador?.let { (id, _) ->
                    val updatedEntrenador = Entrenador(nombre, edad.toInt(), region)
                    db.collection("entrenadores").document(id).set(
                        hashMapOf(
                            "nombre" to updatedEntrenador.nombre,
                            "edad" to updatedEntrenador.edad,
                            "region" to updatedEntrenador.region
                        )
                    ).addOnSuccessListener {
                        entrenadoresList.replaceAll { if (it.first == id) id to updatedEntrenador else it }
                    }
                }
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }
}

@Composable
fun EntrenadorItem(entrenador: Entrenador, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(text = entrenador.nombre, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
            Text(text = "Edad: ${entrenador.edad} | Región: ${entrenador.region}", fontSize = 16.sp, color = Color.Gray)
            Row {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Blue) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red) }
            }
        }
    }
}

@Composable
fun MenuDialog(
    onDismiss: () -> Unit,
    onNavigateToPokemon: () -> Unit,
    onNavigateToBatallas: () -> Unit,
    onSignOut: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Menú") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onDismiss(); onNavigateToPokemon() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF90CAF9)), // Azul Claro
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver Pokémons", color = Color.Black)
                }
                Button(
                    onClick = { onDismiss(); onNavigateToBatallas() }, // Corregido
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA5D6A7)), // Verde Suave
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver Batallas", color = Color.Black) // Corregido
                }

                Button(
                    onClick = { onDismiss(); onSignOut() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF9A9A)), // Rojo Suave
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar Sesión", color = Color.Black)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBDBDBD)), // Gris Suave
            ) {
                Text("Cancelar", color = Color.Black)
            }
        }
    )
}



data class Entrenador(val nombre: String, val edad: Int, val region: String)

@Composable
fun EntrenadorDialog(
    title: String,
    nombre: String,
    edad: String,
    region: String,
    onNombreChange: (String) -> Unit,
    onEdadChange: (String) -> Unit,
    onRegionChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre, onValueChange = onNombreChange, label = { Text("Nombre") })
                OutlinedTextField(value = edad, onValueChange = onEdadChange, label = { Text("Edad") })
                OutlinedTextField(value = region, onValueChange = onRegionChange, label = { Text("Región") })
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Guardar") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}


