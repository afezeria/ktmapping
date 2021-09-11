package com.github.afezeria.ktmapping.model

import com.github.afezeria.ktmapping.classDeclaration
import com.github.afezeria.ktmapping.defaultConstructor
import com.github.afezeria.ktmapping.property.Property
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.TypeName

sealed class ValidModel(
    /**
     * 变量名称
     */
    var varName: String,
    type: KSType,
    /**
     * 在函数中是否作为source存在
     */
    var isSource: Boolean = true,
    allowAsSource: Boolean,
    allowAsTarget: Boolean,
) : Model(type, allowAsSource, allowAsTarget) {

    var properties: List<Property> = emptyList()

    val constructor: KSFunctionDeclaration by lazy {
        type.classDeclaration.defaultConstructor
    }

    abstract fun getProperty(name: String): Property?

    abstract fun createSetterInvoke(propertyName: String): String

    abstract fun createTargetGetter(propertyName: String): GetterInvokeInfo

    abstract fun createSourceGetterInvokeChain(
        propertyNames: Collection<String>,
        targetType: KSType,
    ): GetterInvokeInfo

    protected fun String.wrap(typeName: TypeName? = null): String {
        return replace("(.*\\?:.*)".toRegex(), "(\$1)") + (typeName?.let { " as %T" } ?: "")
    }

    protected fun String.wrap(ksType: KSType? = null): String {
        return replace("(.*\\?:.*)".toRegex(), "(\$1)") + (ksType?.let { " as %T" } ?: "")
    }
}