package com.github.afezeria.ktmapping.model

import com.github.afezeria.ktmapping.classDeclaration
import com.github.afezeria.ktmapping.defaultConstructor
import com.github.afezeria.ktmapping.property.Property
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType

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

}