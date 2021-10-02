package com.github.afezeria.ktmapping.property

import com.github.afezeria.ktmapping.*
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter

class ResultSetProperty(name: String) :
    Property(name, gresolver.getClassDeclarationByName<Any>()!!.asType(emptyList()), true, true) {

    override fun matchTargetParameter(param: KSValueParameter): Boolean {
        if (resultSetCanProvideTypeSet.contains(param.ksType.classDeclaration)) {
            return true
        }
        logger.error(
            "cannot match constructor, ${owner.type} cannot provide a value of the ${param.nameStr}(${param.ksType}) parameter",
            ctx.node
        )
        return false
    }

    override fun matchTargetProperty(property: Property): Boolean {
        if (resultSetCanProvideTypeSet.contains(property.type.classDeclaration)) {
            return true
        }
        logger.error(
            "Incompatible property type. ResultSet cannot provide a value of the ${property.name}(${property.type}) property",
            ctx.node
        )
        return false
    }
}