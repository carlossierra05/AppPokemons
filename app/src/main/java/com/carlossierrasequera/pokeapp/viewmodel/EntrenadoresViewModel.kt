package com.carlossierrasequera.pokeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlossierrasequera.pokeapp.data.Entrenador
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class EntrenadoresViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _entrenadoresList = MutableStateFlow<List<Pair<String, Entrenador>>>(emptyList())
    val entrenadoresList: StateFlow<List<Pair<String, Entrenador>>> get() = _entrenadoresList

    fun cargarEntrenadores() {
        viewModelScope.launch {
            db.collection("entrenadores").get().addOnSuccessListener { result ->
                val lista = mutableListOf<Pair<String, Entrenador>>()
                for (document in result) {
                    lista.add(
                        document.id to Entrenador(
                            document.getString("nombre") ?: "Desconocido",
                            document.getLong("edad")?.toInt() ?: 0,
                            document.getString("region") ?: "Desconocida"
                        )
                    )
                }
                _entrenadoresList.value = lista
            }
        }
    }

    fun agregarEntrenador(nombre: String, edad: Int, region: String) {
        val nuevoEntrenador = Entrenador(nombre, edad, region)
        db.collection("entrenadores").add(
            hashMapOf(
                "nombre" to nombre,
                "edad" to edad,
                "region" to region
            )
        ).addOnSuccessListener { document ->
            _entrenadoresList.value = _entrenadoresList.value + (document.id to nuevoEntrenador)
        }
    }

    fun editarEntrenador(id: String, nombre: String, edad: Int, region: String) {
        val entrenadorActualizado = Entrenador(nombre, edad, region)
        db.collection("entrenadores").document(id).set(
            hashMapOf(
                "nombre" to nombre,
                "edad" to edad,
                "region" to region
            )
        ).addOnSuccessListener {
            _entrenadoresList.value = _entrenadoresList.value.map {
                if (it.first == id) id to entrenadorActualizado else it
            }
        }
    }

    fun eliminarEntrenador(id: String) {
        db.collection("entrenadores").document(id).delete().addOnSuccessListener {
            _entrenadoresList.value = _entrenadoresList.value.filterNot { it.first == id }
        }
    }
}
