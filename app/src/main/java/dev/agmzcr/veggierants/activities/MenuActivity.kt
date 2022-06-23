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
import com.google.firebase.firestore.ktx.toObject
import dev.agmzcr.veggierants.adapters.MenuListAdapter
import dev.agmzcr.veggierants.databinding.ActivityMenuBinding
import dev.agmzcr.veggierants.models.Menu
import dev.agmzcr.veggierants.utils.showFirestoreErrorMessage

class MenuActivity : AppCompatActivity() {

    // Variable de una instancia a la vinculación de los componentes del layout
    private lateinit var binding: ActivityMenuBinding

    // Variable de la instancia de Firestore
    private val rootRef: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Variable a la instancia a la colección restaurants
    private val restaurantsRef: CollectionReference = rootRef.collection("restaurants")

    // Variables de los adapters para los RecyclerViews
    private val adapterStarters = MenuListAdapter()
    private val adapterMaindishes = MenuListAdapter()
    private val adapterDesserts = MenuListAdapter()
    private val adapterDrinks = MenuListAdapter()

    // Variable del id del restaurante
    private lateinit var restautantId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Iniciamos la vinculación de los componentes del layout
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Habilitamos que se muestre el icono de retroceso en el action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        supportActionBar?.title = "Carta"

        // Cargarmos el id enviado desde el otro activity
        restautantId = intent.getStringExtra("restaurant_id").toString()

        // Configuramos el RecyclerView
        setupRecyclerViews()

        // Obtenemos los datos de Firestore
        getDessertsDataFromFirestore()
        getDrinksDataFromFirestore()
        getMainDishesDataFromFirestore()
        getStartersDataFromFirestore()
    }

    // Obtenemos los datos de los platos principales de Firestore
    private fun getMainDishesDataFromFirestore() {
        var maindishesList: MutableList<DocumentSnapshot> = ArrayList()

        restaurantsRef.document(restautantId).collection("maindishes")
            .addSnapshotListener { snapshot, error ->

                // Comprobamos si ha dado error
                if (error != null) {
                    Log.i("MenuActivity", error.message.toString())

                    // Mostramos el error en un Toast
                    Toast.makeText(this, showFirestoreErrorMessage(error.code), Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                // Comprobampos que hay datos del servidor
                if (snapshot != null && !snapshot.isEmpty) {

                    // Limpiamos la lista
                    maindishesList.clear()

                    // Añadimos a la variable la lista de documentos
                    maindishesList = snapshot.documents

                    // Enviamos la lista al adapter
                    adapterMaindishes.submitList(maindishesList)

                    // Notificamos al adapter que ha habido cambios en la lista
                    adapterMaindishes.notifyDataSetChanged()
                }
            }
    }

    // Obtenemos los datos de los postres de Firestore
    private fun getDessertsDataFromFirestore() {
        var dessertsList: MutableList<DocumentSnapshot> = ArrayList()

        restaurantsRef.document(restautantId).collection("desserts")
            .addSnapshotListener { snapshot, error ->

                // Comprobamos si ha dado error
                if (error != null) {
                    Log.i("MenuActivity", error.message.toString())

                    // Mostramos el error en un Toast
                    Toast.makeText(this, showFirestoreErrorMessage(error.code), Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                // Comprobamos que hay datos del servidor
                if (snapshot != null && !snapshot.isEmpty) {

                    // Limpiamos la lista
                    dessertsList.clear()

                    // Añadimos a la variable la lista de documentos
                    dessertsList = snapshot.documents

                    // Enviamos la lista al adapter
                    adapterDesserts.submitList(dessertsList)

                    // Notificamos al adapter que ha habido cambios en la lista
                    adapterDesserts.notifyDataSetChanged()
                }
            }
    }

    // Obtenemos los datos de los entrantes de Firestore
    private fun getStartersDataFromFirestore() {
        var startersList: MutableList<DocumentSnapshot> = ArrayList()

        restaurantsRef.document(restautantId).collection("starters")
            .addSnapshotListener { snapshot, error ->

                // Comprobamos si ha dado error
                if (error != null) {
                    Log.i("MenuActivity", error.message.toString())

                    // Mostramos el error en un Toast
                    Toast.makeText(this, showFirestoreErrorMessage(error.code), Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                // Comprobamos si hay datos del servidor
                if (snapshot != null && !snapshot.isEmpty) {

                    // Limpiamos la lista
                    startersList.clear()

                    // Añadimos a la variable la lista de documentos
                    startersList = snapshot.documents

                    // Enviamos la lista al adapter
                    adapterStarters.submitList(startersList)

                    // Notificamos al adapter que ha habiado cambios en la lista
                    adapterStarters.notifyDataSetChanged()
                }
            }
    }

    // Obtenemos los datos de las bebidas de Firestore
    private fun getDrinksDataFromFirestore() {
        var drinksList: MutableList<DocumentSnapshot> = ArrayList()

        restaurantsRef.document(restautantId).collection("drinks")
            .addSnapshotListener { snapshot, error ->

                // Comprobamos si ha dado error
                if (error != null) {
                    Log.i("MenuActivity", error.message.toString())

                    // Mostramos el error en un Toast
                    Toast.makeText(this, showFirestoreErrorMessage(error.code), Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                // Comprobamos si hay datos del servidor
                if (snapshot != null && !snapshot.isEmpty) {

                    // Limpiamos la lista
                    drinksList.clear()

                    // Añadimos a la variable la lista de documentos
                    drinksList = snapshot.documents

                    // Enviamos la lista al adapter
                    adapterDrinks.submitList(drinksList)

                    // Notificamos al adapter que ha habido cambios en la lista
                    adapterDrinks.notifyDataSetChanged()
                }
            }
    }
    // Configuramos los RecyclerViews
    private fun setupRecyclerViews() {
        binding.apply {
            startersRecyclerView.layoutManager = LinearLayoutManager(this@MenuActivity)
            startersRecyclerView.adapter = adapterStarters
            startersRecyclerView.addItemDecoration(DividerItemDecoration(this@MenuActivity, LinearLayoutManager.VERTICAL))

            maindishesRecyclerView.layoutManager = LinearLayoutManager(this@MenuActivity)
            maindishesRecyclerView.adapter = adapterMaindishes
            maindishesRecyclerView.addItemDecoration(DividerItemDecoration(this@MenuActivity, LinearLayoutManager.VERTICAL))

            dessertsRecyclerView.layoutManager = LinearLayoutManager(this@MenuActivity)
            dessertsRecyclerView.adapter = adapterDesserts
            dessertsRecyclerView.addItemDecoration(DividerItemDecoration(this@MenuActivity, LinearLayoutManager.VERTICAL))

            drinksRecyclerView.layoutManager = LinearLayoutManager(this@MenuActivity)
            drinksRecyclerView.adapter = adapterDrinks
            drinksRecyclerView.addItemDecoration(DividerItemDecoration(this@MenuActivity, LinearLayoutManager.VERTICAL))
        }
    }

    // Enlazamos las lógicas a los elementos del menu de opciones
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
        intent.putExtra("restaurant_id", restautantId)
        startActivity(intent)
        finish()
    }
}