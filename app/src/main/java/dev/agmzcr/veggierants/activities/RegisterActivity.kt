package dev.agmzcr.veggierants.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageException
import dev.agmzcr.veggierants.databinding.ActivityRegisterBinding
import dev.agmzcr.veggierants.models.User
import dev.agmzcr.veggierants.utils.showFirestoreErrorMessage

class RegisterActivity : AppCompatActivity() {

    // Variable de una instancia a la vinculación de los componentes del layout
    private lateinit var binding: ActivityRegisterBinding

    // Variable de la instancia de Firebase Auth
    private val auth = Firebase.auth

    // Variable de la instancia de Firestore
    private val rootRef: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Variable a la instancia a la colección users
    private val userRef: CollectionReference = rootRef.collection("users")

    // Variable del avatar por defecto
    private val userAvatar = "https://firebasestorage.googleapis.com/v0/b/veggierants.appspot.com/o/user.png?alt=media&token=6e700ca8-b005-49c5-aa2f-a39b56c079a7"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Iniciamos la vinculación de los componentes del layout
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Habilitamos que se muestre el icono de la flecha de retroceso
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Cambiamos el título del ActionBar
        supportActionBar?.title = "Registro"

        // Enlazamos la lógica al pulsar el boton de Registrarse
        binding.registerButton.setOnClickListener { registerOnFirebaseAuth() }
    }

    // Enlazamos las lógicas a los elementos del menu de opciones
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    // Navegación hacia LoginActivity
    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    // Añadimos el usuario a Firebase Auth
    private fun registerOnFirebaseAuth() {

        val name = binding.nameEditText.text.toString()
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
            stateLoading()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    // Si la tarea es existosa añade el usuario al Cloud Firestore
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        addUserOnFirebaseFirestore(userId!!, name, email, password)

                        /*En caso de que no sea existoso daremos el error con un Toast
                Se que hay errores que nunca van a salir por la validación
                de los EditTexts, pero igualmente los he añadidos.*/
                    } else {
                        when (task.exception) {
                            is FirebaseAuthWeakPasswordException -> {
                                Toast.makeText(
                                    this,
                                    "La contraseña no es lo suficiente larga. Se necesitan minimo 6 caracteres.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                stateDone()
                                Log.e("RegisterActivity", task.exception.toString())
                            }
                            is FirebaseAuthInvalidCredentialsException -> {
                                Toast.makeText(
                                    this,
                                    "El email esta mal formado.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                stateDone()
                                Log.e("RegisterActivity", task.exception.toString())
                            }
                            is FirebaseAuthUserCollisionException -> {
                                Toast.makeText(
                                    this,
                                    "El email ya esta registrado.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                stateDone()
                                Log.e("RegisterActivity", task.exception.toString())
                            }
                            is FirebaseNetworkException -> {
                                Toast.makeText(
                                    this,
                                    "Error de conexión. Asegurate de tener el internet activo.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                stateDone()
                                Log.e("RegisterActivity", task.exception.toString())
                            }
                            else -> {
                                Toast.makeText(this, "Error desconocido. Contacta con el desarrollador.", Toast.LENGTH_SHORT).show()
                                stateDone()
                                Log.e("RegisterActivity", task.exception.toString())
                            }
                        }
                    }
                }
        } else {
            Toast.makeText(this, "No puedes dejar nada en blanco.", Toast.LENGTH_SHORT).show()
        }
    }

    // Funcion para meter el usuario en la base de datos Cloud Firestore
    private fun addUserOnFirebaseFirestore(id: String, name: String, email: String, password: String) {

        // Metemos los datos en las variables del objeto User
        val user = User(id, name, email, password, userAvatar)

        // Añadimos el usuario a la base de datos Cloud Firestore
        userRef.document(id)
            .set(user)
            // Si la tarea es satifastoria lo avisa con un Toast
            // y mueve al usuario al HomeActivity
            .addOnSuccessListener {
                Toast.makeText(this, "Usuario: $email creado correctamente", Toast.LENGTH_SHORT).show()
                Log.i("RegisterActivity", "User: $email created!")
                moveToHome()
            }

            .addOnFailureListener { error ->
                // Mostramos el error en un Toast
                val firestoreError = error as FirebaseFirestoreException
                Toast.makeText(this, showFirestoreErrorMessage(firestoreError.code), Toast.LENGTH_SHORT).show()
                Log.e("RegisterActivity", error.code.toString())
                deleteUserFromFirebaseAuth()
                }
            }

    private fun deleteUserFromFirebaseAuth() {
        auth.currentUser!!.delete()
        Log.i("RegisterActivity", "User deleted!")
        stateDone()
    }

    // Movemos al usuario al HomeActivity
    private fun moveToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    // Mostramos el circulo de loading
    private fun stateLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    // Ocultamos el circulo de loading
    private fun stateDone() {
        binding.progressBar.visibility = View.INVISIBLE
    }
}