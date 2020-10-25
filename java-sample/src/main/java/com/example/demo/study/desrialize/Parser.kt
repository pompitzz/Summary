package com.example.demo.study.desrialize

import java.io.Reader

class Parser(reader: Reader) {
    private val lexer = Lexer(reader)

    fun parse(rootObject: JsonObject) {
        expect(Token.LBRACE)
        parseObject(rootObject)
        if (lexer.nextToken() != null) {
            throw IllegalArgumentException()
        }
    }

    private fun parseObject(jsonObject: JsonObject) {
        parseCommaSeparated(Token.RBRACE) { token ->
            if (token !is Token.StringValue) {
                throw IllegalArgumentException()
            }
            val propertyName = token.value
            expect(Token.COLON)
            parsePropertyValue(jsonObject, propertyName, nextToken())
        }
    }

    private fun parsePropertyValue(jsonObject: JsonObject, propertyName: String, nextToken: Token) {
        when (nextToken) {
            is Token.ValueToken -> jsonObject.setSimpleProperty(propertyName, nextToken.value)
            Token.LBRACE -> parseObject(jsonObject.createJsonObject(propertyName))
            Token.LBRACKET -> parserArray(jsonObject.createJsonArray(propertyName), propertyName)
        }
    }

    private fun parserArray(arrayJsonObject: JsonObject, propertyName: String) {
        parseCommaSeparated(Token.RBRACKET) {
            parsePropertyValue(arrayJsonObject, propertyName, it)
        }
    }

    private fun parseCommaSeparated(stopToken: Token, body: (Token) -> Unit) {
        var expectComma = false
        while (true) {
            var nextToken = nextToken()
            if (nextToken == stopToken) break
            if (expectComma) {
                if (nextToken != Token.COMMA) throw IllegalArgumentException()
                nextToken = nextToken()
            }

            body(nextToken)
            expectComma = true
        }
    }

    private fun expect(token: Token) {
        if (lexer.nextToken() != token) {
            throw IllegalArgumentException()
        }
    }

    private fun nextToken() = lexer.nextToken() ?: throw IllegalArgumentException()
}
