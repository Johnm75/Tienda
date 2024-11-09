package com.example.tienda.viewmodels

import androidx.lifecycle.ViewModel
import com.example.tienda.models.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class CartViewModel : ViewModel() {
    private val _cartItems = MutableStateFlow<List<Product>>(emptyList())
    val cartItems: StateFlow<List<Product>> = _cartItems

    fun addProductToCart(product: Product) {
        _cartItems.update { currentItems ->
            currentItems + product
        }
    }

    fun calculateTotal(): Double {
        return _cartItems.value.sumOf { it.price }
    }

    fun clearCart() {
        _cartItems.update { emptyList() }
    }
}
