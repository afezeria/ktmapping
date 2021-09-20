package com.github.afezeria.ktmapping.property

import com.github.afezeria.ktmapping.ctx
import com.github.afezeria.ktmapping.ksType
import com.github.afezeria.ktmapping.logger
import com.github.afezeria.ktmapping.nameStr
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter

class EntityProperty(
    name: String,
    type: KSType,
    isNullable: Boolean,
    hasGetter: Boolean,
    hasSetter: Boolean,
    isLateinit: Boolean,
) : Property(name, type, isNullable, hasGetter, hasSetter, isLateinit) {
    override fun isCompatibleType(ksType: KSType): Boolean {
        return ksType.isAssignableFrom(type)
    }

    override fun matchTargetParameter(param: KSValueParameter): Boolean {
        if (param.ksType.isAssignableFrom(type)) {
            return true
        }
        logger.error(
            "cannot match constructor, incompatible types, parameter:${param.nameStr}, source property:${name}",
            ctx.node
        )
        return false
    }

    override fun matchTargetProperty(property: Property): Boolean {
        if (property.type.isAssignableFrom(type)) {
            return true
        }
        logger.error(
            "Incompatible property type. source: ${owner.type}.${name}, target: ${property.owner.type}.${property.name}",
            ctx.node
        )
        return false
    }

}