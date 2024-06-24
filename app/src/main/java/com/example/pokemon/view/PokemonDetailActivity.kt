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
        val pokemonHeight = intent.getIntExtra("pokemon_height", 0)
        val pokemonWeight = intent.getIntExtra("pokemon_weight", 0)
        val pokemonTypes = intent.getStringExtra("pokemon_types") ?: "Tipo no disponible"

        val formattedHeight = formatDecimal(pokemonHeight)
        val formattedWeight = formatDecimal(pokemonWeight)

        // Encuentra las vistas por ID y establece los textos
        val textViewPokemonName: TextView = findViewById(R.id.textViewPokemonName)
        val textViewPokemonHeight: TextView = findViewById(R.id.textViewPokemonHeight)
        val textViewPokemonWeight: TextView = findViewById(R.id.textViewPokemonWeight)
        val textViewPokemonTypes: TextView = findViewById(R.id.textViewPokemonTypes)

        "Name: $pokemonName ".also { textViewPokemonName.text = it }
        "Height: $formattedHeight m".also { textViewPokemonHeight.text = it }
        "Weight: $formattedWeight kg".also { textViewPokemonWeight.text = it }
        "Type: $pokemonTypes".also { textViewPokemonTypes.text = it }

    }
}

fun formatDecimal(number: Int): String {
    val stringValue = number.toString()
    return if (stringValue.length > 1) {
        stringValue.substring(0, stringValue.length - 1) + "," + stringValue.substring(stringValue.length - 1)
    } else {
        "0,$stringValue"
    }
}