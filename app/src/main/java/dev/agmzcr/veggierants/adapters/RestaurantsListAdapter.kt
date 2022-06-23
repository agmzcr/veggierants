package dev.agmzcr.veggierants.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.toObject
import dev.agmzcr.veggierants.databinding.ItemRestaurantBinding
import dev.agmzcr.veggierants.interfaces.ClickListener
import dev.agmzcr.veggierants.models.Restaurant

// Esta clase es un adapter para el RecyclerView
class RestaurantsListAdapter(
    private val clickListener: ClickListener
) : ListAdapter<DocumentSnapshot, RestaurantsListAdapter.RestaurantsViewHolder>(RestaurantsComparator()) {

    // Crea un nuevo soporte de vista cuando no hay soportes de vista existentes
    // que el RecyclerView pueda reutilizar
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RestaurantsListAdapter.RestaurantsViewHolder {
        val binding = ItemRestaurantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RestaurantsViewHolder(binding)
    }

    // Llamado por RecyclerView para mostrar los datos en la posición especificada
    override fun onBindViewHolder(holder: RestaurantsListAdapter.RestaurantsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // Subclase para acceder a la vista de los elemetos que contiene el RecyclerView
    inner class RestaurantsViewHolder(private val binding: ItemRestaurantBinding):
        RecyclerView.ViewHolder(binding.root) {

        // Si el usuario pulsa en algun elemento, le enviamos a la interfaz ese elemento
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val snapshot = getItem(position)
                    if (snapshot != null) {
                        clickListener.onClick(snapshot)
                    }
                }
            }
        }

        fun bind(document: DocumentSnapshot) {
            val restaurant = document.toObject<Restaurant>()!!

            binding.apply {

                Glide.with(itemView)
                    .load(restaurant.logo)
                    .centerCrop()
                    .into(restaurantLogo)

                restaurantName.text = restaurant.name
                restaurantCity.text = restaurant.city
                restaurantTheme.text = restaurant.theme
                restaurantPrice.text = "${restaurant.avgPrice}€"
                restaurantRating.rating = restaurant.avgRatings!!.toFloat()
                restaurantItemNumRatings.text = "(${restaurant.numRatings})"
            }

        }

    }

    /*Clase que compara la nueva lista con la anterior para averiguar
    que objeto se agregó, movió o eliminó, generando una lista de operaciones de actualización.
    Es necesario para que funcione el ListAdapter */
    class RestaurantsComparator : DiffUtil.ItemCallback<DocumentSnapshot>() {
        override fun areItemsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
            return  oldItem == newItem
        }

    }
}