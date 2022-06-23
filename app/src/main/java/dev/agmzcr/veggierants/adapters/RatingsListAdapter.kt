package dev.agmzcr.veggierants.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.toObject
import dev.agmzcr.veggierants.databinding.ItemRatingBinding
import dev.agmzcr.veggierants.models.Rating
import dev.agmzcr.veggierants.utils.FORMAT

// Esta clase es un adapter para el RecyclerView
class RatingsListAdapter : ListAdapter<DocumentSnapshot, RatingsListAdapter.RatingsViewHolder>(RatingsComparator()) {

    // Crea un nuevo soporte de vista cuando no hay soportes de vista existentes
    // que el RecyclerView pueda reutilizar
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RatingsListAdapter.RatingsViewHolder {
        val binding = ItemRatingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RatingsViewHolder(binding)
    }

    // Llamado por RecyclerView para mostrar los datos en la posición especificada
    override fun onBindViewHolder(holder: RatingsListAdapter.RatingsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // Subclase para acceder a la vista de los elemetos que contiene el RecyclerView
    inner class RatingsViewHolder(private val binding: ItemRatingBinding):
            RecyclerView.ViewHolder(binding.root) {
                fun bind(document: DocumentSnapshot) {
                    val rating = document.toObject<Rating>()!!

                    binding.apply {
                        Glide.with(itemView)
                            .load(rating.userAvatar)
                            .centerCrop()
                            .into(ratingAvatar)

                        ratingName.text = rating.userName
                        ratingDate.text = FORMAT.format(rating.date!!)
                        ratingRating.rating = rating.rating!!.toFloat()
                        ratingComment.text = rating.comment
                    }
                }
            }

    /*Clase que compara la nueva lista con la anterior para averiguar
    que objeto se agregó, movió o elimino, generando una lista de operaciones de actualización.
    Es necesario para que funcione el ListAdapter */
    class RatingsComparator: DiffUtil.ItemCallback<DocumentSnapshot>() {
        override fun areItemsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
            return oldItem == newItem
        }

    }

}