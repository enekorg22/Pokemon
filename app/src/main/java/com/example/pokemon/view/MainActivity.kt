package com.example.pokemon.view

import android.os.Bundle
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
    private var isLoading = false
    private var currentOffset = 0
    private var noMorePokemon = false
    private val loadThreshold = 149

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
                val newPokemons = controller.getPokemonPage(currentOffset)
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

        if (newPokemons.isNotEmpty()) {
            pokemonAdapter.addPokemon(newPokemons)
            currentOffset += newPokemons.size
        } else {
            noMorePokemon = true // Establecer bandera si no hay más Pokémon
            Toast.makeText(this@MainActivity, "No se encontraron más Pokémon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        pokemonAdapter = PokemonAdapter(this, emptyList())
        recyclerView.adapter = pokemonAdapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                // Revisar si se debe cargar más Pokémon
                if (!isLoading && !noMorePokemon && totalItemCount <= lastVisibleItemPosition + loadThreshold) {
                    loadMorePokemon()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Verificar si estamos al final de la lista y no hay más Pokémon para cargar
                    if (!recyclerView.canScrollVertically(1) && noMorePokemon) {
                        Toast.makeText(this@MainActivity, "No se encontraron más Pokémon", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun loadMorePokemon() {
        if (isLoading || noMorePokemon) return // Si ya está cargando o no hay más Pokémon, no hacer nada
        isLoading = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val newPokemons = controller.getPokemonPage(currentOffset)
                withContext(Dispatchers.Main) {
                    if (newPokemons.isNotEmpty()) {
                        pokemonAdapter.addPokemon(newPokemons)
                        currentOffset += newPokemons.size
                        isLoading = false // Marcar como no cargando solo si se cargaron nuevos Pokémon
                    } else {
                        noMorePokemon = true // Establecer bandera si no hay más Pokémon
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error al cargar los datos", Toast.LENGTH_SHORT).show()
                }
            } finally {
                isLoading = false // Asegurarse de marcar como no cargando en cualquier caso
            }
        }
    }
}
