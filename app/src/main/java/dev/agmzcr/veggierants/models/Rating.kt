package dev.agmzcr.veggierants.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

// Objeto de las valoraciones de los restaurantes
data class Rating(
    val userId: String? = null,
    val userName: String? = null,
    val userAvatar: String? = null,
    val userEmail: String? = null,
    val comment: String? = null,
    val rating: Double? = null,
    @ServerTimestamp
    val date: Date? = null
)
