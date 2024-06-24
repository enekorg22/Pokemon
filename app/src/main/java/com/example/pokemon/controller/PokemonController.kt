package com.example.pokemon.controller

import com.example.pokemon.model.Pokemon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class PokemonController {

    // Esta función se suspende y se ejecuta en el contexto de Dispatchers.IO
    suspend fun getAllPokemons(): List<Pokemon> = withContext(Dispatchers.IO) {
        val allPokemons = mutableListOf<Pokemon>()
        var nextUrl = "https://pokeapi.co/api/v2/pokemon"

        while (nextUrl.isNotEmpty()) {
            val api = URL(nextUrl)
            val response = api.readText()
            val json = JSONObject(response)

            val results = json.getJSONArray("results")
            val next = json.getString("next")
            nextUrl = if (next != "null") next else ""

            for (i in 0 until results.length()) {
                val pokemonJson = results.getJSONObject(i)
                val name = pokemonJson.getString("name")
                val url = pokemonJson.getString("url")
                allPokemons.add(Pokemon(name, url))
            }
        }

        allPokemons
    }

    // Nueva función para obtener una página de Pokémon según el offset
    suspend fun getPokemonPage(offset: Int, limit: Int = 20): List<Pokemon> = withContext(Dispatchers.IO) {
        val api = URL("https://pokeapi.co/api/v2/pokemon?offset=$offset&limit=$limit")
        val response = api.readText()
        val json = JSONObject(response)

        val results = json.getJSONArray("results")
        val pokemons = mutableListOf<Pokemon>()

        for (i in 0 until results.length()) {
            val pokemonJson = results.getJSONObject(i)
            val name = pokemonJson.getString("name")
            val url = pokemonJson.getString("url")
            pokemons.add(Pokemon(name, url))
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

        Pokemon(name, url, height, weight, types)
    }
}
