package dev.agmzcr.veggierants.models

import com.google.firebase.firestore.Query

data class Filter(
    var theme: String? = null,
    var city: String? = null,
    var sortBy: String? = null,
    var sortDirection: Query.Direction? = null
) {
    companion object {
        val default: Filter
            get() {
                return Filter(
                    sortBy = "avgRatings",
                    sortDirection = Query.Direction.DESCENDING
                )
            }
    }
}
