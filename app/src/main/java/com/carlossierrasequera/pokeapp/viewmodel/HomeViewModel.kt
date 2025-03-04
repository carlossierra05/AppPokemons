package com.carlossierrasequera.pokeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlossierrasequera.pokeapp.data.Pokemon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import java.net.URL

class HomeViewModel : ViewModel() {
    private val _pokemonList = MutableStateFlow<List<Pokemon>>(emptyList())
    val pokemonList: StateFlow<List<Pokemon>> get() = _pokemonList

    init {
        fetchPokemon()
    }

    private fun fetchPokemon() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
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
                _pokemonList.value = pokemons
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
