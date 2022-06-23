package dev.agmzcr.veggierants.dialogs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.ktx.storage
import dev.agmzcr.veggierants.databinding.FragmentPhotoDialogBinding
import dev.agmzcr.veggierants.models.Photo
import dev.agmzcr.veggierants.models.User
import dev.agmzcr.veggierants.utils.showFirestoreErrorMessage
import dev.agmzcr.veggierants.utils.showStorageErrorMessage
import java.util.*

class PhotoDialogFragment : DialogFragment() {

    // Variable de una instancia a la vinculación de los componentes del layout
    private lateinit var binding: FragmentPhotoDialogBinding

    // Variable del id del restaurante
    private lateinit var restaurantId: String

    // Variable de la Uri de la imagen
    private var selectedPhotoUri: Uri? = null

    // Variable del usuario de Firebase Auth
    private val auth = Firebase.auth.currentUser

    // Variable de la instancia de Firebase Storage
    private val storageRef = Firebase.storage

    // Variable de la instancia de Firebase Firestore
    private val rootRef: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Variable de la referencia a la colección de los restaurantes de Firebase Firestore
    private val restaurantsRef: CollectionReference = rootRef.collection("restaurants")

    // Variable de la referencia a la colección de los usuarios de Firebase Firestore
    private val usersRef: CollectionReference = rootRef.collection("users")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Iniciamos la vinculación de los componentes del layout
        binding = FragmentPhotoDialogBinding.inflate(inflater, container, false)

        // Cargamos el id del restaurante enviado desde el activity
        restaurantId = requireArguments().getString("restaurant_id").toString()

        // Optenemos los datos del usuario desde la base de datos Firestore
        //getUserDataFromFirestore()

        // Enlazamos las logicas a los botones al ser pulsados
        binding.selectButton.setOnClickListener { onSelectClicked() }
        binding.cancelButton.setOnClickListener { dismiss() }
        binding.uploadButton.setOnClickListener { onUploadClicked() }

        // Limitamos a 10000 milisegundos los intentos de las operaciones de Firebase Storage
        storageRef.maxUploadRetryTimeMillis = 10000
        storageRef.maxDownloadRetryTimeMillis = 10000
        storageRef.maxOperationRetryTimeMillis = 10000

        return binding.root
    }

    // Subimos la imagen a Firebase Storage y enviamos el url de la imagen para subir la foto a Firebase Firestore
    private fun onUploadClicked() {
        // Comprobamos si el usuario ha seleccionado la foto
        if (selectedPhotoUri != null) {
            val fileName = UUID.randomUUID().toString() + ".jpg"
            val reference = storageRef.reference.child("/photos/$fileName")

            stateLoading()

            reference.putFile(selectedPhotoUri!!)

                // Si la tarea a sido correcta
                .addOnSuccessListener {
                    Log.i("AddPhotoDialogFragment", "Image uploaded to Storage!")

                    // Obtenemos la url de la foto para poder añadirla a la base de datos Firestore
                    reference.downloadUrl

                        // Si la tarea a sido correcta enviamos el uri
                        .addOnSuccessListener { uri ->

                            uploadPhotoToFirestore(uri.toString())
                        }
                        // Si la tarea no a sido correcta muestra el error en un Toast
                        .addOnFailureListener { error ->
                            Log.e("AddPhotoDialogFragment", error.toString())
                            val storageError = error as StorageException
                            Toast.makeText(context, showStorageErrorMessage(storageError), Toast.LENGTH_SHORT).show()
                            stateDone()
                        }

                }
                // Si la tarea no a sido correcta muestra el error en un Toast
                .addOnFailureListener { error ->
                    Log.e("AddPhotoDialogFragment", error.toString())
                    val storageError = error as StorageException
                    Toast.makeText(context, showStorageErrorMessage(storageError), Toast.LENGTH_SHORT).show()
                    stateDone()
                }

        } else {
            Toast.makeText(context, "Selecciona primero la foto!", Toast.LENGTH_SHORT).show()
        }
    }

    // Subimos la foto a Firebase
    private fun uploadPhotoToFirestore(uri: String) {
        val date = Timestamp(Date())

        rootRef.runTransaction { transition ->
            // Optenemos los datos del usuario convirtiendolo en objeto User
            val user = transition.get(usersRef.document(auth!!.uid)).toObject<User>()

            //Creamos la variable del objeto Photo con los datos en sus variables
            val photo = Photo(user!!.id, user.name, user.avatar, user.email, uri, date.toDate())

            // Añadimos los datos en Firestore
            transition.set(restaurantsRef.document(restaurantId).collection("photos").document(), photo)
        }
            // Si la tarea ha salido bien, muestra un Toast indicandolo y cierra el dialogo
            .addOnSuccessListener {
                Toast.makeText(context, "Foto subida correctamente!", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            // Si la tarea no a sido correcta muestra el error en un Toast
            .addOnFailureListener { error ->
                Log.e("AddPhotoDialogFragment", error.message.toString())
                val firestoreError = error as FirebaseFirestoreException
                Toast.makeText(context, showFirestoreErrorMessage(firestoreError.code), Toast.LENGTH_SHORT).show()
                stateDone()
            }
    }

    // Iniciamos la aplicación gestion de archivo del sistema
    private fun onSelectClicked() {
        try {
            val intent = Intent()
            intent.type = "image/"
            intent.action = Intent.ACTION_GET_CONTENT

            //Optenemos el resultado de la actividad
            startActivityForResult(intent, 100)
        } catch (e: Exception) {
            Toast.makeText(context, "Necesitas una aplicación de gestión de archivos", Toast.LENGTH_SHORT).show()
        }
    }

    // Comprobamos que se ha enviado la imagen y lo añadimos al ImageView para su previsualización
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            binding.photoImageView.setImageURI(selectedPhotoUri)
        }
    }

    // Ocultamos elementos de la interfaz y mostramos un loading para dar la sensación al usuario
    // que la aplicación esta trabajando
    private fun stateLoading() {
        binding.apply {
            photoImageView.visibility = View.INVISIBLE
            selectButton.visibility = View.INVISIBLE
            uploadButton.visibility = View.INVISIBLE
            cancelButton.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
        }
    }

    // Ocultamos el loading y mostramos elementos del interfaz para dar efecto de que la tarea a acabado
    private fun stateDone() {
        binding.apply {
            photoImageView.visibility = View.VISIBLE
            selectButton.visibility = View.VISIBLE
            uploadButton.visibility = View.VISIBLE
            cancelButton.visibility = View.VISIBLE
            progressBar.visibility = View.INVISIBLE
        }
    }

    // Al reanudar el fragment le definimos los parámetros de su layout
    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
    }

}