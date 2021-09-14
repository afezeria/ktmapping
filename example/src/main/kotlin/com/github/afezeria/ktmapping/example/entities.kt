package com.github.afezeria.ktmapping.example

import java.time.LocalDateTime

class A(
    val valString: String,
    val valInt: Int,
    var varString: String,
    val valNullString: String? = null,
) {
    var e: String? = null
    lateinit var lateinitLocalDateTime: LocalDateTime
}