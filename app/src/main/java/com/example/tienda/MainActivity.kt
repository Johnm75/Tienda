// MainActivity.kt
package com.example.tienda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalPagerApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TiendaTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val products = remember { mutableStateListOf<Product>() }
    var showBottomBar by remember { mutableStateOf(false) }

    // Cargar productos desde Firebase al iniciar la pantalla
    LaunchedEffect(Unit) {
        loadProductsFromFirebase(products)
    }

    // Escuchar cambios en la ruta actual
    navController.addOnDestinationChangedListener { _, destination, _ ->
        showBottomBar = destination.route !in listOf("login", "register")
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "login",  // Asegurarse de que la pantalla inicial sea "login"
            Modifier.padding(paddingValues)
        ) {
            composable("login") { LoginScreen(navController) }
            composable("register") { RegisterScreen(navController) }
            composable("products") { ProductsScreen(navController, products) }
            composable("cart") { CartScreen() }
            composable("profile") { ProfileScreen(navController) }

        }
    }
}




// Función para cargar los productos desde Firebase
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
        e.printStackTrace() // Maneja el error de forma adecuada, por ejemplo, mostrando un mensaje de error en la UI
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Configuración", "profile", icon = R.drawable.ic_settings),
        BottomNavItem("Productos", "products", icon = R.drawable.ic_products),
        BottomNavItem("Carrito", "cart", icon = R.drawable.ic_cart)
    )



    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(painterResource(id = item.icon), contentDescription = item.label) },
                label = { Text(item.label) },
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
