package dev.agmzcr.veggierants.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import dev.agmzcr.veggierants.R
import dev.agmzcr.veggierants.adapters.RatingsListAdapter
import dev.agmzcr.veggierants.databinding.ActivityRatingsBinding
import dev.agmzcr.veggierants.models.Restaurant
import dev.agmzcr.veggierants.utils.showFirestoreErrorMessage

class RatingsActivity : AppCompatActivity() {

    // Variable de una instancia a la vinculación de los componentes del layout
    private lateinit var binding: ActivityRatingsBinding

    // Variable del id del restaurante
    private lateinit var restaurantId: String

    // Variable de la instancia de Firestore
    private val rootRef: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Variable de la referencia a la colección de los restaurantes
    private val restaurantsRef: CollectionReference = rootRef.collection("restaurants")

    // Variable del adapter para el RecyvclerView
    private val adapter = RatingsListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Iniciamos la vinculación de los componentes del layout
        binding = ActivityRatingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Habilitamos que se muestre el icono de retroceso en el ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        supportActionBar?.title = "Valoraciones"

        // Cargamos el id del restaurante que hemos enviado desde el otro activity
        restaurantId = intent.getStringExtra("restaurant_id").toString()

        // Configuramos el RecyvlerView
        setupRecyclerView()

        // Optenemos la lista de valoraciones desde Firestore
        getDataFromFirestore()
    }

    // Optenemos la lista de valoraciones de Firestore
    private fun getDataFromFirestore() {
        var ratingsList: MutableList<DocumentSnapshot> = ArrayList()

        restaurantsRef.document(restaurantId).collection("ratings")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                // Comprobamos si a dado error
                if (error != null) {
                    Log.i("RatingsActivity", error.message.toString())

                    // Mostramos el error en un Toast
                    Toast.makeText(this, showFirestoreErrorMessage(error.code), Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                // Comprobamos si hay datos del servidor
                if (snapshot != null) {

                    // Limpiamos la lista
                    ratingsList.clear()

                    // Añadimos a la variable la lista de documentos
                    ratingsList = snapshot.documents

                    // Enviamos la variable de la lista de documentos al adapter
                    adapter.submitList(ratingsList)

                    //Notificamos cambios en la lista al adapter
                    adapter.notifyDataSetChanged()
                }
            }
    }

    // Configuramos el RecyvlerView
    private fun setupRecyclerView() {
        binding.apply {
            recyclerView.layoutManager = LinearLayoutManager(this@RatingsActivity)
            recyclerView.adapter = adapter
            recyclerView.addItemDecoration(DividerItemDecoration(this@RatingsActivity, LinearLayoutManager.VERTICAL))
        }
    }

    // Enlazamos las lógicas a los elementos del menu de opciones al ser pulsado
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    // Movemos el usuario al DetailsActivity y enviamos el id del restaurante
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("restaurant_id", restaurantId)
        startActivity(intent)
        finish()
    }
}