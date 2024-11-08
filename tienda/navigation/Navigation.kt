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
        composable("login") { LoginScreen() }
        composable("register") { RegisterScreen() }
        composable("cart") { CartScreen() }
        composable("products") { ProductsScreen() }
        // Agrega más rutas según sea necesario
    }
}
