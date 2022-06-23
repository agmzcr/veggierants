package dev.agmzcr.veggierants.models


// Objeto de los usuarios
data class User(
    var id: String? = null,
    var name: String? = null,
    var email: String? = null,
    var password: String? = null,
    var avatar: String? = null
)
