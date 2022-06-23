package dev.agmzcr.veggierants.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import dev.agmzcr.veggierants.databinding.ItemPhotoBinding
import dev.agmzcr.veggierants.interfaces.ClickListener
import dev.agmzcr.veggierants.models.Photo
import dev.agmzcr.veggierants.utils.FORMAT

// Esta clase es un adapter para el RecyclerView
class PhotosListAdapter(private val clickListener: ClickListener) : ListAdapter<DocumentSnapshot, PhotosListAdapter.PhotosViewHolder>(PhotosComparator()) {

    private val auth = Firebase.auth.currentUser

    // Crea un nuevo soporte de vista cuando no hay soportes de vista existentes
    // que el RecyclerView pueda reutilizar
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PhotosListAdapter.PhotosViewHolder {
        val binding = ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotosViewHolder(binding)
    }

    // Llamado por RecyclerView para mostrar los datos en la posición especificada
    override fun onBindViewHolder(holder: PhotosListAdapter.PhotosViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // Subclase para acceder a la vista de los elemetos que contiene el RecyclerView
    inner class PhotosViewHolder(private val binding: ItemPhotoBinding) :
            RecyclerView.ViewHolder(binding.root) {
                fun bind(document: DocumentSnapshot) {
                    val photo = document.toObject<Photo>()!!

                    binding.apply {
                        Glide.with(itemView)
                            .load(photo.photo)
                            .centerCrop()
                            .into(restaurantPhoto)

                        Glide.with(itemView)
                            .load(photo.userAvatar)
                            .circleCrop()
                            .into(photoAvatar)

                        photoDate.text = FORMAT.format(photo.date!!)
                    }

                    if (photo.userId == auth?.uid) {
                        binding.deleteButton.visibility = View.VISIBLE
                        binding.photoName.text = "Tú"
                    } else {
                        binding.deleteButton.visibility = View.INVISIBLE
                        binding.photoName.text = photo.userName.toString()
                    }

                    binding.deleteButton.setOnClickListener {
                        val position = adapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            val snapshot = getItem(position)
                            if (snapshot != null) {
                                clickListener.onClick(snapshot)
                            }
                        }
                    }
                }
            }

    /*Clase que compara la nueva lista con la anterior para averiguar
    que objeto se agregó, movió o elimino, generando una lista de operaciones de actualización.
    Es necesario para que funcione el ListAdapter */
    class PhotosComparator: DiffUtil.ItemCallback<DocumentSnapshot>() {
        override fun areItemsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
            return oldItem == newItem
        }

    }
}