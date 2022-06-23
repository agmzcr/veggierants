package dev.agmzcr.veggierants.interfaces

import com.google.firebase.firestore.DocumentSnapshot

// Interface para escuchar cuando el usuario le de click a algun restaurante de la lista
interface ClickListener {
    fun onClick(document: DocumentSnapshot)
}