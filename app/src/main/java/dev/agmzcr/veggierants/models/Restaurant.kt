package dev.agmzcr.veggierants.models

// Objeto de los restaurantes
data class Restaurant(
    val address: String? = null,
    val avgPrice: Int? = null,
    val avgRatings: Double? = null,
    val theme: String? = null,
    val city: String? = null,
    val logo: String? = null,
    val name: String? = null,
    val numRatings: Int? = null,
    val phone: Long? = null,
    val schedule: String? = null,
    val website: String? = null
)
