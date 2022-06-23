package dev.agmzcr.veggierants.utils


import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreException.Code.*
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageException.*
import java.text.SimpleDateFormat
import java.util.*

// Le damos formato a la fecha
val FORMAT = SimpleDateFormat(
    "dd/MM/yyyy", Locale.getDefault()
)

// Devuelve un String dependiendo del error que reciba
fun showFirestoreErrorMessage(errorCode: FirebaseFirestoreException.Code) : String? {
    var errorMessage = ""

    when (errorCode) {
        OK -> {
            errorMessage = "Ok"
        }
        CANCELLED -> {
            errorMessage = "La operación a sido cancelada"
        }
        UNKNOWN -> {
            errorMessage = "Error desconocido"
        }
        INVALID_ARGUMENT -> {
            errorMessage = "Argumento inválido"
        }
        DEADLINE_EXCEEDED -> {
            errorMessage = "La operación no a podido completarse"
        }
        NOT_FOUND -> {
            errorMessage = "No existe"
        }
        ALREADY_EXISTS -> {
            errorMessage = "Ya existe"
        }
        PERMISSION_DENIED -> {
            errorMessage = "Permiso denegado"
        }
        RESOURCE_EXHAUSTED -> {
            errorMessage = "No tienes espacio suficiente en la base de dato"
        }
        FAILED_PRECONDITION -> {
            errorMessage = "Fallo de precondición"
        }
        ABORTED -> {
            errorMessage = "Operación abortada"
        }
        OUT_OF_RANGE -> {
            errorMessage = "La operación se ha intento hacer varias veces pero no ha sido satisfactorio"
        }
        UNIMPLEMENTED -> {
            errorMessage = "La operación no esta implementada o no esta disponible"
        }
        INTERNAL -> {
            errorMessage = "Error interno. Contacta con el desarrollador."
        }
        UNAVAILABLE -> {
            errorMessage = "No se ha podido conectar con el servidor. Comprueba que tienes conexión a internet"
        }
        DATA_LOSS -> {
            errorMessage = "Datos corructos o irrecuperable."
        }
        UNAUTHENTICATED -> {
            errorMessage = "No estas identificado"
        }
        else -> {
            errorMessage = "Error sin identificación. Contacta con el desarrollador"
        }
    }
    return errorMessage
}

// Devuelve un String dependiendo del error que reciba
fun showStorageErrorMessage(error: StorageException) : String? {
    var errorMessage = ""

    if (error.errorCode != null) {
        when (error.errorCode) {
            ERROR_CANCELED -> {
                errorMessage = "La operación ha sido cancelada"
            }
            ERROR_BUCKET_NOT_FOUND -> {
                errorMessage = "El bucket no existe"
            }
            ERROR_PROJECT_NOT_FOUND -> {
                errorMessage = "El proyecto no existe"
            }
            ERROR_QUOTA_EXCEEDED -> {
                errorMessage = "Se ha superado la cuota del bucket"
            }
            ERROR_NOT_AUTHENTICATED -> {
                errorMessage = "No esta identificado"
            }
            ERROR_NOT_AUTHORIZED -> {
                errorMessage = "No tienes permisos suficientes"
            }
            ERROR_RETRY_LIMIT_EXCEEDED -> {
                errorMessage = "Se ha superado los intentos para realizar la operación. Comprueba la conexion a internet."
            }
            ERROR_INVALID_CHECKSUM -> {
                errorMessage = "El checksum del objeto no coincide"
            }
            ERROR_UNKNOWN -> {
                errorMessage = "Error desconocido"
            }
            else -> {
                errorMessage = "Error desconocido. Contacta con el desarrollador."
            }
        }
    } else {
        errorMessage = "Error desconocido. Contacta con el desarrollador.2"
    }

    return errorMessage
}