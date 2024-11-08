// Navigation.kt
package com.example.tienda.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.tienda.screens.login.LoginScreen
import com.example.tienda.screens.register.RegisterScreen
import com.example.tienda.screens.cart.CartScreen
import com.example.tienda.screens.products.ProductsScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {

        // Pantalla de inicio de sesión
        composable("login") {
            LoginScreen(navController = navController)
        }

        // Pantalla de registro
        composable("register") {
            RegisterScreen(navController = navController)
        }

        // Pantalla de carrito
        composable("cart") {
            CartScreen(navController = navController)
        }

        // Pantalla de productos
        composable("products") {
            ProductsScreen(navController = navController)
        }



        // Agrega más pantallas aquí según sea necesario
    }
}
