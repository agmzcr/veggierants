package dev.agmzcr.veggierants.dialogs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import dev.agmzcr.veggierants.databinding.FragmentRatingDialogBinding
import dev.agmzcr.veggierants.models.Rating
import dev.agmzcr.veggierants.models.Restaurant
import dev.agmzcr.veggierants.models.User
import dev.agmzcr.veggierants.utils.showFirestoreErrorMessage
import java.util.*


class RatingDialogFragment : DialogFragment() {

    // Variable de una instancia a la vinculación de los componentes del layout
    private lateinit var binding: FragmentRatingDialogBinding

    // Variable del id del restaurante y de la valoración
    private var restaurantId: String? = null
    private var ratingId: String? = null

    // Variable del usuario de Firebase Auth
    private val auth = Firebase.auth.currentUser

    // Variable de la instancia de Firestore y de las colecciones restaurants y users
    private val rootRef: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val restaurantsRef: CollectionReference = rootRef.collection("restaurants")
    private val usersRef: CollectionReference = rootRef.collection("users")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Iniciamos la vinculación de los componentes del layout
        binding = FragmentRatingDialogBinding.inflate(inflater, container, false)

        // Cargamos el id del restaurante enviado desde el activity
        restaurantId = requireArguments().getString("restaurant_id").toString()
        ratingId = requireArguments().getString("rating_id").toString()

        // Creamos la variable opteniendo el boolean enviado desde el activity
        val hasRating = requireArguments().getBoolean("hasRating")

        //Comprobamos si es true o false, si es true se configura para modificar o borrar
        //Si es false se configura para añadir una valoración
        if (hasRating) {
            editRatingSetup()
        } else {
            addRatingSetup()
        }

