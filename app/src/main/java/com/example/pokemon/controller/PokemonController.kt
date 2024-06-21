package com.example.pokemon.controller

import com.example.pokemon.model.Pokemon
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class PokemonController {
    private val client = OkHttpClient()

    fun getPokemons(): List<Pokemon> {
        return try {
            val request = Request.Builder()
                .url("https://pokeapi.co/api/v2/pokemon?limit=150")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val responseData = response.body?.string()
                println("Response Data: $responseData") // Agregar esta línea para ver la respuesta en consola

                if (responseData != null) {
                    val json = JSONObject(responseData)
                    if (json.has("results")) {
                        val results = json.getJSONArray("results")
                        val pokemons = mutableListOf<Pokemon>()

                        for (i in 0 until results.length()) {
                            val pokemon = results.getJSONObject(i)
                            pokemons.add(
                                Pokemon(
                                    name = pokemon.getString("name"),
                                    url = pokemon.getString("url")
                                )
                            )
                        }
                        return pokemons
                    } else {
                        println("JSON no tiene la clave 'results'")
                        emptyList()
                    }
                } else {
                    println("Response data is null")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            println("Error fetching Pokémon data: ${e.message}")
            e.printStackTrace() // Imprimir el stack trace para más detalles
            emptyList()
        }
    }
}