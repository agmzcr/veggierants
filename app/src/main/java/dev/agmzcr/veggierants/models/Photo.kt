package dev.agmzcr.veggierants.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

// Objeto de las fotos de los restaurantes
data class Photo(
    val userId: String? = null,
    val userName: String? = null,
    val userAvatar: String? = null,
    val userEmail: String? = null,
    val photo: String? = null,
    @ServerTimestamp
    val date: Date? = null
)
