package com.example.pokemon.model

data class Pokemon (
    val name: String,
    val url: String,
    var height: Int = 0,
    var weight: Int = 0,
    var types: List<String> = emptyList()
)
