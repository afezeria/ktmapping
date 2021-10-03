package com.github.afezeria.ktmapping.model

import com.github.afezeria.ktmapping.MappingExt
import com.github.afezeria.ktmapping.UpdatePolicy
import com.github.afezeria.ktmapping.ctx
import com.github.afezeria.ktmapping.getMapValueType
import com.github.afezeria.ktmapping.property.MapProperty
import com.github.afezeria.ktmapping.property.Property
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Nullability

class MapModel(varName: String, type: KSType, isSource: Boolean) :
    ValidModel(varName, type, isSource, true, false) {

    var valueType = getMapValueType(type)!!

    override fun getProperty(name: String): Property {
        return MapProperty(name, valueType)
    }

    override fun createSetterInvoke(propertyName: String): String {
        return "$varName[\"$propertyName\"] = "
    }

    override fun createTargetGetter(propertyName: String): GetterInvokeInfo {
        throw NotImplementedError()
    }

    override fun createSourceGetterInvokeChain(
        propertyNames: Collection<String>,
        targetType: KSType,
    ): GetterInvokeInfo {

        ctx.fileSpecBuilder.addImport(MappingExt::class, "_get")
        ctx.fileSpecBuilder.addImport(MappingExt::class, "_getNullable")

        var isNullable = true
        if (targetType.nullability == Nullability.NOT_NULL) {
            isNullable = false
        }
        if (ctx.isUpdateFunction && ctx.updatePolicy == UpdatePolicy.SOURCE_IS_NOT_NULL) {
            isNullable = true
        }
        val str = if (isNullable) {
            "_getNullable($varName, "
        } else {
            "_get($varName, "
        } + propertyNames.joinToString(", ") { "\"$it\"" } + ")"

        return GetterInvokeInfo(str, emptyList(), isNullable)
    }

}