package com.carlossierrasequera.pokeapp.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.carlossierrasequera.pokeapp.data.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(auth: AuthManager, navigateToLogin: () -> Unit, navigateToEntrenadores: () -> Unit, navigateToBatallas: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var pokemonList by remember { mutableStateOf<List<Pokemon>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val response = URL("https://pokeapi.co/api/v2/pokemon?limit=10").readText()
            val results = JSONObject(response).getJSONArray("results")
            val pokemons = mutableListOf<Pokemon>()
            for (i in 0 until results.length()) {
                val obj = results.getJSONObject(i)
                val name = obj.getString("name")
                val imageUrl =
                    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${i + 1}.png"
                pokemons.add(Pokemon(name, imageUrl))
            }
            pokemonList = pokemons
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PokeApp", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar Pokémon") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { SectionTitle("Pokémon") }
                items(pokemonList.filter { it.name.contains(searchQuery, ignoreCase = true) }) { pokemon ->
                    PokemonItem(pokemon)
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
                            navigateToEntrenadores()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF90CAF9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ver Entrenadores", color = Color.Black)
                    }
                    Button(
                        onClick = {
                            showDialog = false
                            navigateToBatallas()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA5D6A7)),
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF9A9A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar Sesión", color = Color.Black)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBDBDBD)),
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
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun PokemonItem(pokemon: Pokemon) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable {},
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = pokemon.imageUrl, contentDescription = pokemon.name, modifier = Modifier.size(80.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(pokemon.name.capitalize(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

data class Pokemon(val name: String, val imageUrl: String)