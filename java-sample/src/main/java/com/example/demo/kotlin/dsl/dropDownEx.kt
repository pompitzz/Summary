package com.example.demo.kotlin.dsl

import kotlinx.html.*
import kotlinx.html.stream.createHTML

fun buildDropdown() = createHTML()
        .div(classes = "dropdown") {
            button(classes = "btn dropdown-toggle") {
                +"Dropdown"
                span(classes = "caret")
            }
            ul(classes = "dropdown-menu") {
                li { a("#") { +"Action" } }
                li { a("#") { +"Another action" } }
                li { role = "separator"; classes = setOf("divider") }
                li { classes = setOf("dropdown-header"); +"Header" }
                li { a("#") { +"Separated Link" } }
            }
        }


fun buildDropdown2() = createHTML()
        .div(classes = "dropdown") {
            button(classes = "btn dropdown-toggle") {
                +"Dropdown"
                span(classes = "caret")
            }
            // 코틀린 언어이므로 재사용이 가능하다.
            dropdownMenu {
                item("#", "Action")
                item("#", "Another action")
                divider()
                dropdownHeader("Header")
                item("#", "Separated Link")
            }
        }

fun UL.item(href: String, text: String) = li { a(href) { +text } }
fun UL.divider() = li { role = "separator"; classes = setOf("divider") }
fun UL.dropdownHeader(text: String) = li { classes = setOf("dropdown-header"); +text }
fun DIV.dropdownMenu(action: UL.() -> Unit) = ul(classes = "dropdown-menu", action)

