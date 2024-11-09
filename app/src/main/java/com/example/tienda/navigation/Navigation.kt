// Navigation.kt
package com.example.tienda

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.tienda.screens.cart.CartScreen
import com.example.tienda.screens.products.ProductsScreen
import com.example.tienda.screens.register.RegisterScreen
import com.example.tienda.screens.login.LoginScreen
import com.example.tienda.models.Product
import com.google.firebase.auth.FirebaseAuth

val auth = FirebaseAuth.getInstance()
val startDestination = if (auth.currentUser != null) "products" else "login"

@Composable
fun AppNavigation(navController: NavHostController, products: List<Product>) {
    NavHost(
        navController = navController,
        startDestination = "login"  // Asegúrate de que la pantalla inicial sea la de login
    ) {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("products") { ProductsScreen(navController, products) }
        composable("cart") { CartScreen() }
    }
}
