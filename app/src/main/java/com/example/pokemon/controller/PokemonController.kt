package com.example.pokemon.controller

import com.example.pokemon.model.Pokemon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class PokemonController {

    suspend fun getPokemonPage(offset: Int, limit: Int): List<Pokemon> = withContext(Dispatchers.IO) {
        val api = URL("https://pokeapi.co/api/v2/pokemon?offset=$offset&limit=$limit")
        val response = api.readText()
        val json = JSONObject(response)

        val results = json.getJSONArray("results")
        val pokemons = mutableListOf<Pokemon>()

        for (i in 0 until results.length()) {
            val pokemonJson = results.getJSONObject(i)
            val name = pokemonJson.getString("name")
            val url = pokemonJson.getString("url")

            val pokemon = Pokemon(name, url)
            pokemons.add(pokemon)
        }

        pokemons
    }

    suspend fun getPokemonDetails(url: String): Pokemon = withContext(Dispatchers.IO) {
        val api = URL(url)
        val response = api.readText()
        val json = JSONObject(response)

        val name = json.getString("name")
        val height = json.getInt("height")
        val weight = json.getInt("weight")
        val typesJson = json.getJSONArray("types")
        val types = mutableListOf<String>()

        for (j in 0 until typesJson.length()) {
            val type = typesJson.getJSONObject(j).getJSONObject("type").getString("name")
            types.add(type)
        }

        // Obtener la URL de la imagen
        val imageUrl = json.getJSONObject("sprites").getString("front_default")

        Pokemon(name, url, height, weight, types, imageUrl)
    }

    suspend fun getPokemonTypes(name: String): List<String> = withContext(Dispatchers.IO) {
        val url = "https://pokeapi.co/api/v2/pokemon/$name/"
        val api = URL(url)
        val response = api.readText()
        val json = JSONObject(response)

        val typesJson = json.getJSONArray("types")
        val types = mutableListOf<String>()

        if (typesJson.length() > 0) {
            val firstType = typesJson.getJSONObject(0).getJSONObject("type").getString("name")
            types.add(firstType)
        }

        types
    }

    suspend fun searchPokemonByName(name: String): List<Pokemon> = withContext(Dispatchers.IO) {
        try {
            val url = "https://pokeapi.co/api/v2/pokemon?offset=0&limit=1000" // Ajustar límite según necesidad
            val api = URL(url)
            val response = api.readText()
            val json = JSONObject(response)

            val results = json.getJSONArray("results")
            val pokemons = mutableListOf<Pokemon>()

            for (i in 0 until results.length()) {
                val pokemonJson = results.getJSONObject(i)
                val pokemonName = pokemonJson.getString("name")
                if (pokemonName.startsWith(name, ignoreCase = true)) {
                    val url = pokemonJson.getString("url")
                    val pokemon = getPokemonDetails(url)
                    pokemons.add(pokemon)
                }
            }

            pokemons
        } catch (e: Exception) {
            emptyList() // Devolver una lista vacía en caso de error
        }
    }

    // Método para obtener Pokémon por tipo
    suspend fun getPokemonByType(type: String): List<Pokemon> = withContext(Dispatchers.IO) {
        try {
            // Endpoint específico para obtener Pokémon por tipo
            val url = "https://pokeapi.co/api/v2/type/$type/"
            val api = URL(url)
            val response = api.readText()
            val json = JSONObject(response)

            val pokemonTypeArray = json.getJSONArray("pokemon")
            val pokemons = mutableListOf<Pokemon>()

            for (i in 0 until pokemonTypeArray.length()) {
                val pokemonJson = pokemonTypeArray.getJSONObject(i).getJSONObject("pokemon")
                val name = pokemonJson.getString("name")
                val url = pokemonJson.getString("url")

                // Obtener detalles adicionales del Pokémon si es necesario
                val pokemon = getPokemonDetails(url)
                pokemons.add(pokemon)
            }

            pokemons
        } catch (e: Exception) {
            emptyList() // Devolver una lista vacía en caso de error
        }
    }
}