package dev.agmzcr.veggierants.interfaces

import dev.agmzcr.veggierants.models.Filter

// Interface para escuchar y los elementos seleccionados de los Spinners
interface FilterListener {
    fun onFilter(filter: Filter)
}