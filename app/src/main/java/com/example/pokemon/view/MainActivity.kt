package com.example.pokemon.view

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
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
    private val pageSize = 17

    private lateinit var buttonPrevious: Button
    private lateinit var buttonNext: Button
    private lateinit var buttonFilter: Button
    private lateinit var buttonClear: Button
    private lateinit var editTextSearch: EditText
    private lateinit var spinnerType: Spinner
    private lateinit var loadingView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        val imageViewLoadingGif: ImageView = findViewById(R.id.imageViewLoadingGif)
        Glide.with(this)
            .asGif()
            .load(R.drawable.loading_gif)
            .into(imageViewLoadingGif)

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
        spinnerType = findViewById(R.id.spinnerType)

        // Obtener la referencia al layout de carga
        loadingView = layoutInflater.inflate(R.layout.loading_view, null, false)
        addContentView(loadingView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        loadingView.visibility = View.GONE // Ocultar inicialmente el layout de carga

        // Configurar el Spinner de tipos de Pokémon en inglés
        val pokemonTypes = listOf("All", "Normal", "Fire", "Water", "Grass", "Electric", "Ice", "Fighting", "Poison", "Ground", "Flying", "Psychic", "Bug", "Rock", "Ghost", "Dragon", "Dark", "Steel", "Fairy")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, pokemonTypes)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = spinnerAdapter

        // Configurar los botones
        buttonPrevious.setOnClickListener { loadPreviousPage() }
        buttonNext.setOnClickListener { loadNextPage() }
        buttonFilter.setOnClickListener { filterPokemon() }
        buttonClear.setOnClickListener { clearFilter() }

        // Configurar el evento de clic del EditText de búsqueda
        editTextSearch.setOnEditorActionListener { _, _, _ ->
            val query = editTextSearch.text.toString()
            if (query.isNotEmpty()) {
                searchPokemon(query)
            }
            true
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editTextSearch.windowToken, 0)
    }

    private fun filterPokemon() {
        val selectedType = spinnerType.selectedItem.toString()
        val searchQuery = editTextSearch.text.toString().lowercase()

        hideKeyboard()
        showLoadingView()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val filteredPokemons = if (selectedType == "All" && searchQuery.isEmpty()) {
                    // Cargar la página actual de Pokémon si no hay filtros
                    controller.getPokemonPage(currentPage * pageSize, pageSize)
                } else if (selectedType == "All") {
                    // Filtrar solo por nombre si el tipo es "All"
                    controller.searchPokemonByName(searchQuery)
                } else if (searchQuery.isEmpty()) {
                    // Filtrar solo por tipo si la búsqueda está vacía
                    controller.getPokemonByType(selectedType.lowercase())
                } else {
                    // Filtrar por ambos, tipo y nombre
                    val pokemonsByType = controller.getPokemonByType(selectedType.lowercase())
                    pokemonsByType.filter { it.name.startsWith(searchQuery) }
                }

                withContext(Dispatchers.Main) {
                    hideLoadingView() // Ocultar la vista de carga al completar la filtración
                    pokemonAdapter.clearPokemon()
                    if (filteredPokemons.isNotEmpty()) {
                        pokemonAdapter.addPokemon(filteredPokemons)
                        recyclerView.scrollToPosition(0)
                    } else {
                        Toast.makeText(this@MainActivity, "No Pokémon found for type $selectedType and query $searchQuery", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    hideLoadingView() // Asegurarse de ocultar la vista de carga también en caso de error
                    Toast.makeText(this@MainActivity, "Error filtering Pokémon", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun clearFilter() {
        spinnerType.setSelection(0)
        editTextSearch.text.clear()
        loadPokemonPage()
    }

    private fun loadPreviousPage() {
        if (currentPage > 0) {
            currentPage--
            loadPokemonPage()
        }
    }

    private fun loadNextPage() {
        if (!noMorePokemon) {
            currentPage++
            loadPokemonPage()
        }
    }

    private fun loadPokemonPage() {
        isLoading = true
        showLoadingView()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val newPokemons = controller.getPokemonPage(currentPage * pageSize, pageSize)
                withContext(Dispatchers.Main) {
                    hideLoadingView()
                    pokemonAdapter.clearPokemon()
                    if (newPokemons.isNotEmpty()) {
                        pokemonAdapter.addPokemon(newPokemons)
                        recyclerView.scrollToPosition(0) // Desplazar al principio del RecyclerView
                        if (newPokemons.size < pageSize) {
                            noMorePokemon = true
                        } else {
                            noMorePokemon = false
                        }
                    } else {
                        noMorePokemon = true
                        Toast.makeText(this@MainActivity, "No se encontraron más Pokémon", Toast.LENGTH_SHORT).show()
                    }
                    isLoading = false
                    updateButtonStates()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    hideLoadingView()
                    Toast.makeText(this@MainActivity, "Error al cargar los datos", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
            }
        }
    }

    private fun searchPokemon(name: String) {
        showLoadingView()
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

    private fun showLoadingView() {
        // Asegurarse de que la vista de carga no se superponga
        if (loadingView.parent == null) {
            addContentView(loadingView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        }

        loadingView.visibility = View.VISIBLE

        // Deshabilitar la interacción con otros elementos de la interfaz
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )

        // Cargar el GIF de carga en el ImageView dentro de loadingView
        Glide.with(this)
            .asGif()
            .load(R.drawable.loading_gif)
            .into(loadingView.findViewById<ImageView>(R.id.imageViewLoading))
    }

    private fun hideLoadingView() {
        loadingView.visibility = View.GONE

        // Restaurar la interacción normal con la interfaz de usuario
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun updateButtonStates() {
        buttonPrevious.isEnabled = currentPage > 0
        buttonNext.isEnabled = !noMorePokemon && !isLoading
    }
}