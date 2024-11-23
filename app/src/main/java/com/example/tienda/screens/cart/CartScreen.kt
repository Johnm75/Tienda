package com.example.tienda.screens.cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import com.example.tienda.models.Product
import com.example.tienda.R

@Composable
fun CartScreen(initialCartItems: List<Product>) {

    var cartItems by remember { mutableStateOf(initialCartItems.toMutableList()) }

  
    val total = cartItems.sumOf { it.price }


    var showWebView by remember { mutableStateOf(false) }

    if (showWebView) {

        PayPalWebView(
            total = total,
            businessEmail = "john.fredy.marciales@gmail.com",
            onPaymentComplete = {
                showWebView = false

            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Carrito de Compras",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground // Color de texto según el esquema
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
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showWebView = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Pagar con PayPal")
            }
        }
    }
}

@Composable
fun PayPalWebView(total: Double, businessEmail: String, onPaymentComplete: () -> Unit) {

    val paypalHtml = generatePaypalFormHtml(total, businessEmail)

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {

                        if (url != null && url.contains("success")) {
                            onPaymentComplete()
                        }
                    }
                }
                settings.javaScriptEnabled = true
                loadDataWithBaseURL(null, paypalHtml, "text/html", "UTF-8", null)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}


fun generatePaypalFormHtml(total: Double, businessEmail: String): String {
    return """
        <html>
        <body onload="document.forms['paypalForm'].submit();">
            <form id="paypalForm" action="https://www.paypal.com/cgi-bin/webscr" method="post">
                <input type="hidden" name="cmd" value="_xclick">
                <input type="hidden" name="business" value="$businessEmail">
                <input type="hidden" name="item_name" value="Compra en Tienda App">
                <input type="hidden" name="amount" value="$total">
                <input type="hidden" name="currency_code" value="USD">
                <button type="submit">Pagar con PayPal</button>
            </form>
        </body>
        </html>
    """
}

@Composable
fun CartItemRow(product: Product, onRemoveItem: () -> Unit) {
    val imageResourceId = when (product.imageUrl) {
        "producto1" -> R.drawable.producto1
        "producto2" -> R.drawable.producto2
        "producto3" -> R.drawable.producto3
        "producto4" -> R.drawable.producto4
        else -> R.drawable.ic_default_profile
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Image(
            painter = painterResource(id = imageResourceId),
            contentDescription = product.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(50.dp)
                .padding(8.dp)
        )


        Text(
            text = product.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )


        Text(
            text = "$${product.price}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )


        IconButton(
            onClick = onRemoveItem,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_delete),
                contentDescription = "Eliminar",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
