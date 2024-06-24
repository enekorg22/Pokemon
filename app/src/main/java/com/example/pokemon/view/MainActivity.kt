package com.example.pokemon.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokemon.R
import com.example.pokemon.controller.PokemonController
import com.example.pokemon.model.Pokemon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var pokemonAdapter: PokemonAdapter
    private val controller = PokemonController()
    private var isLoading = false
    private var currentOffset = 0
    private val loadThreshold = 99

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        // Cargar datos en segundo plano mientras se muestra la pantalla de portada
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val newPokemons = controller.getPokemonPage(currentOffset)
                withContext(Dispatchers.Main) {
                    setupMainActivity(newPokemons)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error al cargar los datos",
                        Toast.LENGTH_SHORT
                    ).show()
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
            Toast.makeText(
                this@MainActivity,
                "No se encontraron más Pokémon",
                Toast.LENGTH_SHORT
            ).show()
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

                if (!isLoading && totalItemCount <= lastVisibleItemPosition + loadThreshold) {
                    loadMorePokemon()
                }
            }
        })

        // Aquí puedes llamar loadMorePokemon() si quieres que se cargue automáticamente al abrir la aplicación
        // loadMorePokemon()
    }

    private fun loadMorePokemon() {
        if (isLoading) return
        isLoading = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val newPokemons = controller.getPokemonPage(currentOffset)
                withContext(Dispatchers.Main) {
                    if (newPokemons.isNotEmpty()) {
                        pokemonAdapter.addPokemon(newPokemons)
                        currentOffset += newPokemons.size
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "No se encontraron más Pokémon",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error al cargar los datos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } finally {
                isLoading = false
            }
        }
    }
}
