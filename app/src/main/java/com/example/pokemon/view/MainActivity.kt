package com.example.pokemon.view

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
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
    private var isLoading = false
    private val pageSize = 50

    private lateinit var buttonPrevious: Button
    private lateinit var buttonNext: Button
    private lateinit var buttonFilter: Button
    private lateinit var buttonClear: Button
    private lateinit var editTextSearch: EditText

    private lateinit var loadingView: View // Vista para mostrar el GIF de carga

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
        setupViews()

        if (newPokemons.isNotEmpty()) {
            pokemonAdapter.addPokemon(newPokemons)
            if (newPokemons.size < pageSize) {
                noMorePokemon = true
            }
        } else {
            noMorePokemon = true
        }

        updateButtonStates()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        pokemonAdapter = PokemonAdapter(this, emptyList())
        recyclerView.adapter = pokemonAdapter

        buttonPrevious = findViewById(R.id.button_previous)
        buttonNext = findViewById(R.id.button_next)
        buttonFilter = findViewById(R.id.button_filter)
        buttonClear = findViewById(R.id.button_clear)
        editTextSearch = findViewById(R.id.editTextSearch)

        // Obtener la referencia al layout de carga
        loadingView = layoutInflater.inflate(R.layout.loading_view, null, false)
        addContentView(loadingView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        loadingView.visibility = View.GONE // Ocultar inicialmente el layout de carga

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

        buttonFilter.setOnClickListener {
            val searchTerm = editTextSearch.text.toString().trim()
            if (searchTerm.isNotEmpty()) {
                hideKeyboard()
                searchPokemon(searchTerm)
                buttonPrevious.visibility = View.GONE
                buttonNext.visibility = View.GONE
                showLoadingView() // Mostrar el GIF de carga al iniciar la búsqueda
            } else {
                Toast.makeText(this, "Ingrese un término de búsqueda", Toast.LENGTH_SHORT).show()
            }
        }

        buttonClear.setOnClickListener {
            editTextSearch.text.clear()
            loadPokemonPage() // Cargar la página inicial de Pokémon
            buttonPrevious.visibility = View.VISIBLE
            buttonNext.visibility = View.VISIBLE
        }
    }

    private fun searchPokemon(name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val searchedPokemons = controller.searchPokemonByName(name)
                withContext(Dispatchers.Main) {
                    hideLoadingView()
                    pokemonAdapter.clearPokemon()
                    if (searchedPokemons.isNotEmpty()) {
                        pokemonAdapter.addPokemon(searchedPokemons)
                        recyclerView.scrollToPosition(0) // Desplazar al principio del RecyclerView
                    } else {
                        Toast.makeText(this@MainActivity, "No se encontraron Pokémon", Toast.LENGTH_SHORT).show()
                    }
                    updateButtonStates()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    hideLoadingView()
                    Toast.makeText(this@MainActivity, "Error al buscar Pokémon", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
                        recyclerView.scrollToPosition(0) // Desplazar al principio del RecyclerView
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

    private fun showLoadingView() {
        loadingView.visibility = View.VISIBLE
        Glide.with(this)
            .asGif()
            .load(R.drawable.loading_gif)
            .into(loadingView.findViewById<ImageView>(R.id.imageViewLoading))
    }

    private fun hideLoadingView() {
        loadingView.visibility = View.GONE
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editTextSearch.windowToken, 0)
    }
}