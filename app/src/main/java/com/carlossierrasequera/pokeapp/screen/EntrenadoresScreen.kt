package com.carlossierrasequera.pokeapp.screen


import androidx.compose.animation.animateContentSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carlossierrasequera.pokeapp.data.Entrenador
import com.carlossierrasequera.pokeapp.viewmodel.EntrenadoresViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrenadoresScreen(
    navigateToPokemon: () -> Unit,
    navigateToBatallas: () -> Unit,
    navigateToLogin: () -> Unit
) {
    val viewModel: EntrenadoresViewModel = viewModel()
    val entrenadoresList by viewModel.entrenadoresList.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedEntrenador by remember { mutableStateOf<Pair<String, Entrenador>?>(null) }

    var nombre by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.cargarEntrenadores()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entrenadores", fontSize = 22.sp) },
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
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        viewModel.eliminarEntrenador(id)
                    })
                }
            }
        }
    }

    if (showDialog) {
        MenuDialogEntrenadores(
            onDismiss = { showDialog = false },
            onNavigateToPokemon = { showDialog = false; navigateToPokemon() },
            onNavigateToBatallas = { showDialog = false; navigateToBatallas() },
            onSignOut = {
                showDialog = false
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
                    viewModel.agregarEntrenador(nombre, edad.toInt(), region)
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
                    viewModel.editarEntrenador(id, nombre, edad.toInt(), region)
                }
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }
}

@Composable
fun MenuDialogEntrenadores(
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF90CAF9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver Pokémons", color = Color.Black)
                }
                Button(
                    onClick = { onDismiss(); onNavigateToBatallas() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA5D6A7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver Batallas", color = Color.Black)
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


@Composable
fun EntrenadorItem(entrenador: com.carlossierrasequera.pokeapp.data.Entrenador, onEdit: () -> Unit, onDelete: () -> Unit) {
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


