package com.example.tienda.screens.products

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.tienda.models.Product
import com.google.accompanist.pager.*
import androidx.navigation.NavController
import com.example.tienda.R

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ProductsScreen(navController: NavController, products: List<Product>, cartItems: MutableList<Product>) {
    val pagerState = rememberPagerState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 56.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            count = products.size,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            ProductCard(product = products[page], navController = navController, cartItems = cartItems)
        }

        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
        )
    }
}

@Composable
fun ProductCard(product: Product, navController: NavController, cartItems: MutableList<Product>) {
    val context = LocalContext.current

    // Obtener el recurso de imagen en base al campo `imageUrl` del producto
    val imageResourceId = when (product.imageUrl) {
        "producto1" -> R.drawable.producto1
        "producto2" -> R.drawable.producto2
        "producto3" -> R.drawable.producto3
        "producto4" -> R.drawable.producto4
        else -> R.drawable.ic_default_profile // Imagen por defecto si no se encuentra
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(750.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Cargar imagen desde recursos locales
            Image(
                painter = painterResource(id = imageResourceId),
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(400.dp)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "$${product.price}",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                cartItems.add(product)
                Toast.makeText(context, "${product.name} añadido al carrito", Toast.LENGTH_SHORT).show()
            }) {
                Text("Añadir al carrito")
            }
        }
    }
}
