package com.example.tienda.screens.cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.tienda.models.Product
import coil.compose.rememberImagePainter
import com.example.tienda.R

@Composable
fun CartScreen(initialCartItems: List<Product>) {
    // Estado mutable para la lista de productos en el carrito
    var cartItems by remember { mutableStateOf(initialCartItems.toMutableList()) }

    // Estado para el total
    val total = cartItems.sumOf { it.price }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Carrito de Compras",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        cartItems.forEach { product ->
            CartItemRow(product = product, onRemoveItem = {
                cartItems = cartItems.toMutableList().apply { remove(product) }
            })
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Total: $$total",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.End)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* Lógica para realizar el pago */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pagar")
        }
    }
}

@Composable
fun CartItemRow(product: Product, onRemoveItem: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Imagen del producto a la izquierda
        Image(
            painter = rememberImagePainter(data = product.imageUrl),
            contentDescription = product.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(50.dp)
                .padding(8.dp)
        )

        // Nombre del producto en el centro
        Text(
            text = product.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        // Precio del producto a la derecha
        Text(
            text = "$${product.price}",
            style = MaterialTheme.typography.bodyLarge
        )

        // Botón para eliminar el producto
        IconButton(onClick = onRemoveItem) {
            Icon(
                painter = rememberImagePainter(data = R.drawable.ic_delete), // Reemplaza con un ícono de eliminación
                contentDescription = "Eliminar",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
