package com.example.tienda.screens.login

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tienda.R
import com.example.tienda.RequestLocationPermission
import com.example.tienda.saveLocationToFirebase
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var correo by remember { mutableStateOf("") }
    var contraseña by remember { mutableStateOf("") }
    var showConsentScreen by remember { mutableStateOf(false) }

    val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(
        context,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    )

    RequestLocationPermission(
        context = context,
        fusedLocationClient = fusedLocationClient,
        onLocationReceived = { location ->
            saveLocationToFirebase(context, location)
        }
    )

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).result
        account?.let {
            val credential = GoogleAuthProvider.getCredential(it.idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        handleLoginSuccess(navController, db, context, it.displayName ?: "Usuario") {
                            showConsentScreen = true
                        }
                    } else {
                        Toast.makeText(context, "Error de autenticación con Google: ${authTask.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Iniciar Sesión",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo Electrónico", color = MaterialTheme.colorScheme.onSurface) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = MaterialTheme.colorScheme.surface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )


        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = contraseña,
            onValueChange = { contraseña = it },
            label = { Text("Contraseña", color = MaterialTheme.colorScheme.onSurface) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = MaterialTheme.colorScheme.surface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                auth.signInWithEmailAndPassword(correo, contraseña)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            handleLoginSuccess(navController, db, context, null) {
                                showConsentScreen = true
                            }
                        } else {
                            Toast.makeText(context, "Error de autenticación: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Iniciar Sesión")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { googleSignInLauncher.launch(googleSignInClient.signInIntent) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Iniciar sesión con Google")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { navController.navigate("register") }
        ) {
            Text("¿No tienes cuenta? Regístrate aquí", color = MaterialTheme.colorScheme.primary)
        }
    }

    if (showConsentScreen) {
        ConsentScreen(
            onConsentGiven = {
                showConsentScreen = false
                navController.navigate("products")
            },
            onCancel = { showConsentScreen = false }
        )
    }
}

@Composable
fun ConsentScreen(onConsentGiven: () -> Unit, onCancel: () -> Unit) {
    AlertDialog(
        onDismissRequest = { /* No hacer nada para evitar cerrar el diálogo sin consentir */ },
        title = { Text("Consentimiento de Acceso", color = MaterialTheme.colorScheme.onBackground) },
        text = { Text("¿Aceptas compartir tus datos con la aplicación?", color = MaterialTheme.colorScheme.onBackground) },
        confirmButton = {
            Button(
                onClick = onConsentGiven,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Cancelar")
            }
        }
    )
}

private fun handleLoginSuccess(
    navController: NavController,
    db: FirebaseFirestore,
    context: Context,
    displayName: String?,
    onConsentNeeded: () -> Unit
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    userId?.let {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nombre = document.getString("nombre") ?: displayName
                    Toast.makeText(context, "Bienvenido $nombre", Toast.LENGTH_SHORT).show()
                    onConsentNeeded()
                } else {
                    db.collection("users").document(userId).set(mapOf("nombre" to displayName))
                        .addOnSuccessListener {
                            Toast.makeText(context, "Bienvenido $displayName", Toast.LENGTH_SHORT).show()
                            onConsentNeeded()
                        }
                }
            }
    }
}
