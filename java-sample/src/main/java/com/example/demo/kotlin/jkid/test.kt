package com.example.demo.kotlin.jkid

data class Item(
        val name: String,
        @JsonName("somePrice") val price: Int,
        @JsonExclude val otherItem: Item?
)

fun main() {
    println(serialize(Item("item1", 10000, Item("item2", 10000, null))))
}
