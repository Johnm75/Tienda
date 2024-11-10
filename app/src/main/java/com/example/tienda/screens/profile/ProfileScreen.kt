// ProfileScreen.kt
package com.example.tienda.screens.profile

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tienda.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()


    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var deleteCountdown by remember { mutableStateOf(5) }
    var isDeleteButtonEnabled by remember { mutableStateOf(false) }

    val isGoogleAccount = remember {
        auth.currentUser?.providerData?.any { it.providerId == "google.com" } == true
    }

    var hasRedirected by remember { mutableStateOf(false) }

    if (isGoogleAccount && !hasRedirected) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Esta cuenta está vinculada a Google. Para realizar ajustes, use la configuración de su cuenta de Google.", Toast.LENGTH_LONG).show()
            navController.navigate("products") {
                popUpTo("profile") { inclusive = true }
            }
            hasRedirected = true
        }
        return
    }

    var nombre by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var profileImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imagePath by remember { mutableStateOf<String?>(null) }

     fun saveImageToInternalStorage(context: Context, bitmap: Bitmap): String? {
        val filename = "profile_image.png"
        return try {
            val file = File(context.filesDir, filename)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val photo = result.data?.extras?.get("data") as? Bitmap
            if (photo != null) {
                profileImageBitmap = photo
                imagePath = saveImageToInternalStorage(context, photo)
            }
        }
    }

  fun saveProfileChanges(
        context: Context,
        nombre: String,
        edad: String,
        telefono: String,
        correo: String,
        imagePath: String?,
        db: FirebaseFirestore
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            val userUpdates = hashMapOf(
                "nombre" to nombre,
                "edad" to edad,
                "telefono" to telefono,
                "correo" to correo,
                "profileImagePath" to imagePath
            )
            db.collection("users").document(userId).update(userUpdates as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(context, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error al actualizar perfil: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }




    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userDocument = db.collection("users").document(userId).get().await()
            if (userDocument.exists()) {
                nombre = userDocument.getString("nombre") ?: ""
                edad = userDocument.getString("edad") ?: ""
                telefono = userDocument.getString("telefono") ?: ""
                correo = userDocument.getString("correo") ?: ""
                imagePath = userDocument.getString("profileImagePath")

                imagePath?.let {
                    profileImageBitmap = BitmapFactory.decodeFile(it)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Configuración de Perfil", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .clickable {
                    val cameraIntent = android.provider.MediaStore.ACTION_IMAGE_CAPTURE
                    cameraLauncher.launch(Intent(cameraIntent))
                },
            contentAlignment = Alignment.Center
        ) {
            profileImageBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Imagen de Perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } ?: run {
                Image(
                    painter = painterResource(id = R.drawable.ic_default_profile),
                    contentDescription = "Imagen de Perfil Predeterminada",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = edad,
            onValueChange = { edad = it },
            label = { Text("Edad") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Teléfono") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo Electrónico") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                saveProfileChanges(context, nombre, edad, telefono, correo, imagePath, db)
                navController.navigate("products") {
                    popUpTo("profile") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar Cambios")
        }


        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                showDeleteConfirmationDialog = true
                deleteCountdown = 5
                isDeleteButtonEnabled = false
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Eliminar Cuenta")
        }

        if (showDeleteConfirmationDialog) {
            DeleteConfirmationDialog(
                onConfirm = {
                    deleteUserAccount(navController, context)
                    showDeleteConfirmationDialog = false
                },
                onCancel = {
                    showDeleteConfirmationDialog = false
                },
                countdown = deleteCountdown,
                isButtonEnabled = isDeleteButtonEnabled
            )
            LaunchedEffect(deleteCountdown) {
                while (deleteCountdown > 0) {
                    delay(1000)
                    deleteCountdown--
                }
                isDeleteButtonEnabled = true
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    countdown: Int,
    isButtonEnabled: Boolean
) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text("Confirmar Eliminación") },
        text = { Text("¿Estás seguro de que deseas eliminar tu cuenta? Este proceso es irreversible. Espera $countdown segundos para habilitar el botón de confirmación.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = isButtonEnabled,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            Button(onClick = onCancel) {
                Text("Cancelar")
            }
        }
    )
}

private fun deleteUserAccount(navController: NavController, context: Context) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    userId?.let {
        db.collection("users").document(userId).delete().addOnSuccessListener {
            auth.currentUser?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Cuenta eliminada", Toast.LENGTH_SHORT).show()
                    navController.navigate("login") {
                        popUpTo("products") { inclusive = true }
                    }
                } else {
                    Toast.makeText(context, "Error al eliminar cuenta: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
