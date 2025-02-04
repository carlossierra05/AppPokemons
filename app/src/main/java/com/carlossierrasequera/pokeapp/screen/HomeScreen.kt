package com.carlossierrasequera.pokeapp.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.carlossierrasequera.pokeapp.data.AuthManager
import com.carlossierrasequera.pokeapp.data.PokemonDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL

@Composable
fun HomeScreen(auth: AuthManager, navigateToLogin: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedPokemon by remember { mutableStateOf<PokemonDetails?>(null) }
    var pokemonList by remember { mutableStateOf<List<Pokemon>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    // Cargar lista inicial de Pokémon
    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val response = URL("https://pokeapi.co/api/v2/pokemon?limit=10").readText()
            val results = JSONObject(response).getJSONArray("results")
            val pokemons = mutableListOf<Pokemon>()
            for (i in 0 until results.length()) {
                val obj = results.getJSONObject(i)
                val name = obj.getString("name")
                val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${i + 1}.png"
                pokemons.add(Pokemon(name, imageUrl))
            }
            pokemonList = pokemons
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar Pokémon") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Botón para cerrar sesión
        Button(
            onClick = { showDialog = true }, // Activa el diálogo de confirmación
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth().padding(8.dp).height(50.dp)
        ) {
            Text("Cerrar Sesión", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de Pokémon
        LazyColumn {
            items(pokemonList.filter { it.name.contains(searchQuery, ignoreCase = true) }) { pokemon ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable {
                            coroutineScope.launch(Dispatchers.IO) {
                                val details = getPokemonDetails(pokemon.name)
                                selectedPokemon = details
                            }
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = pokemon.imageUrl,
                            contentDescription = pokemon.name,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = pokemon.name.capitalize(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Detalles del Pokémon seleccionado
        if (selectedPokemon != null) {
            PokemonDetailsDialog(
                pokemon = selectedPokemon!!,
                onDismiss = { selectedPokemon = null }
            )
        }

        // Diálogo de confirmación de cierre de sesión
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Cerrar Sesión") },
                text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
                confirmButton = {
                    Button(
                        onClick = {
                            auth.signOut() // Cierra sesión
                            navigateToLogin() // Navega al login
                        }
                    ) {
                        Text("Sí")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

// Función para obtener los detalles de un Pokémon
suspend fun getPokemonDetails(name: String): PokemonDetails {
    val response = URL("https://pokeapi.co/api/v2/pokemon/$name").readText()
    val json = JSONObject(response)
    val height = json.getInt("height")
    val weight = json.getInt("weight")
    val typesArray = json.getJSONArray("types")
    val types = mutableListOf<String>()
    for (i in 0 until typesArray.length()) {
        types.add(typesArray.getJSONObject(i).getJSONObject("type").getString("name"))
    }
    return PokemonDetails(name, height, weight, types)
}

@Composable
fun PokemonDetailsDialog(pokemon: PokemonDetails, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(pokemon.name.capitalize()) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Altura: ${pokemon.height / 10.0} m", fontSize = 16.sp)
                Text("Peso: ${pokemon.weight / 10.0} kg", fontSize = 16.sp)
                Text("Tipos: ${pokemon.types.joinToString(", ")}", fontSize = 16.sp)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

// Modelos de datos
data class Pokemon(val name: String, val imageUrl: String)
