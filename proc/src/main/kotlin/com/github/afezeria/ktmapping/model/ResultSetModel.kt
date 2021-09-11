package com.github.afezeria.ktmapping.model

import com.github.afezeria.ktmapping.MappingExt
import com.github.afezeria.ktmapping.UpdatePolicy
import com.github.afezeria.ktmapping.ctx
import com.github.afezeria.ktmapping.property.Property
import com.github.afezeria.ktmapping.property.ResultSetProperty
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Nullability

class ResultSetModel(varName: String, type: KSType, isSource: Boolean) :
    ValidModel(varName, type, isSource, true, false) {

    override fun getProperty(name: String): Property? {
        return ResultSetProperty(name)
    }

    override fun createSetterInvoke(propertyName: String): String {
        throw NotImplementedError()
    }

    override fun createTargetGetter(propertyName: String): GetterInvokeInfo {
        throw NotImplementedError()
    }

    private fun String.wrap(isNullable: Boolean) =
        if (isNullable) this else "requireNotNull($this)"

    override fun createSourceGetterInvokeChain(
        propertyNames: Collection<String>,
        targetType: KSType,
    ): GetterInvokeInfo {

        ctx.fileSpecBuilder.addImport(MappingExt::class, "_get")

        var isNullable = true
        if (targetType.nullability == Nullability.NOT_NULL) {
            isNullable = false
        }
        if (ctx.isUpdateFunction && ctx.updatePolicy == UpdatePolicy.SOURCE_IS_NOT_NULL) {
            isNullable = true
        }
        val str = propertyNames.joinToString(" ?: ") {
            "_get($varName, \"$it\")"
        }.wrap(isNullable)
        return GetterInvokeInfo(str, emptyList(), isNullable)
    }

}