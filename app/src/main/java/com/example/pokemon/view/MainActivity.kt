package com.example.pokemon.view

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pokemon.R
import com.example.pokemon.controller.PokemonController
import com.example.pokemon.model.Pokemon
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var pokemonAdapter: PokemonAdapter
    private val controller = PokemonController()
    private var currentPage = 0
    private var noMorePokemon = false
    private val pageSize = 17 // Cambiado a 20 Pokémon por página

    private lateinit var buttonPrevious: Button
    private lateinit var buttonNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        val imageViewLoadingGif: ImageView = findViewById(R.id.imageViewLoadingGif)
        Glide.with(this)
            .asGif()
            .load(R.drawable.loading_gif)
            .into(imageViewLoadingGif)

        // Cargar datos en segundo plano mientras se muestra la pantalla de portada
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val newPokemons = controller.getPokemonPage(currentPage * pageSize, pageSize)
                withContext(Dispatchers.Main) {
                    delay(3000) // Mantener el delay de 3 segundos para mostrar la portada con el GIF
                    setupMainActivity(newPokemons)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error al cargar los datos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupMainActivity(newPokemons: List<Pokemon>) {
        setContentView(R.layout.activity_main)
        setupRecyclerView()

        buttonPrevious = findViewById(R.id.button_previous)
        buttonNext = findViewById(R.id.button_next)

        buttonPrevious.setOnClickListener {
            if (currentPage > 0) {
                loadPreviousPage()
            }
        }

        buttonNext.setOnClickListener {
            if (!noMorePokemon) {
                loadNextPage()
            }
        }

        // Mostrar la primera página de Pokémon
        if (newPokemons.isNotEmpty()) {
            pokemonAdapter.addPokemon(newPokemons)
            if (newPokemons.size < pageSize) {
                noMorePokemon = true // Establecer bandera si no hay más Pokémon en la siguiente página
            }
        } else {
            noMorePokemon = true // Establecer bandera si no hay más Pokémon
        }

        updateButtonStates()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        pokemonAdapter = PokemonAdapter(this, emptyList())
        recyclerView.adapter = pokemonAdapter
    }

    private fun loadNextPage() {
        currentPage++
        loadPokemonPage()
    }

    private fun loadPreviousPage() {
        if (currentPage > 0) {
            currentPage--
            loadPokemonPage()
        }

        noMorePokemon = false
    }

    private fun loadPokemonPage() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val newPokemons = controller.getPokemonPage(currentPage * pageSize, pageSize)
                withContext(Dispatchers.Main) {
                    pokemonAdapter.clearPokemon() // Limpiar la lista antes de agregar la nueva página
                    if (newPokemons.isNotEmpty()) {
                        pokemonAdapter.addPokemon(newPokemons)
                        if (newPokemons.size < pageSize) {
                            noMorePokemon = true // Establecer bandera si no hay más Pokémon en la siguiente página
                        } else {
                            noMorePokemon = false // Restablecer la bandera si hay más Pokémon por cargar
                        }
                    } else {
                        noMorePokemon = true // Establecer bandera si no hay más Pokémon
                        Toast.makeText(this@MainActivity, "No se encontraron más Pokémon", Toast.LENGTH_SHORT).show()
                    }
                    updateButtonStates()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error al cargar los datos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateButtonStates() {
        buttonPrevious.isEnabled = currentPage > 0
        buttonNext.isEnabled = !noMorePokemon
    }
}