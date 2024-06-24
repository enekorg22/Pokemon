package com.example.pokemon.view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokemon.R
import com.example.pokemon.controller.PokemonController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var pokemonAdapter: PokemonAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inicializa el adaptador con el contexto y una lista vacía
        pokemonAdapter = PokemonAdapter(this, emptyList())
        recyclerView.adapter = pokemonAdapter

        fetchPokemonData()
    }

    private fun fetchPokemonData() {
        val controller = PokemonController()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pokemons = controller.getPokemons()
                withContext(Dispatchers.Main) {
                    if (pokemons.isNotEmpty()) {
                        pokemonAdapter.updateData(pokemons)
                    } else {
                        // Muestra un mensaje si no se reciben datos
                        Toast.makeText(this@MainActivity, "No se encontraron Pokémon", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error al cargar los datos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
