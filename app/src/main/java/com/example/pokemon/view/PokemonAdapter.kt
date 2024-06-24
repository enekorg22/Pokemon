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
import com.example.pokemon.model.Pokemon

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
        holder.textViewPokemonName.text = pokemon.name

        holder.itemView.setOnClickListener {
            // Crear un Intent para abrir PokemonDetail y pasar el nombre y la URL del Pokémon
            val intent = Intent(context, PokemonDetailActivity::class.java).apply {
                putExtra("pokemon_name", pokemon.name)
                putExtra("pokemon_url", pokemon.url)
            }
            context.startActivity(intent)
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
