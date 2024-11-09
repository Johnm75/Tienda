package com.example.tienda.screens.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.tienda.models.Product
import com.google.accompanist.pager.*
import androidx.navigation.NavController
import coil.request.CachePolicy
import coil.request.ImageRequest

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ProductsScreen(navController: NavController, products: List<Product>) {
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
            ProductCard(product = products[page], navController = navController)
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
fun ProductCard(product: Product, navController: NavController) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(750.dp) // Ajusta la altura para hacer la tarjeta más grande
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(product.imageUrl)
                    .diskCachePolicy(CachePolicy.DISABLED) // Desactiva el caché de disco
                    .memoryCachePolicy(CachePolicy.DISABLED) // Desactiva el caché en memoria
                    .build(),
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

            Button(onClick = { /* Agregar al carrito */ }) {
                Text("Añadir al carrito")
            }
        }
    }
}

