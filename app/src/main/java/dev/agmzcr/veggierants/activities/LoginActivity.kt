package dev.agmzcr.veggierants.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dev.agmzcr.veggierants.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    // Variable de una instancia a la vinculación de los componentes del layout
    private lateinit var binding: ActivityLoginBinding

    // Variable de Firebase Auth
    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Iniciamos la vinculación de los componentes del layout
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cambiamos el título del ActionBar
        supportActionBar?.title = "Iniciar sesión"

        // Enlazamos la lógica al pulsar en el button o en el textview
        binding.loginButton.setOnClickListener { loginOnFirebaseAuth() }
        binding.registerTextView.setOnClickListener { onRegisterClicked() }
    }

    // Movemos al usuario al RegisterActivity
    private fun onRegisterClicked() {
        startActivity(Intent(this, RegisterActivity::class.java))
        finish()
    }

    // Enviamos los datos a Firebase Auth para poder iniciar sesión
    private fun loginOnFirebaseAuth() {
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                // Si la tarea es satisfactoria mueve el usuario al HomeActivity
            if (task.isSuccessful) {
                moveToHome()

                // Si no es satisfactoria muestra un Toast con el error
            } else {
                when (task.exception) {
                    is FirebaseAuthInvalidUserException -> {
                        Toast.makeText(this, "El email no existe o esta desactivado.", Toast.LENGTH_SHORT).show()
                        Log.e("LoginActivity", task.exception.toString())
                    }
                    is FirebaseAuthInvalidCredentialsException -> {
                        Toast.makeText(this, "El email o la contraseña son incorrectos.", Toast.LENGTH_SHORT).show()
                        Log.e("LoginActivity", task.exception.toString())
                    }
                    is FirebaseNetworkException -> {
                        Toast.makeText(this, "Error de conexión. Asegurate de tener conexión a internet.", Toast.LENGTH_SHORT).show()
                        Log.e("LoginActivity", task.exception.toString())
                    }
                    else -> {
                        Toast.makeText(this, "Error desconocido. Contacta con el desarrollador.", Toast.LENGTH_SHORT).show()
                        Log.e("LoginActivity", task.exception.toString())
                    }
                }
            }
        }
    }

    /* Comprobamos si ya hay un usuario ya autentificado.
    Firebase Auth permite recordar si un usuario ya se ha autentificado con anteriodad
    usando un token personalizado generado por el servidor
    y asi evitamos que el usuario tenga que iniciar sesión manualmente cada vez que abra la aplicación */
    private fun checkCurrentUser() {
        if (auth.currentUser != null) {
            moveToHome()
        }
    }

    // Al iniciar el activity comprueba si hay un usuario autentificado
    override fun onStart() {
        super.onStart()
        checkCurrentUser()
    }

    // Movemos al usuario al HomeActivity
    private fun moveToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}