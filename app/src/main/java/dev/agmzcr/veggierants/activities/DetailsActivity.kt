package dev.agmzcr.veggierants.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import dev.agmzcr.veggierants.R
import dev.agmzcr.veggierants.databinding.ActivityDetailsBinding
import dev.agmzcr.veggierants.dialogs.PhotoDialogFragment
import dev.agmzcr.veggierants.dialogs.RatingDialogFragment
import dev.agmzcr.veggierants.models.Menu
import dev.agmzcr.veggierants.models.Photo
import dev.agmzcr.veggierants.models.Rating
import dev.agmzcr.veggierants.models.Restaurant
import dev.agmzcr.veggierants.utils.FORMAT
import dev.agmzcr.veggierants.utils.showFirestoreErrorMessage

class DetailsActivity : AppCompatActivity() {

    // Variable de una instancia a la vinculación de los componentes del layout
    private lateinit var binding: ActivityDetailsBinding

    // Variables de las referencias de Firestore
    private val rootRef: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val restaurantsRef: CollectionReference = rootRef.collection("restaurants")

    // Variable de la id del restaurante
    private lateinit var restaurantId: String

    // Variable para que nos indica si el usuario tiene valoración en el restaurante o no
    private var hasRating = false

    // Variable de usuario actual
    private val auth = Firebase.auth.currentUser

    // Variable de la id de la valoración del usuario en caso de que tenga valoración
    private var ratingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Iniciamos la vinculación de los componentes del layout
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Habilitamos que se muestre el icono de la flecha de retroceso en el Action Bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Recogemos el String que hemos transferido desde el otro activity
        restaurantId = intent.getStringExtra("restaurant_id").toString()

        // Enlazamos la lógica a los botones al ser pulsados
        binding.showAllPhotosButton.setOnClickListener { showAllPhotosClicked() }
        binding.addPhotoButton.setOnClickListener { addPhotoClicked() }
        binding.showAllMenuButton.setOnClickListener { showAllMenuClicked() }
        binding.showAllRatingsButton.setOnClickListener { showAllRatingsClicked() }
        binding.addRatingButton.setOnClickListener { addOrEditRatingClicked() }

