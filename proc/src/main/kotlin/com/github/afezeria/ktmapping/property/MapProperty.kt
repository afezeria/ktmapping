package com.github.afezeria.ktmapping.property

import com.github.afezeria.ktmapping.ctx
import com.github.afezeria.ktmapping.ksType
import com.github.afezeria.ktmapping.logger
import com.github.afezeria.ktmapping.nameStr
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter

class MapProperty(name: String, type: KSType) : Property(name, type, true, true, true, false) {
    override fun isCompatibleType(ksType: KSType): Boolean {
        return type.isAssignableFrom(ksType)
    }

    override fun matchTargetParameter(param: KSValueParameter): Boolean {
        if (type.isAssignableFrom(param.ksType)) {
            return true
        }
        logger.error(
            "cannot match constructor, ${owner.type} cannot provide a value of the ${param.nameStr}(${param.ksType}) parameter",
            ctx.node
        )
        return false
    }

    override fun matchTargetProperty(property: Property): Boolean {
        if (type.isAssignableFrom(property.type)) {
            return true
        }
        logger.error(
            "Incompatible property type. ${owner.type} cannot provide a value of the ${property.name}(${property.type}) property",
            ctx.node
        )
        return false
    }
}