        return binding.root
    }

    // Configuramos el dialogo para que el usuario pueda añadir una valoración
    private fun addRatingSetup() {
        binding.apply {
            applyButton.setOnClickListener { getUserAndRestaurantData() }
            deleteButton.visibility = View.GONE
            ratingDialogTitle.text = "Añadir valoración"
            cancelButton.setOnClickListener { dismiss() }
        }
    }

    // Configuramos el dialogo para que el usuario pueda modificar o borrar la valoración
    private fun editRatingSetup() {
        stateLoading()
        var rating: Rating? = null

        rootRef.runTransaction { transition ->

            // Optenemos los datos de la valoracion y lo convertimos en el objeto Rating
            rating = transition.get(
                restaurantsRef.document(restaurantId!!)
                    .collection("ratings").document(ratingId!!)
            ).toObject<Rating>()!!
        }
            // Si la tarea a salido corretamente se muestran los datos en la interfaz
            // y se enlaza funciones al pulsar en los botones
            .addOnSuccessListener {
                binding.apply {
                    ratingComment.setText(rating!!.comment)
                    ratingRate.rating = rating!!.rating!!.toFloat()
                    cancelButton.setOnClickListener { dismiss() }
                    deleteButton.visibility = View.VISIBLE
                    ratingDialogTitle.text = "Editar o borrar valoración"
                    applyButton.setOnClickListener { getRestaurantDataForUpdate(rating) }
                    deleteButton.setOnClickListener { getRestaurantDataForDelete(rating) }
                }
                stateDone()
            }
            //Si da error se muestra con un Toast y cierra el dialogo
            .addOnFailureListener { error ->
                val firestoreError = error as FirebaseFirestoreException
                Toast.makeText(
                    activity,
                    showFirestoreErrorMessage(firestoreError.code),
                    Toast.LENGTH_SHORT
                ).show()
                dismiss()
            }
    }

    // Añadimos la valoración y actualizamos los datos del restaurante en la base de datos
    private fun addRatingAndUpdateRestaurant(user: User?, restaurant: Restaurant?) {
        val comment = binding.ratingComment.text.toString()
        val ratingFromUser = binding.ratingRate.rating.toDouble()

        // Comprobamos que el comentario no esta vacio y ni lo objetos user y restaurant son nulos
        if (comment.isNotBlank() && user != null && restaurant !=null) {
            val date = Timestamp(Date())
            val rating = Rating(user.id, user.name, user.avatar, user.email, comment, ratingFromUser, date.toDate())

            // Optenemos el nuevo numero de valoraciones sumandole 1
            val newNumRatings = restaurant.numRatings!!.plus(1)

            // Recalculamos la valoración media del restaurante
            val oldRatingTotal = restaurant.avgRatings!!.times(restaurant.numRatings!!)
            val newRatingTotal = oldRatingTotal.plus(ratingFromUser)
            val newAvgRating = newRatingTotal.div(newNumRatings)

            rootRef.runTransaction { transition ->
                // Añadimos la valoración
                transition.set(restaurantsRef.document(restaurantId!!)
                    .collection("ratings").document(), rating)

                // Actualizamos el restaurante
                transition.update(restaurantsRef.document(restaurantId!!)
                    ,"avgRatings", newAvgRating, "numRatings", newNumRatings)
            }
                // Si la tarea sale bien, lo avisa con un Toast y cierra el dialogo
                .addOnSuccessListener {
                    Toast.makeText(activity, "Valoración añadida!", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                // si no ha salido bien mostramos el error en un Toast
                .addOnFailureListener { error ->
                    Log.e("RatingDialogFragment", error.message.toString())
                    val firestoreError = error as FirebaseFirestoreException
                    Toast.makeText(activity, showFirestoreErrorMessage(firestoreError.code), Toast.LENGTH_SHORT).show()
                    stateDone()
                }
        } else {
            Toast.makeText(activity, "No puedes dejar el comentario en blanco", Toast.LENGTH_SHORT).show()
            stateDone()
        }
    }

    // Obtenemos los datos del usuario y del restaurante
    private fun getUserAndRestaurantData() {
        stateLoading()
        var user: User? = null
        var restaurant: Restaurant? = null

        rootRef.runTransaction { transition ->
            // Optenemos los datos del usuarios y lo convertimos en el objeto User
            user = transition.get(usersRef.document(auth!!.uid)).toObject<User>()!!

            // Optenemos los datos del restaurante y lo convertimos en el objeto Restaurant
            restaurant = transition.get(restaurantsRef.document(restaurantId!!)).toObject<Restaurant>()!!
        }
            // Si la tarea ha salido bien enviamos los onjetos user y restaurant para que se añada
            // a la base de datos
            .addOnSuccessListener {
                addRatingAndUpdateRestaurant(user, restaurant)
            }
            // si no ha salido bien mostramos el error en un Toast
            .addOnFailureListener { error ->
                val firestoreError = error as FirebaseFirestoreException
                Toast.makeText(context, showFirestoreErrorMessage(firestoreError.code), Toast.LENGTH_SHORT).show()
                Log.e("RatingDialogFragment", error.message.toString())
                stateDone()
            }
    }


    // Optenemos los datos del restaurante para actualizar la valoración
    private fun getRestaurantDataForUpdate(rating: Rating?) {
        stateLoading()
        var restaurant: Restaurant? = null

        rootRef.runTransaction { transition ->
            // Optenemos los datos del restaurante y lo convertimos en el objeto Restaurant
            restaurant =
                transition.get(restaurantsRef.document(restaurantId!!)).toObject<Restaurant>()!!
        }
            // si la tarea ha salido bien enviamos los objetos user y rating para que se actualizen
            // en la base de datos
            .addOnSuccessListener {
                updateRatingAndRestaurant(restaurant, rating)
            }
            // si no ha salido bien mostramos el error en un Toast
            .addOnFailureListener { error ->
                val firestoreError = error as FirebaseFirestoreException
                Toast.makeText(
                    activity,
                    showFirestoreErrorMessage(firestoreError.code),
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("RatingDialogFragment", error.message.toString())
                stateDone()
            }
    }

    // Actualizamos los datos de la valoración y del restaurante.
    private fun updateRatingAndRestaurant(restaurant: Restaurant?, rating: Rating?) {
        val newComment = binding.ratingComment.text.toString()
        val newRating = binding.ratingRate.rating.toDouble()

        // Comprobamos que el comentario no esta vacio y que los objetos no son nulos
        if (newComment.isNotBlank() && restaurant != null && rating != null) {
            stateLoading()

            // Calculamos la nueva valoracion media
            val oldRatingTotal = restaurant.avgRatings!!.times(restaurant.numRatings!!)
            val minusRating = oldRatingTotal.minus(rating.rating!!)
            val newRatingTotal = minusRating.plus(newRating)
            val newAvgRating = newRatingTotal.div(restaurant.numRatings!!)

            rootRef.runTransaction { transition ->
                // Actualizamos la valoración
                transition.update(restaurantsRef.document(restaurantId!!).collection("ratings").document(ratingId!!),
                    "comment", newComment, "rating", newRating)

                // Actualizamos el restaurante
                transition.update(restaurantsRef.document(restaurantId!!), "avgRatings", newAvgRating)
            }
                // Si la tarea ha salido bien, avisa con un toast y cierra el dialogo
                .addOnSuccessListener {
                    Log.i("RatingDialogFragment", "Update rating and restaurant done!")
                    Toast.makeText(context, "Valoración modificada!", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                // si no ha salido bien mostramos el error en un Toast
                .addOnFailureListener { error ->
                    Log.e("RatingDialogFragment", error.message.toString())
                    val firestoreError = error as FirebaseFirestoreException

                    Toast.makeText(
                        activity,
                        showFirestoreErrorMessage(firestoreError.code),
                        Toast.LENGTH_SHORT
                    ).show()

                    stateDone()
                }
        } else {
            Toast.makeText(activity, "No puedes dejar el comentario en blanco", Toast.LENGTH_SHORT).show()
        }
    }

    // Optenemos los datos del restaurante para borrar la valoracion
    private fun getRestaurantDataForDelete(rating: Rating?) {
        stateLoading()
        var restaurant: Restaurant? = null

        rootRef.runTransaction { transition ->
            // Optenemos los datos del restaurante y lo convertimos en el objeto Restaurant
            restaurant =
                transition.get(restaurantsRef.document(restaurantId!!)).toObject<Restaurant>()!!
        }
            // Si ha salido bien la tarea enviamos los objetos restaurant y rating
            // para que se borre de la base de datos y se actualize el restaurante
            .addOnSuccessListener {
                deleteRatingAndUpdateRestaurant(restaurant, rating)
            }
            // si no ha salido bien mostramos el error en un Toast
            .addOnFailureListener { error ->
                val firestoreError = error as FirebaseFirestoreException
                Toast.makeText(
                    activity,
                    showFirestoreErrorMessage(firestoreError.code),
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("RatingDialogFragment", error.message.toString())
                stateDone()
            }
    }

    // Borramos la valoración y actualizamos el restaurante
    private fun deleteRatingAndUpdateRestaurant(restaurant: Restaurant?, rating: Rating?) {
        if (restaurant != null && rating != null) {

            //Optenemos el nuevo numero de valoracion de total restandole 1
            val newNumRatings = restaurant.numRatings!!.minus(1)

            // Recalculamos la valoración media
            val oldRatingTotal = restaurant.avgRatings!!.times(restaurant.numRatings!!)
            val newRatingTotal = oldRatingTotal.minus(rating.rating!!)
            val newAvgRating = newRatingTotal.div(newNumRatings)

            // Borramos y actualizamos
            rootRef.runTransaction { transition ->

                // Borramos la valoración
                transition.delete(
                    restaurantsRef.document(restaurantId!!).collection("ratings").document(ratingId!!)
                )

                // Actualizamos el restaurante
                transition.update(
                    restaurantsRef.document(restaurantId!!),
                    "avgRatings",
                    newAvgRating,
                    "numRatings",
                    newNumRatings
                )
            }
                // Si ha salido bien la tarea este avisa con un Toas y cierra el dialogo
                .addOnSuccessListener {
                    Log.i("RatingDialogFragment", "Rating deleted and Restaurant updated!")
                    Toast.makeText(activity, "Valoración borrada!", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                // si no ha salido bien mostramos el error en un Toast
                .addOnFailureListener { error ->
                    Log.e("RatingDialogFragment", error.message.toString())
                    val firestoreError = error as FirebaseFirestoreException
                    Toast.makeText(
                        activity,
                        showFirestoreErrorMessage(firestoreError.code),
                        Toast.LENGTH_SHORT
                    ).show()
                    stateDone()
                }
        }
    }

    // Ocultamos elementos de la interfaz y mostramos un loading para dar la sensación al usuario
    // que la aplicación esta trabajando
    private fun stateLoading() {
        binding.apply {
            linearLayoutButtons.visibility = View.INVISIBLE
            ratingComment.visibility = View.INVISIBLE
            ratingRate.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
            ratingDialogTitle.visibility = View.INVISIBLE
        }
    }

    // Ocultamos el loading y mostramos elementos del interfaz para dar efecto de que la tarea a acabado
    private fun stateDone() {
        binding.apply {
            linearLayoutButtons.visibility = View.VISIBLE
            ratingComment.visibility = View.VISIBLE
            ratingRate.visibility = View.VISIBLE
            progressBar.visibility = View.INVISIBLE
            ratingDialogTitle.visibility = View.VISIBLE
        }
    }

    // Al reanudar el fragment le definimos los parámetros de su layout
    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    // Al cerrar el dialogo limpiamos el EditText y el RatingBar
    // Hacemos esto por un bug que al añadir y despues borrar al volver abrir el dialogo
    // se muestraban los datos de la valoracion antigua.
    override fun dismiss() {
        super.dismiss()
        binding.ratingRate.rating = 0.0F
        binding.ratingComment.setText("")
    }

}