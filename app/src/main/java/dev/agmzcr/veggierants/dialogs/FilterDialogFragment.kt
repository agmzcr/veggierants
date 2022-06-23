package dev.agmzcr.veggierants.dialogs

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.firebase.firestore.Query
import dev.agmzcr.veggierants.R
import dev.agmzcr.veggierants.databinding.FragmentFilterDialogBinding
import dev.agmzcr.veggierants.interfaces.FilterListener
import dev.agmzcr.veggierants.models.Filter


class FilterDialogFragment : DialogFragment() {

    // Variable de una instancia a la vinculación de los componentes del layout
    private lateinit var binding: FragmentFilterDialogBinding

    // Variable de instancia a la interfaz
    private var filterListener: FilterListener? = null

    private var filter = Filter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Iniciamos la vinculación de los componentes del layout
        binding = FragmentFilterDialogBinding.inflate(inflater, container, false)

        binding.applyButton.setOnClickListener { onApplyClicked() }
        binding.cancelButton.setOnClickListener { dismiss() }
        binding.resetButton.setOnClickListener { onResetClicked() }

        return binding.root
    }

    private fun buildFilter() {
        when (binding.spinnerSort.selectedItem as String) {
            "Ordenar por valoración media" -> {
                filter.sortBy = "avgRatings"
            }
            "Ordenar por número de valoraciones" -> {
                filter.sortBy = "numRatings"
            }
            "Ordenar por precio medio" -> {
                filter.sortBy = "avgPrice"
            }
        }

        filter.sortDirection = if (binding.orderByDe.isChecked) {
            Query.Direction.DESCENDING
        } else {
            Query.Direction.ASCENDING
        }


        filter.theme = if (binding.spinnerTheme.selectedItem as String != "Todos los restaurantes") {
            binding.spinnerTheme.selectedItem as String
        } else {
            null
        }

        filter.city = if (binding.spinnerCity.selectedItem as String != "Todas las ciudades") {
            binding.spinnerCity.selectedItem as String
        } else {
            null
        }
    }

    // Enviamos el objeto filter a la interfaz
    private fun onApplyClicked() {
        buildFilter()
        filterListener?.onFilter(filter)
        dismiss()
    }

    // Reiniciamos los spinners, ejecutamos la escucha y cerramos el dialogo
    private fun onResetClicked() {
        resetSpinners()
        buildFilter()
        filterListener?.onFilter(filter)
        dismiss()
    }

    // Ponemos todos los Spinners en la posición 0
    private fun resetSpinners() {
        binding.apply {
            spinnerTheme.setSelection(0)
            spinnerCity.setSelection(0)
            spinnerSort.setSelection(0)
            orderByDe.isChecked = true
        }
    }

    // Es llamado cuando el fragment esta asociado al activity
    // y le damos su context que es el interface
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FilterListener) {
            filterListener = context
        }
    }

    // Al reanudar el fragment le defenimos los parametros de su layout
    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}