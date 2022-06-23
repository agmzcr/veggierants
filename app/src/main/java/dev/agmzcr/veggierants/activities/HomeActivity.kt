package dev.agmzcr.veggierants.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import dev.agmzcr.veggierants.R
import dev.agmzcr.veggierants.adapters.RestaurantsListAdapter
import dev.agmzcr.veggierants.databinding.ActivityHomeBinding
import dev.agmzcr.veggierants.dialogs.FilterDialogFragment
import dev.agmzcr.veggierants.interfaces.ClickListener
import dev.agmzcr.veggierants.interfaces.FilterListener
import dev.agmzcr.veggierants.models.Filter
import dev.agmzcr.veggierants.utils.showFirestoreErrorMessage

// Le añadimos a la clase las interfazes de la lista y del dialogo
class HomeActivity : AppCompatActivity(), ClickListener, FilterListener {

    // Variable de una instancia a la vinculación de los componentes del layout
    private lateinit var binding: ActivityHomeBinding

    // Variable del dialogo
    private val filterDialog = FilterDialogFragment()

    // Variable del Firebase Auth
    private val auth = Firebase.auth

    // Variable que nos ayuda a ejecutar fragments
    private val fragmentManager = this@HomeActivity.supportFragmentManager

    // Variable de la instancia de Cloud Firestore
    private val rootRef: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Variable del adapter para el RecyclerView
    private val adapter = RestaurantsListAdapter(this)

    // Variable de la lista de documentos que nos da Firestore
    private var documentsList: MutableList<DocumentSnapshot> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
    }

    // Cargamos los datos de Firestore
    private fun getDataFromFirebaseFirestore(query: Query) {
            /* Firestore funciona online y offline, o sea, intenta optener los datos del servidor si no,
         * lo intenta desde el cache del dispositivo.
         * Con addSnapshotListener escuchamos los eventos producidos en Cloud Firestore
         * Con evento se considera a toda modificación que reciba la base de datos.
         * Al producirse el evento se vuelve a ejecutar, por eso primero limpiamos la lista antes
         * para evitar que se repitan los datos en la lista.
         * La escucha no da error si el dispositivo no tiene conexión a internet. */
            query.addSnapshotListener { snapshot, error ->

                // Comprobamos si la escucha a dado algún error.
                // En un principio estos errores son para el desarrollador, avisamos con un Toast genérico al usuario.
                if (error != null) {
                    Log.e("HomeActivity", error.message.toString())
                    Toast.makeText(this, showFirestoreErrorMessage(error.code), Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                // Comprobamos si los datos recibidos son nulo o la lista esta vacia
                if (snapshot != null && !snapshot.isEmpty) {

                    // Limpiamos la lista
                    documentsList.clear()

                    // Añadimos a la variables la lista de documentos
                    documentsList = snapshot.documents

                    // Enviamos la lista al adapter
                    adapter.submitList(documentsList)

                    // Avisamos al adapter que la lista tiene cambios
                    adapter.notifyDataSetChanged()

                // Si es nulo o la lista esta vacía
                // puede ser porque es la primera vez que accede
                } else {
                        Toast.makeText(this, "Si es la primera vez que accedes necesitas conexión a internet", Toast.LENGTH_SHORT).show()
                    }
                }
            }

    // Seleccionamos el menu de opciones para el action bar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.home_menu, menu)
        return true
    }

    // Enlazamos la lógica a los elementos del menu del action bar al ser pulsado
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            /* Si pulsas Cerrar Sesión.
             * Cerramos sesion desde Firebase auth
             * y movemos al usuario al LoginActivity */
            R.id.sign_out -> {
                auth.signOut()
                moveToLogin()
            }
            // Si pulsas el icono de filter abres el dialogo para filtar la lista
            R.id.filter_list -> {
                onFilterClicked()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Configuramos el RecyclerView
    private fun setupRecyclerView() {
        binding.apply {
            recyclerView.layoutManager = LinearLayoutManager(this@HomeActivity)
            recyclerView.adapter = adapter
            recyclerView.addItemDecoration(DividerItemDecoration(this@HomeActivity, LinearLayoutManager.VERTICAL))
        }
    }

    // Al ser click en cualquier restaurante, movemos el usuario a DetailsActivity
    // y enviamos id del restaurante.
    override fun onClick(document: DocumentSnapshot) {
        val id = document.id
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("restaurant_id", id)
        startActivity(intent)
        finish()
    }

    // Abrimos el dialogo
    private fun onFilterClicked() {
        filterDialog.show(fragmentManager, "Filter")
    }

    // Construimos el Query dependiendo de las variables del objeto Filter que hemos recibido
    // de la interface FilterListener
    override fun onFilter(filter: Filter) {
        var query: Query = rootRef.collection("restaurants")

        if (filter.theme != null) {
            query = query.whereEqualTo("theme", filter.theme)
        }

        if (filter.city != null) {
            query = query.whereEqualTo("city", filter.city)
        }

        if (filter.sortBy != null && filter.sortDirection != null) {
            query = query.orderBy(filter.sortBy!!, filter.sortDirection!!)
        }

        // Enviamos el Query
        getDataFromFirebaseFirestore(query)
    }

    // Movemos al usuario a LoginActivity
    private fun moveToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    // Al iniciar el activity enviamos el objeto Filter que tenemos por defecto
    override fun onStart() {
        super.onStart()
        onFilter(Filter.default)
    }
}