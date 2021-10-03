package com.github.afezeria.ktmapping.model

import com.squareup.kotlinpoet.TypeName

data class GetterInvokeInfo(
    val str: String,
    val explicitType: List<TypeName>,
    val isNullable: Boolean,
)