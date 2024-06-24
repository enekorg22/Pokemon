package com.example.pokemon.view

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.pokemon.R

class PokemonDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pokemon_detail)

        // Obtén los extras del intent
        val pokemonName = intent.getStringExtra("pokemon_name") ?: "Nombre no disponible"
        val pokemonUrl = intent.getStringExtra("pokemon_url") ?: "URL no disponible"

        // Encuentra las vistas por ID y establece los textos
        val textViewPokemonName: TextView = findViewById(R.id.textViewPokemonName)
        val textViewPokemonUrl: TextView = findViewById(R.id.textViewPokemonUrl)

        textViewPokemonName.text = pokemonName
        textViewPokemonUrl.text = pokemonUrl

        // Aquí puedes agregar más detalles y lógica si lo necesitas
    }
}
