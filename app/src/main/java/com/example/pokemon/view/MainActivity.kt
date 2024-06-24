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
    private val controller = PokemonController()
    private var isLoading = false // Para evitar múltiples cargas simultáneas
    private var currentOffset = 0 // Para rastrear la paginación
    private val loadThreshold = 99 // Umbral de carga anticipada

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inicializa el adaptador con el contexto y una lista vacía
        pokemonAdapter = PokemonAdapter(this, emptyList())
        recyclerView.adapter = pokemonAdapter

        // Configurar el listener para la paginación
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                // Cargar más Pokémon cuando el usuario está a 'loadThreshold' elementos del final
                if (!isLoading && totalItemCount <= lastVisibleItemPosition + loadThreshold) {
                    loadMorePokemon()
                }
            }
        })

        // Cargar la primera página de datos
        loadMorePokemon()
    }

    private fun loadMorePokemon() {
        if (isLoading) return
        isLoading = true

        // Mostrar el indicador de carga (puedes usar un ProgressBar o similar en tu layout)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val newPokemons = controller.getPokemonPage(currentOffset)
                withContext(Dispatchers.Main) {
                    if (newPokemons.isNotEmpty()) {
                        pokemonAdapter.addPokemon(newPokemons)
                        currentOffset += newPokemons.size
                    } else {
                        // Mostrar un mensaje si no se reciben más datos
                        Toast.makeText(this@MainActivity, "No se encontraron más Pokémon", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error al cargar los datos", Toast.LENGTH_SHORT).show()
                }
            } finally {
                isLoading = false

                // Ocultar el indicador de carga (puedes usar un ProgressBar o similar en tu layout)
            }
        }
    }
}
