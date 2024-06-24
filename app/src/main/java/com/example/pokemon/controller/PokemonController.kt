package com.example.pokemon.controller

import com.example.pokemon.model.Pokemon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class PokemonController {

    // Esta función se suspende y se ejecuta en el contexto de Dispatchers.IO
    suspend fun getPokemons(): List<Pokemon> = withContext(Dispatchers.IO) {
        // Define la URL de la API que proporciona la lista de Pokémon
        val api = URL("https://pokeapi.co/api/v2/pokemon?limit=151")

        // Realiza una solicitud GET a la API y lee la respuesta como texto
        val response = api.readText()

        // Convierte la respuesta de texto en un objeto JSON
        val json = JSONObject(response)

        // Extrae la lista de resultados de la respuesta JSON
        val results = json.getJSONArray("results")

        // Crea una lista mutable para almacenar los objetos Pokemon
        val pokemons = mutableListOf<Pokemon>()

        // Itera a través de la lista de resultados JSON
        for (i in 0 until results.length()) {
            // Obtén cada objeto Pokemon JSON individual
            val pokemonJson = results.getJSONObject(i)

            // Extrae el nombre y la URL del Pokemon del objeto JSON
            val name = pokemonJson.getString("name")
            val url = pokemonJson.getString("url")

            // Crea un objeto Pokemon y agrégalo a la lista de Pokemon
            pokemons.add(Pokemon(name, url))
        }

        // Retorna la lista completa de Pokemon obtenidos de la API
        pokemons
    }
}
