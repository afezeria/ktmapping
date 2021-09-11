package com.github.afezeria.ktmapping

/**
 *
 * @date 2021/8/4
 */
private val group = Regex("[a-z]+|[0-9]+|[A-Z][a-z]+|[A-Z]++(?![a-z])|[A-Z]")
fun camelCase(name: String): String {
    return group.findAll(name)
        .joinToString("") { it.value.lowercase().replaceFirstChar { it.uppercaseChar() } }
        .replaceFirstChar { it.lowercase() }
}

fun constantCase(name: String): String {
    return group.findAll(name)
        .joinToString("_") { it.value.uppercase() }
}

fun dashCase(name: String): String {
    return group.findAll(name)
        .joinToString("-") { it.value.lowercase() }
}

fun httpHeaderCase(name: String): String {
    return group.findAll(name)
        .joinToString("-") { it.value.lowercase().replaceFirstChar { it.uppercaseChar() } }
}

fun pascalCase(name: String): String {
    return group.findAll(name)
        .joinToString("") { it.value.lowercase().replaceFirstChar { it.uppercaseChar() } }
}

fun snakeCase(name: String): String {
    return group.findAll(name)
        .joinToString("_") { it.value.lowercase() }
}

fun trainCase(name: String): String {
    return group.findAll(name)
        .joinToString("-") { it.value.uppercase() }
}

operator fun NamingStyle.invoke(name: String) = when (this) {
    NamingStyle.CAMEL_CASE -> ::camelCase
    NamingStyle.CONSTANT_CASE -> ::constantCase
    NamingStyle.DASH_CASE -> ::dashCase
    NamingStyle.HTTP_HEADER_CASE -> ::httpHeaderCase
    NamingStyle.PASCAL_CASE -> ::pascalCase
    NamingStyle.SNAKE_CASE -> ::snakeCase
    NamingStyle.TRAIN_CASE -> ::trainCase
}.invoke(name)