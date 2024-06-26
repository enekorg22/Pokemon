package com.example.pokemon.controller

import com.example.pokemon.model.Pokemon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class PokemonController {

    suspend fun getPokemonPage(offset: Int, limit: Int = 150): List<Pokemon> = withContext(Dispatchers.IO) {
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

    // Función para obtener los tipos de un Pokémon por su nombre
    suspend fun getPokemonTypes(name: String): List<String> = withContext(Dispatchers.IO) {
        val url = "https://pokeapi.co/api/v2/pokemon/$name/"
        val api = URL(url)
        val response = api.readText()
        val json = JSONObject(response)

        val typesJson = json.getJSONArray("types")
        val types = mutableListOf<String>()

        for (j in 0 until typesJson.length()) {
            val type = typesJson.getJSONObject(j).getJSONObject("type").getString("name")
            types.add(type)
        }

        types
    }
}