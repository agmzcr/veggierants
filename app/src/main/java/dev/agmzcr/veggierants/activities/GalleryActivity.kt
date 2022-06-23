package dev.agmzcr.veggierants.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.firestore.*
import com.google.firebase.firestore.FirebaseFirestoreException.Code.*
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import dev.agmzcr.veggierants.adapters.PhotosListAdapter
import dev.agmzcr.veggierants.databinding.ActivityGalleryBinding
import dev.agmzcr.veggierants.interfaces.ClickListener
import dev.agmzcr.veggierants.models.Photo
import dev.agmzcr.veggierants.utils.showFirestoreErrorMessage
import dev.agmzcr.veggierants.utils.showStorageErrorMessage

class GalleryActivity : AppCompatActivity(), ClickListener {

    // Variable de una instancia a la vinculación de los componentes del layout
    private lateinit var binding: ActivityGalleryBinding

    // Variable a la instancia de Cloud Firestore
    private val rootRef: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Variable de la referencia a la colección de los restaurantes
    private val restaurantsRef: CollectionReference = rootRef.collection("restaurants")

    // Variable del adapter para el ViewPager
    private val adapter = PhotosListAdapter(this)

    // Variable del id del restaurante
    private lateinit var restaurantId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Iniciamos la vinculación de los componentes del layout
        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Habilitamos que se muestre el icono de retroceso en el ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        supportActionBar?.title = "Fotos"

        // Cargamos el id del restaurante que hemos enviado desde el otro activity
        restaurantId = intent.getStringExtra("restaurant_id").toString()


        // Optenemos la lista de fotos de Firestore
        getDataFromFirestore()

        // Cargamos el adapter al ViewPager
        binding.viewPager.adapter = adapter
    }

    // Optenemos la lista de fotos de Firestore
    private fun getDataFromFirestore() {
        var photosList: MutableList<DocumentSnapshot> = ArrayList()

        restaurantsRef.document(restaurantId).collection("photos")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                // Comprobamos si a dado error
                if (error != null) {
                    Log.e("GalleryActivity", error.message.toString())

                    // Mostramos el error en un Toast
                    Toast.makeText(this, showFirestoreErrorMessage(error.code), Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                // Comprobamos si hay datos del servidor
                if (snapshot != null && !snapshot.isEmpty) {

                    //Limpiamos la lista
                    photosList.clear()

                    // Añadimos a la variable la lista de documentos
                    photosList = snapshot.documents

                    // Enviamos la lista al adapter del ViewPager
                    adapter.submitList(photosList)

                    //Notificamos cambios en la lista al adapter
                    adapter.notifyDataSetChanged()
                }
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
        intent.putExtra("restaurant_id", restaurantId)
        startActivity(intent)
        finish()
    }

    override fun onClick(document: DocumentSnapshot) {
        stateLoading()
        val photo = document.toObject<Photo>()
        val storageReference: StorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(photo?.photo!!)
        storageReference.delete()
            .addOnSuccessListener {
                    restaurantsRef.document(restaurantId).collection("photos").document(document.id)
                    .delete()
                    .addOnSuccessListener {
                        stateDone()
                        Toast.makeText(this, "Foto borrada!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { error ->
                        stateDone()
                        val firestoreError = error as FirebaseFirestoreException
                        Toast.makeText(this, showFirestoreErrorMessage(firestoreError.code), Toast.LENGTH_SHORT).show()
                    }
            Log.i("STODELETED", "Done")
        }
            .addOnFailureListener { error ->
                stateDone()
                val storageError = error as StorageException
                Toast.makeText(this, showStorageErrorMessage(storageError), Toast.LENGTH_SHORT).show()
                Log.i("STODELETED", "error while deleting")
        }
    }

    private fun stateLoading() {
        binding.apply {
            progressBar.visibility = View.VISIBLE
            viewPager.visibility = View.INVISIBLE
        }
    }

    private fun stateDone() {
        binding.apply {
            progressBar.visibility = View.INVISIBLE
            viewPager.visibility = View.VISIBLE
        }
    }
}