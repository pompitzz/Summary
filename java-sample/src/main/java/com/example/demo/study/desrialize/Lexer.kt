package com.example.demo.study.desrialize

import java.io.Reader

interface Token {
    object COMMA : Token
    object COLON : Token
    object LBRACE : Token
    object RBRACE : Token
    object LBRACKET : Token
    object RBRACKET : Token

    interface ValueToken : Token {
        val value: Any?
    }

    object NullValue : ValueToken {
        override val value: Any? = null
    }

    data class BoolValue(override val value: Boolean) : ValueToken
    data class StringValue(override val value: String) : ValueToken
    data class LongValue(override val value: Long) : ValueToken
    data class DoubleValue(override val value: Double) : ValueToken

    companion object {
        val TRUE = BoolValue(true)
        val FALSE = BoolValue(false)
    }
}

class Lexer(reader: Reader) {
    private val charReader = CharReader(reader)
    private val valueEndChars = setOf(',', '}', ']', ' ', '\t', '\r', '\n')

    private val tokenMap = hashMapOf<Char, (Char) -> Token>(
            ',' to { c -> Token.COMMA },
            '{' to { c -> Token.LBRACE },
            '}' to { c -> Token.RBRACE },
            '[' to { c -> Token.LBRACKET },
            ']' to { c -> Token.RBRACKET },
            ':' to { c -> Token.COLON },
            't' to { c -> charReader.expectText("rue", valueEndChars); Token.TRUE },
            'f' to { c -> charReader.expectText("alse", valueEndChars); Token.FALSE },
            'n' to { c -> charReader.expectText("ull", valueEndChars); Token.NullValue },
            '"' to { c -> readStringToken() },
            '-' to { c -> readNumberToken(c) }
    ).apply {
        for (i in '0'..'9') {
            this[i] = { c -> readNumberToken(c) }
        }
    }

    private fun readNumberToken(c: Char): Token {
        val builder = StringBuilder(c.toString())
        while (true) {
            val next = charReader.peekNext()
            if (next == null || c in valueEndChars) {
                break
            }
            builder.append(charReader.readNext()!!)
        }
        val value = builder.toString()
        return if(value.contains('.')) Token.DoubleValue(value.toDouble()) else Token.LongValue(value.toLong())
    }

    private fun readStringToken(): Token {
        val builder = StringBuilder()
        while (true) {
            val next = charReader.readNext() ?: throw IllegalArgumentException()
            if (next == '"') break
            if (next == '\\') {
                val escaped = charReader.readNext() ?: throw IllegalArgumentException()
                when(escaped) {
                    '\\', '/', '\"' -> builder.append(escaped)
                    'b' -> builder.append('\b')
                    'f' -> builder.append('\u000C')
                    'n' -> builder.append('\n')
                    'r' -> builder.append('\r')
                    't' -> builder.append('\t')
                    'u' -> {
                        val hexChars = charReader.readNextChars(4)
                        builder.append(Integer.parseInt(hexChars, 16).toChar())
                    }
                    else -> throw IllegalArgumentException("Unsupported escape sequence \\$escaped")
                }
            }
            else {
                builder.append(next)
            }
        }
        return Token.StringValue(builder.toString())
    }

    fun nextToken(): Token? {
        var c : Char?
        do {
            c = charReader.readNext()
        } while (c != null && c.isWhitespace())
        if (c == null) return null
        return tokenMap[c]?.invoke(c)
                ?: throw IllegalArgumentException()
    }
}

private class CharReader(val reader: Reader) {
    private var nextChar: Char? = null
    private var eof: Boolean = false
    private val charBuffer = CharArray(4)

    private fun advance() {
        if (eof) return

        val read = reader.read()
        if (read == -1) {
            eof = true
        } else {
            nextChar = read.toChar()
        }
    }

    fun readNext(): Char? = peekNext().apply { nextChar = null }

    fun peekNext(): Char? {
        if (nextChar == null) {
            advance()
        }
        return if (eof) null else nextChar
    }

    fun expectText(text: String, followedBy: Set<Char>) {
        if (readNextChars(text.length) != text) {
            throw IllegalArgumentException()
        }
        val next = peekNext()
        if (next != null && next !in followedBy) {
            throw IllegalArgumentException()
        }
    }

    fun readNextChars(length: Int): String {
        reader.read(charBuffer, 0, length)
        return String(charBuffer, 0, length)
    }
}
