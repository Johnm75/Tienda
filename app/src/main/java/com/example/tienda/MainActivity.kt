package com.example.tienda

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tienda.models.Product
import com.example.tienda.screens.cart.CartScreen
import com.example.tienda.screens.products.ProductsScreen
import com.example.tienda.screens.register.RegisterScreen
import com.example.tienda.screens.profile.ProfileScreen
import com.example.tienda.screens.login.LoginScreen
import com.example.tienda.ui.theme.TiendaTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.core.app.ActivityCompat

@OptIn(ExperimentalPagerApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TiendaTheme {
                val cartItems = remember { mutableStateListOf<Product>() }
                MainScreen(cartItems = cartItems)
            }
        }
    }
}

fun saveLocationToFirebase(context: Context, location: Location) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    userId?.let {
        val userLocation = hashMapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude
        )
        db.collection("users").document(userId).update(userLocation as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(context, "Ubicación guardada en Firebase", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al guardar ubicación: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

@Composable
fun RequestLocationPermission(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Location) -> Unit
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                getLocation(context, fusedLocationClient, onLocationReceived)
            } else {
                Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getLocation(context, fusedLocationClient, onLocationReceived)
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}

private fun getLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Location) -> Unit
) {
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                onLocationReceived(it)
            } ?: run {
                Toast.makeText(context, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun MainScreen(cartItems: MutableList<Product>) {
    val navController = rememberNavController()
    val products = remember { mutableStateListOf<Product>() }
    var showBottomBar by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(Unit) {
        loadProductsFromFirebase(products)
    }

    // Request location permission and save location to Firebase
    RequestLocationPermission(
        context = context,
        fusedLocationClient = fusedLocationClient,
        onLocationReceived = { location ->
            saveLocationToFirebase(context, location)
        }
    )

    navController.addOnDestinationChangedListener { _, destination, _ ->
        showBottomBar = destination.route !in listOf("login", "register")
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController = navController)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "login",
            Modifier.padding(paddingValues)
        ) {
            composable("login") { LoginScreen(navController) }
            composable("register") { RegisterScreen(navController) }
            composable("products") { ProductsScreen(navController, products, cartItems) }
            composable("cart") { CartScreen(cartItems) }
            composable("profile") { ProfileScreen(navController) }
        }
    }
}

suspend fun loadProductsFromFirebase(products: MutableList<Product>) {
    val db = FirebaseFirestore.getInstance()
    try {
        val snapshot = db.collection("products").get().await()
        val loadedProducts = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Product::class.java)
        }
        products.clear()
        products.addAll(loadedProducts)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Configuración", "profile", icon = R.drawable.ic_settings),
        BottomNavItem("Productos", "products", icon = R.drawable.ic_products),
        BottomNavItem("Carrito", "cart", icon = R.drawable.ic_cart)
    )

    NavigationBar(containerColor = MaterialTheme.colorScheme.primary) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(painterResource(id = item.icon), contentDescription = item.label, tint = MaterialTheme.colorScheme.onPrimary) },
                label = { Text(item.label, color = MaterialTheme.colorScheme.onPrimary) },
                selected = navController.currentDestination?.route == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

data class BottomNavItem(val label: String, val route: String, val icon: Int)