        // Obtenemos los datos de Firestore
        getDetailsDataFromFirestore()
        getPhotosDataFromFirestore()
        getMenuDataFromFirestore()
        getRatingDataFromFirestoreOfTheUser()
    }

    // Abre el dialogo para subir una foto
    private fun addPhotoClicked() {
        val fragmentManager = this@DetailsActivity.supportFragmentManager
        val photodialog = PhotoDialogFragment()
        val bundle = Bundle()
        bundle.putString("restaurant_id", restaurantId)
        photodialog.show(fragmentManager, "AddRating")
        photodialog.arguments = bundle
    }

    /* Dependiendo si el usuario tiene una valoración en el restaurante o no,
    * abre el dialogo para añadir una valoración o para modificarlo/borrarlo */
    private fun addOrEditRatingClicked() {
        val ratingDialog = RatingDialogFragment()
        val fragmentManager = this@DetailsActivity.supportFragmentManager
            if (hasRating) {
                val bundle = Bundle()
                bundle.putString("restaurant_id", restaurantId)
                bundle.putString("rating_id", ratingId)
                bundle.putBoolean("addOrEdit", hasRating)
                ratingDialog.show(fragmentManager, "EditRating")
                ratingDialog.arguments = bundle
            } else {
                val bundle = Bundle()
                bundle.putString("restaurant_id", restaurantId)
                bundle.putBoolean("addOrEdit", hasRating)
                ratingDialog.show(fragmentManager, "AddRating")
                ratingDialog.arguments = bundle
            }
    }

    // Mueve al usuario al GalleryActivity y envia el id del restaurante
    private fun showAllPhotosClicked() {
        val intent = Intent(this, GalleryActivity::class.java)
        intent.putExtra("restaurant_id", restaurantId)
        intent.putExtra("user_id", auth?.uid)
        startActivity(intent)
        finish()
    }

    // Mueve al usuario al RatingsActivity y envia el id del restaurante
    private fun showAllRatingsClicked() {
        val intent = Intent(this, RatingsActivity::class.java)
        intent.putExtra("restaurant_id", restaurantId)
        startActivity(intent)
        finish()
    }

    // Mueve al usuario al MenuActiviy y envia el id del restaurante
    private fun showAllMenuClicked() {
        val intent = Intent(this, MenuActivity::class.java)
        intent.putExtra("restaurant_id", restaurantId)
        startActivity(intent)
        finish()
    }

    // Le añadimos la función al pulsar el botón de retroceso
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    // Mueve al usuario al HomeActivity
    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    // Obtenemos los datos de Firestore del restaurante
    private fun getDetailsDataFromFirestore() {
        restaurantsRef.document(restaurantId)
            .addSnapshotListener { snapshot, error ->

                // Comprobamos si ha dado error
                if (error != null) {
                    Log.e("DetailsActivity", error.message.toString())
                    // Mostramos el error en un Toast
                    Toast.makeText(this, showFirestoreErrorMessage(error.code), Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                // Comprobamos si hay datos del servidor
                if (snapshot != null) {

                    // Lo convertimos en el objeto Restaurant
                    val restaurant = snapshot.toObject<Restaurant>()

                    // Lo enviamos a estas variables
                    restaurant?.let {
                        setDetailsData(it)
                    }
                }
            }
    }

    // Obtenemos los datos de Firestore de las fotos del restaurante
    private fun getPhotosDataFromFirestore() {
        val photosList: MutableList<Photo> = ArrayList()

        restaurantsRef.document(restaurantId).collection("photos")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                // Comprobamos si ha dado error
                if (error != null) {
                    Log.e("DetailsActivity", error.message.toString())
                    // Mostramos el error en un Toast
                    Toast.makeText(this, showFirestoreErrorMessage(error.code), Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                // Comprobamos si hay datos del servidor
                if (snapshot != null) {

                    // Limpiamos la lista
                    photosList.clear()

                    // Recorremos la lista que nos ha propocionado Firebase
                    // Convertimos caada documento al objeto Photo y lo añadimos a la variable
                    for (document in snapshot) {
                        val photo = document.toObject<Photo>()
                        photosList.add(photo)
                    }
                    // Enviamos la lista
                    setPhotosData(photosList)
                }
            }
    }

    // Obtenemos los datos de Firestore de los platos principales del restaurante
    private fun getMenuDataFromFirestore() {
        val maindishesList: MutableList<Menu> = ArrayList()

        restaurantsRef.document(restaurantId).collection("maindishes")
            .addSnapshotListener { snapshot, error ->

                // Comprobamos si ha dado error
                if (error != null) {
                    Log.e("DetailsActivity", error.message.toString())
                    // Mostramos el error en un Toast
                    Toast.makeText(this, showFirestoreErrorMessage(error.code), Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                // Comprobamos si hay datos del servidor
                if (snapshot != null) {

                    // Limpiamos la lista
                    maindishesList.clear()

                    // Recorremos la lista que nos ha propocionado Firebase
                    // Convertimos caada documento al objeto Menu y lo añadimos a la variable
                    for (document in snapshot) {
                        maindishesList.add(document.toObject<Menu>())
                    }

                    // Enviamos la lista
                    setMenuData(maindishesList)
                }
            }
    }

    // Intentamos obtener la valoración del usuario en caso de que tenga
    private fun getRatingDataFromFirestoreOfTheUser() {
        restaurantsRef.document(restaurantId).collection("ratings").whereEqualTo("userId", auth!!.uid)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("DetailsActivity", error.message.toString())
                    // Mostramos el error en un Toast
                    Toast.makeText(this, showFirestoreErrorMessage(error.code), Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                // Comprobamos si hay datos del servidor
                if (value != null && !value.isEmpty) {
                    // Creamos una variable de los datos transformado en el Objeto Rating
                    val rating = value.documents[0].toObject<Rating>()

                    // Indicamos al boolean que tiene una valoracion
                    hasRating = true

                    // Optenemos el id de la valoración para enviarselo al dialogo
                    ratingId = value.documents[0].id

                    // Enviamos la valoracion para que muestre los datos en la interfaz
                    setRatingDataOfTheUser(rating!!)
                } else {
                    /*En caso de que no optenga datos del servidor quiere decir que no tiene
                    una valoración, a si que ejecutamos una variable para que optenga la
                    ultima valoracion añadida*/
                    getLastRating()
                    hasRating = false
                }
            }
    }

    // Mostramos en la interfaz los datos de la valoracion del usuario
    private fun setRatingDataOfTheUser(rating: Rating) {
        binding.apply {
            Glide.with(applicationContext)
                .load(rating.userAvatar)
                .centerCrop()
                .into(ratingsAvatar)

            ratingsUserName.text = "Tú"
            ratingsDate.text = FORMAT.format(rating.date!!)
            ratingsRate.rating = rating.rating!!.toFloat()
            ratingsComment.text = rating.comment

            addRatingButton.text = "Editar valoración"
        }
    }

    // Optenemos los datos de la ultima valoración añadida.
    // Pero esta vez sin escucha permanente ya que la escucha permatente la tiene
    // la función para optener la valoracion del usuario
    private fun getLastRating() {
        restaurantsRef.document(restaurantId).collection("ratings").limit(1)
            .orderBy("date", Query.Direction.DESCENDING).get()
            // Si la tarea es correcta
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    //Creamos una variable del objeto Rating
                    val rating = it.documents[0].toObject<Rating>()

                    // Lo enviamos para que se muestre en la interfaz
                    setLastRating(rating)
                }
            }
            // Si da error lo mostramos con un Toast
            .addOnFailureListener { error ->
                val firestoreError = error as FirebaseFirestoreException
                Toast.makeText(this, showFirestoreErrorMessage(firestoreError.code), Toast.LENGTH_SHORT).show()
            }
    }

    // Mostramos en la interfaz los datos de la valoración
    // Primero comprobamos que el objeto Restaurant no es nula para evitar crasheos si accede por primera vez sin internet.
    private fun setLastRating(rating: Rating?) {
        if (rating != null) {
            binding.apply {
                Glide.with(applicationContext)
                    .load(rating.userAvatar)
                    .centerCrop()
                    .into(ratingsAvatar)

                ratingsUserName.text = rating.userName
                ratingsDate.text = FORMAT.format(rating.date!!)
                ratingsRate.rating = rating.rating!!.toFloat()
                ratingsComment.text = rating.comment

                addRatingButton.text = "Añadir valoración"
            }
        }
    }

    // Incrustamos los datos a la interfaz de los detalles del restaurante
    // Primero comprobamos que el objeto Restaurant no es nula para evitar crasheos si accede por primera vez sin internet.
    private fun setDetailsData(restaurant: Restaurant?) {
        if (restaurant != null) {
            binding.apply {
                nameRestaurant.text = restaurant.name
                addressRestaurant.text = restaurant.address
                cityRestaurant.text = restaurant.city
                themeRestaurant.text = restaurant.theme
                phoneRestaurant.text = restaurant.phone.toString()
                websiteRestaurant.text = restaurant.website
                priceAvgRestaurant.text = "Precio medio: ${restaurant.avgPrice}€"
                scheduleRestaurant.text = restaurant.schedule
                ratingAvgRestaurant.rating = restaurant.avgRatings!!.toFloat()
            }
        }
    }

    // Incrustamos los datos a la interfaz de las fotos del restaurante
    // Primero comprobamos que la lista no es nula ni esta vacia para evitar crasheos
    private fun setPhotosData(listPhotos: List<Photo>?) {
        if (!listPhotos.isNullOrEmpty()) {
            if (listPhotos.size == 1) {
                binding.apply {
                    Glide.with(applicationContext)
                        .load(listPhotos[0].photo)
                        .centerCrop()
                        .into(photo1)

                    photo2.setImageResource(R.drawable.no_pictures)
                }
            }

            if (listPhotos.size >= 2) {
                binding.apply {
                    Glide.with(applicationContext)
                        .load(listPhotos[0].photo)
                        .centerCrop()
                        .into(photo1)

                    Glide.with(applicationContext)
                        .load(listPhotos[1].photo)
                        .centerCrop()
                        .into(photo2)
                }
            }
        } else {
            binding.apply {
                photo1.setImageResource(R.drawable.no_pictures)
                photo2.setImageResource(R.drawable.no_pictures)
            }
        }
    }

    // Incrustamos los datos a la interfaz de la carta del restaurante
    // Primero comprobamos que la lista no es nula para evitar crasheos si accede por primera vez sin internet.
    private fun setMenuData(listMenu: List<Menu>?) {
        if (!listMenu.isNullOrEmpty()) {
            binding.apply {
                menuName1.text = listMenu[0].name
                menuPrice1.text = "${listMenu[0].price}€"

                menuName2.text = listMenu[1].name
                menuPrice2.text = "${listMenu[1].price}€"

                menuName3.text = listMenu[2].name
                menuPrice3.text = "${listMenu[2].price}€"
            }
        }
    }
}