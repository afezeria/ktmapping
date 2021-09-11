package com.github.afezeria.ktmapping.property

import com.github.afezeria.ktmapping.model.ValidModel
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter

sealed class Property(
    var name: String,
    var type: KSType,
    var isNullable: Boolean = false,
    var hasGetter: Boolean = false,
    var hasSetter: Boolean = false,
    var isLateinit: Boolean = false,
) {
    lateinit var owner: ValidModel
    abstract fun isCompatibleType(ksType: KSType): Boolean
    abstract fun matchTargetParameter(param: KSValueParameter): Boolean
    abstract fun matchTargetProperty(property: Property): Boolean
    override fun toString(): String {
        return "Property(name='$name', type=$type, isNullable=$isNullable, hasGetter=$hasGetter, hasSetter=$hasSetter, isLateinit=$isLateinit)"
    }

}

