package com.example.pokemon.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
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
    private var isLoading = false // Variable para controlar el estado de carga
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
            if (!isLoading && currentPage > 0) {
                loadPreviousPage()
            }
        }

        buttonNext.setOnClickListener {
            if (!isLoading && !noMorePokemon) {
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

        val editTextSearch = findViewById<EditText>(R.id.editTextSearch)
        editTextSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchTerm = editTextSearch.text.toString().trim()
                if (searchTerm.isNotEmpty()) {
                    searchPokemon(searchTerm)
                }
                true
            } else {
                false
            }
        }

        var searchJob: Job? = null // Para manejar el trabajo de búsqueda

        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // No es necesario implementar nada aquí
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No es necesario implementar nada aquí
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchJob?.cancel() // Cancelar la búsqueda anterior si está en curso
                searchJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(300) // Esperar 300ms después de la última modificación
                    val searchTerm = s.toString().trim()
                    if (searchTerm.isNotEmpty()) {
                        searchPokemon(searchTerm)
                    } else {
                        loadPokemonPage() // Cargar la página inicial si el texto está vacío
                    }
                }
            }
        })
    }

    private fun searchPokemon(name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val searchedPokemons = controller.searchPokemonByName(name)
                withContext(Dispatchers.Main) {
                    pokemonAdapter.clearPokemon()
                    if (searchedPokemons.isNotEmpty()) {
                        pokemonAdapter.addPokemon(searchedPokemons)
                    } else {
                        Toast.makeText(this@MainActivity, "No se encontraron Pokémon", Toast.LENGTH_SHORT).show()
                    }
                    updateButtonStates()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error al buscar Pokémon", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        pokemonAdapter = PokemonAdapter(this, emptyList())
        recyclerView.adapter = pokemonAdapter
    }

    private fun loadNextPage() {
        isLoading = true
        currentPage++
        loadPokemonPage()
    }

    private fun loadPreviousPage() {
        isLoading = true
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
                    isLoading = false // Marcar como carga finalizada
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error al cargar los datos", Toast.LENGTH_SHORT).show()
                    isLoading = false // Marcar como carga finalizada en caso de error
                }
            }
        }
    }

    private fun updateButtonStates() {
        buttonPrevious.isEnabled = currentPage > 0
        buttonNext.isEnabled = !noMorePokemon
    }
}