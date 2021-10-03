package com.github.afezeria.ktmapping

import com.github.afezeria.ktmapping.model.EntityModel
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName

/**
 *
 * @date 2021/8/16
 */
class BuildFunction private constructor(private val fn: KSFunctionDeclaration) {
    var builder: FunSpec.Builder = FunSpec.builder(fn.simpleName.asString())
        .addModifiers(KModifier.OVERRIDE)
        .returns(fn.returnType!!.className())
        .also { spec ->
            fn.parameters.forEach {
                spec.addParameter(ParameterSpec(it.nameStr, it.type.className()))
            }
        }

    fun build(): FunSpec {
        return if (ctx.isUpdateFunction) {
            buildUpdateFunction()
        } else {
            buildMappingFunction()
        }
    }

    private fun buildMappingFunction(): FunSpec {
        val types: MutableList<TypeName?> = mutableListOf(ctx.targetModel.type.className(false))
        val constructorStr =
            ctx.targetModel.constructor.parameters.joinToString { parameter ->
                val (str, type, _) = ctx.sourceModel.createSourceGetterInvokeChain(
                    ctx.target2Sources[parameter.nameStr]!!,
                    parameter.ksType
                )
                types += type
                str
            }.let {
                "val result = %T($it)"
            }
        builder.addStatement(constructorStr, *types.filterNotNull().toTypedArray())

        ctx.target2Sources
            .map { ctx.targetModel.getProperty(it.key)!! }
            .filter { it.hasSetter }
            .forEach { property ->
                val setterStr = ctx.targetModel.createSetterInvoke(property.name)
                val (getterStr, type, _) = ctx.sourceModel.createSourceGetterInvokeChain(
                    ctx.target2Sources[property.name]!!,
                    property.type
                )
                builder.addStatement(setterStr + getterStr, *type.toTypedArray())
            }
        builder.addStatement("return result")
        return builder.build()
    }

    private fun buildUpdateFunction(): FunSpec {
        ctx.target2Sources
            .map { ctx.targetModel.getProperty(it.key)!! }
            .filter { it.hasSetter }
            .forEach { property ->
                val setterStr = ctx.targetModel.createSetterInvoke(property.name)
                val (getterStr, types, isNullable) =
                    ctx.sourceModel.createSourceGetterInvokeChain(
                        ctx.target2Sources[property.name]!!,
                        property.type
                    )
                when (ctx.updatePolicy) {
                    UpdatePolicy.UPDATE_ALL -> builder.addStatement(setterStr + getterStr,
                        *types.toTypedArray())
                    UpdatePolicy.TARGET_IS_NULL -> {
                        val (targetGetterStr, _, nullable) = ctx.targetModel.createTargetGetter(
                            property.name)
                        if (nullable) {
                            val tmpVarName = property.name + "Tmp"
                            builder.addStatement("val $tmpVarName = $targetGetterStr",
                                property.type.makeNullable().className())
                                .beginControlFlow("if ($tmpVarName == null)")
                                .addStatement(setterStr + getterStr, *types.toTypedArray())
                                .endControlFlow()
                        } else {
                            builder.addStatement(setterStr + getterStr, *types.toTypedArray())
                        }
                    }
                    UpdatePolicy.SOURCE_IS_NOT_NULL -> {
                        val tmpVarName = property.name + "Tmp"

                        if (isNullable) {
                            if (ctx.sourceModel is EntityModel) {
                                builder.addStatement("val $tmpVarName = $getterStr",
                                    *types.toTypedArray())
                            } else {
                                builder.addStatement("val $tmpVarName: %T = $getterStr",
                                    property.type.className(true))
                            }
                                .beginControlFlow("if ($tmpVarName != null)")
                                .addStatement(setterStr + tmpVarName, *types.toTypedArray())
                                .endControlFlow()
                        } else {
                            builder.addStatement(setterStr + getterStr, *types.toTypedArray())
                        }
                    }
                }

            }

        return builder.build()
    }

    companion object {
        operator fun invoke(fn: KSFunctionDeclaration): FunSpec? {
            return Context(fn) {
                if (!FunctionValidation(fn)) {
                    return@Context null
                }
                BuildFunction(fn).build()
            }
        }
    }
}