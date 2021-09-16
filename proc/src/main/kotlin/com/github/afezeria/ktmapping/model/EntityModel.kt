package com.github.afezeria.ktmapping.model

import com.github.afezeria.ktmapping.*
import com.github.afezeria.ktmapping.property.EntityProperty
import com.github.afezeria.ktmapping.property.Property
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Origin
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import java.lang.reflect.Field

class EntityModel(varName: String, type: KSType, isSource: Boolean) :
    ValidModel(varName, type, isSource, true, true) {

    init {
        type.classDeclaration.apply {
            properties = when (origin) {
                Origin.KOTLIN, Origin.KOTLIN_LIB -> {
                    getAllProperties()
                        .map {
                            EntityProperty(
                                name = it.simpleName.asString(),
                                type = it.type.resolve(),
                                isNullable = it.isNullable(),
                                hasGetter = it.getter != null,
                                hasSetter = it.setter != null,
                                isLateinit = it.isLateinit(),
                            )
                        }
                }
                Origin.JAVA, Origin.JAVA_LIB -> {
                    getAllProperties()
                        .mapNotNull {
                            val name = it.simpleName.asString()
                            val propType = it.type.resolve()
                            var hasGetter = false
                            var hasSetter = false
                            getAllFunctions()
                                .filter(KSDeclaration::isPublic)
                                .forEach { method ->
                                    when (method.simpleName.asString()) {
                                        "get${pascalCase(name)}" -> {
                                            if (method.returnType!!.resolve() == propType) {
                                                hasGetter = true
                                            }
                                        }
                                        "set${pascalCase(name)}" -> {
                                            if (method.parameters.isNotEmpty() && method.parameters[0].type.resolve() == propType) {
                                                hasSetter = true
                                            }
                                        }
                                    }
                                }
                            if (hasGetter || hasSetter) {
                                EntityProperty(
                                    name = name,
                                    type = propType.makeNullable(),
                                    isNullable = true,
                                    hasGetter = hasGetter,
                                    hasSetter = hasSetter,
                                    isLateinit = false
                                )
                            } else {
                                null
                            }
                        }
                }
                Origin.SYNTHETIC -> throw IllegalStateException()
            }.toList()
        }
    }

    override fun getProperty(name: String): Property? {
        return properties.find { it.name == name }
    }

    override fun createSetterInvoke(propertyName: String): String {
        return "$varName.$propertyName = "
    }

    override fun createTargetGetter(propertyName: String): GetterInvokeInfo {
        properties.find { it.name == propertyName }!!
            .run {
                return when {
                    isLateinit ->
                        GetterInvokeInfo(
                            str = createGetterWithReflect(name),
                            explicitType = emptyList(),
                            isNullable = true
                        )
                    else -> GetterInvokeInfo("$varName.$name", emptyList(), isNullable)
                }
            }
    }

    override fun createSourceGetterInvokeChain(
        propertyNames: Collection<String>,
        targetType: KSType,
    ): GetterInvokeInfo {
        //因为实体类映射到实体类的时候要求是source必须能够转换成target
        //所以实际上只有当函数为更新函数且update策略为(SOURCE_IS_NOT_NULL,TARGET_IS_NULL)
        //且source列表中包含lateinit字段时explicitType才不为null，且此时类型必定为nullable
        var explicitType: KSType? = null
        var isNullable = false
        val helper: (Property) -> String =
            if (ctx.isUpdateFunction && ctx.updatePolicy == UpdatePolicy.SOURCE_IS_NOT_NULL) {
                { p ->
                    if (p.isLateinit) {
                        explicitType = targetType.makeNullable()
                        isNullable = true
                        createGetterWithReflect(p.name)
                    } else {
                        if (p.isNullable) {
                            isNullable = true
                        }
                        "$varName.${p.name}"
                    }
                }
            } else if (ctx.mappingPolicy == MappingPolicy.FIRST_NOT_NULL) {
                { p ->
                    if (p.isLateinit) {
                        if (!targetType.isMarkedNullable) {
                            explicitType = targetType
                        }
                        createGetterWithReflect(p.name)
                    } else {
                        if (p.isNullable && !targetType.isMarkedNullable) {
                            explicitType = targetType
                        }
                        "$varName.${p.name}"
                    }
                }
            } else {
                { p ->
                    isNullable = isNullable || p.isNullable
                    "$varName.${p.name}"
                }
            }
        val str = properties.filter { it.name in propertyNames }
            .joinToString(" ?: ") {
                helper(it)
            }.wrap(explicitType)
        return GetterInvokeInfo(str, listOfNotNull(explicitType?.className()), isNullable)
    }

    private fun String.wrap(ksType: KSType? = null): String {
        return replace("(.*\\?:.*)".toRegex(), "(\$1)") + (ksType?.let { " as %T" } ?: "")
    }

    private fun createGetterWithReflect(propertyName: String): String {
        val name = type.qualifierName.replace('.', '_') + '_' + propertyName
        ctx.reflectHelperProperties.getOrPut(name) {
            PropertySpec.builder(name, Field::class)
                .addModifiers(KModifier.PRIVATE)
                .initializer("%T::class.java.getField(\"$propertyName\")", type.className())
                .build()
        }
        return "$name.get($varName)"
    }

}