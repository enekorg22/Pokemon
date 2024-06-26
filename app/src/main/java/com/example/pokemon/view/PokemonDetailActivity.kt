package com.example.pokemon.view

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.pokemon.R

class PokemonDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pokemon_detail)

        // Obtén los extras del intent
        val pokemonName = intent.getStringExtra("pokemon_name") ?: "Nombre no disponible"
        val pokemonHeight = intent.getIntExtra("pokemon_height", 0)
        val pokemonWeight = intent.getIntExtra("pokemon_weight", 0)
        val pokemonTypes = intent.getStringArrayExtra("pokemon_types")?.joinToString(", ") ?: "Tipo no disponible"
        val pokemonImageUrl = intent.getStringExtra("pokemon_image_url") ?: ""

        val formattedHeight = formatDecimal(pokemonHeight)
        val formattedWeight = formatDecimal(pokemonWeight)

        // Encuentra las vistas por ID y establece los textos
        val textViewPokemonName: TextView = findViewById(R.id.textViewPokemonName)
        val textViewPokemonHeight: TextView = findViewById(R.id.textViewPokemonHeight)
        val textViewPokemonWeight: TextView = findViewById(R.id.textViewPokemonWeight)
        val textViewPokemonTypes: TextView = findViewById(R.id.textViewPokemonTypes)
        val imageViewPokemon: ImageView = findViewById(R.id.imageViewPokemon)

        "Name: $pokemonName".also { textViewPokemonName.text = it }
        "Height: $formattedHeight m".also { textViewPokemonHeight.text = it }
        "Weight: $formattedWeight kg".also { textViewPokemonWeight.text = it }
        "Type: $pokemonTypes".also { textViewPokemonTypes.text = it }

        // Cargar la imagen usando Glide
        Glide.with(this)
            .load(pokemonImageUrl) // URL real desde la API
            .placeholder(R.drawable.loading_gif)
            .into(imageViewPokemon)
    }
}

fun formatDecimal(number: Int): String {
    val stringValue = number.toString()
    return if (stringValue.length > 1) {
        val newValue = StringBuilder(stringValue).insert(stringValue.length - 1, ".")
        newValue.toString()
    } else {
        "0.$stringValue"
    }
}
