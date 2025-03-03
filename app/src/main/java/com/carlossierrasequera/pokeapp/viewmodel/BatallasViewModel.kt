package com.carlossierrasequera.pokeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.carlossierrasequera.pokeapp.data.Batalla

class BatallasViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _batallasList = MutableStateFlow<List<Pair<String, Batalla>>>(emptyList())
    val batallasList: StateFlow<List<Pair<String, Batalla>>> get() = _batallasList

    fun cargarBatallas() {
        viewModelScope.launch {
            db.collection("batallas").get().addOnSuccessListener { result ->
                val lista = mutableListOf<Pair<String, Batalla>>()
                for (document in result) {
                    lista.add(
                        document.id to Batalla(
                            document.getString("entrenador1") ?: "Desconocido",
                            document.getString("entrenador2") ?: "Desconocido",
                            document.getString("ganador") ?: "Indefinido"
                        )
                    )
                }
                _batallasList.value = lista
            }
        }
    }

    fun agregarBatalla(entrenador1: String, entrenador2: String, ganador: String) {
        val nuevaBatalla = Batalla(entrenador1, entrenador2, ganador)
        db.collection("batallas").add(
            hashMapOf(
                "entrenador1" to entrenador1,
                "entrenador2" to entrenador2,
                "ganador" to ganador
            )
        ).addOnSuccessListener { document ->
            _batallasList.value = _batallasList.value + (document.id to nuevaBatalla)
        }
    }

    fun editarBatalla(id: String, entrenador1: String, entrenador2: String, ganador: String) {
        val batallaActualizada = Batalla(entrenador1, entrenador2, ganador)
        db.collection("batallas").document(id).set(
            hashMapOf(
                "entrenador1" to entrenador1,
                "entrenador2" to entrenador2,
                "ganador" to ganador
            )
        ).addOnSuccessListener {
            _batallasList.value = _batallasList.value.map {
                if (it.first == id) id to batallaActualizada else it
            }
        }
    }

    fun eliminarBatalla(id: String) {
        db.collection("batallas").document(id).delete().addOnSuccessListener {
            _batallasList.value = _batallasList.value.filterNot { it.first == id }
        }
    }
}
