package dev.agmzcr.veggierants.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.toObject
import dev.agmzcr.veggierants.databinding.ItemMenuBinding
import dev.agmzcr.veggierants.models.Menu

// Esta clase es un adapter para el RecyclerView
class MenuListAdapter : ListAdapter<DocumentSnapshot, MenuListAdapter.MenuViewHolder>(MenuComparator()) {

    // Crea un nuevo soporte de vista cuando no hay soportes de vista existentes
    // que el RecyclerView pueda reutilizar
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuListAdapter.MenuViewHolder {
        val binding = ItemMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuViewHolder(binding)
    }

    // Llamado por RecyclerView para mostrar los datos en la posición especificada
    override fun onBindViewHolder(holder: MenuListAdapter.MenuViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // Clase para acceder a la vista de los elemetos que contiene el RecyclerView
    // y añadirle los datos
    inner class MenuViewHolder(private val binding: ItemMenuBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(document: DocumentSnapshot) {

            val menu = document.toObject<Menu>()!!

            binding.apply {
                menuName.text = menu.name
                menuPrice.text = "${menu.price}€"
            }
        }
    }

    /*Clase que compara la nueva lista con la anterior para averiguar
    que objeto se agregó, movió o elimino, generando una lista de operaciones de actualización.
    Es necesario para que funcione el ListAdapter */
    class MenuComparator: DiffUtil.ItemCallback<DocumentSnapshot>() {
        override fun areItemsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
            return oldItem == newItem
        }

    }
}