package com.example.pokemon.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pokemon.R
import com.example.pokemon.controller.PokemonController
import com.example.pokemon.model.Pokemon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class PokemonAdapter(
    private val context: Context,
    private var pokemonList: List<Pokemon>
) : RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pokemon, parent, false)
        return PokemonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val pokemon = pokemonList[position]
        holder.textViewPokemonName.text = pokemon.name.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.getDefault()
            ) else it.toString()
        }

        holder.itemView.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val detailedPokemon = PokemonController().getPokemonDetails(pokemon.url)

                    withContext(Dispatchers.Main) {
                        val intent = Intent(context, PokemonDetailActivity::class.java).apply {
                            putExtra("pokemon_name",
                                detailedPokemon.name.replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase(
                                        Locale.getDefault()
                                    ) else it.toString()
                                })
                            putExtra("pokemon_url", detailedPokemon.url)
                            putExtra("pokemon_height", detailedPokemon.height)
                            putExtra("pokemon_weight", detailedPokemon.weight)
                            val capitalizedTypes = detailedPokemon.types.map {
                                it.replaceFirstChar { char -> char.uppercaseChar() }
                            }.toTypedArray()
                            putExtra("pokemon_types", capitalizedTypes)
                        }
                        context.startActivity(intent)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun getItemCount(): Int = pokemonList.size

    class PokemonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewPokemonName: TextView = view.findViewById(R.id.textPokemonName)
    }

    // Método para actualizar los datos en el adaptador
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newPokemonList: List<Pokemon>) {
        pokemonList = newPokemonList
        notifyDataSetChanged()
    }
}
