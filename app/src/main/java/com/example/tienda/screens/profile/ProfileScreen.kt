package com.example.tienda.screens.profile

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // Estado para los campos del formulario
    var nombre by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }

    // Estado para verificar si los datos están listos para mostrar
    var isDataLoaded by remember { mutableStateOf(false) }
    var isGoogleAccount by remember { mutableStateOf(false) }

    // Cargar datos del usuario desde Firebase si no es un registro con Google
    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            isGoogleAccount = auth.currentUser?.providerData?.any { it.providerId == "google.com" } == true
            if (!isGoogleAccount) {
                val userDocument = db.collection("users").document(userId).get().await()
                if (userDocument.exists()) {
                    nombre = userDocument.getString("nombre") ?: ""
                    edad = userDocument.getString("edad") ?: ""
                    telefono = userDocument.getString("telefono") ?: ""
                    correo = userDocument.getString("correo") ?: ""
                    isDataLoaded = true
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Configuración de Cuenta", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        if (isGoogleAccount) {
            // Mostrar mensaje si es una cuenta de Google
            Text(
                text = "Esta cuenta está vinculada a Google. Para realizar ajustes, por favor use la configuración de su cuenta de Google.",
                style = MaterialTheme.typography.bodyLarge
            )
        } else if (isDataLoaded) {
            // Mostrar el formulario de edición si no es cuenta de Google
            TextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = edad,
                onValueChange = { edad = it },
                label = { Text("Edad") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo Electrónico") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    saveProfileChanges(nombre, edad, telefono, correo, userId = auth.currentUser?.uid, db, context)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Cambios")
            }
        }
    }
}

// Función para guardar los cambios en Firebase
private fun saveProfileChanges(
    nombre: String,
    edad: String,
    telefono: String,
    correo: String,
    userId: String?,
    db: FirebaseFirestore,
    context: android.content.Context
) {
    if (userId != null) {
        val userUpdates = mapOf(
            "nombre" to nombre,
            "edad" to edad,
            "telefono" to telefono,
            "correo" to correo
        )
        db.collection("users").document(userId)
            .update(userUpdates)
            .addOnSuccessListener {
                Toast.makeText(context, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
