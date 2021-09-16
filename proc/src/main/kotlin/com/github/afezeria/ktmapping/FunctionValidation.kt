package com.github.afezeria.ktmapping

import com.github.afezeria.ktmapping.model.MapModel
import com.github.afezeria.ktmapping.model.Model
import com.github.afezeria.ktmapping.model.ResultSetModel
import com.github.afezeria.ktmapping.model.ValidModel
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

/**
 *
 * @date 2021/8/13
 */
class FunctionValidation private constructor(val fn: KSFunctionDeclaration) {
    private var explicitMappings: MutableList<Mapping> = mutableListOf()
    private var isUpdateFunction: Boolean = false
    private lateinit var targetModel: ValidModel
    private lateinit var sourceModel: ValidModel

    fun test(): Boolean {
        return isDefault()
                && signatureCheck()
                && checkType()
                && targetHavePublicConstructor()
                && extractExplicitMapping()
                && mappingCheck()
    }

    /**
     * 跳过默认函数
     * @return Boolean
     */
    private fun isDefault(): Boolean {
        if (!fn.isAbstract) {
            logger.info("Ignore default function:${fn.simpleName.asString()}", fn)
            return false
        }
        return true
    }

    private fun signatureCheck(): Boolean {
        ctx.isUpdateFunction = when (fn.parameters.size) {
            1 -> false
            2 -> true
            else -> {
                logger.error("The number of function arguments must be 1 or 2", fn)
                return false
            }
        }
        return true
    }


    /**
     * 检查转换函数的target的类型
     * target必须为用户自定义类型
     * @return Boolean
     */
    private fun checkType(): Boolean {
        val sourceM = Model(fn.parameters[0].type.resolve(), fn.parameters[0].nameStr, true)
        val targetM = if (ctx.isUpdateFunction) {
            Model(fn.parameters[1].type.resolve(), fn.parameters[1].nameStr, false)
        } else {
            Model(fn.returnType!!.resolve(), "result", false)
        }
        val sourceError = sourceM.valid()?.apply { logger.error(this, fn) }
        val targetError = targetM.valid()?.apply { logger.error(this, fn) }
        if (sourceError != null || targetError != null) {
            return false
        }
        if (ctx.isUpdateFunction &&
            !fn.returnType!!.isType<Unit>()
        ) {
            logger.error("The return value of the update function must be unit", fn)
            return false
        }
        sourceModel = sourceM as ValidModel
        targetModel = targetM as ValidModel
        ctx.sourceModel = sourceModel
        ctx.targetModel = targetModel
        return true
    }

    /**
     * 检查target是否有公开的构造函数
     * @return Boolean
     */
    private fun targetHavePublicConstructor(): Boolean {
        if (ctx.isUpdateFunction) {
            //更新函数不检查构造器
            return true
        }
        if (!targetModel.constructor.isPublic()) {
            logger.error("${targetModel.type.name} is missing a public constructor", fn)
            return false
        }
        return true
    }

    /**
     * 检查使用Mapping指定的显示映射并存储结果
     * @return Boolean
     */
    private fun extractExplicitMapping(): Boolean {
        var result = true
        for (ksAnnotation in fn.annotations) {
            val mapping = ksAnnotation.toAnnotation<Mapping>() ?: continue
            if (mapping.target.isBlank()) {
                logger.error("Mapping.target should not be blank", ksAnnotation)
                result = false
                continue
            }
            if (mapping.source.isBlank()) {
                logger.error("Mapping.source should not be blank", ksAnnotation)
                result = false
                continue
            }
            //检查显示声明的target属性是否存在

            if (!targetModel.properties.any { it.hasSetter && it.name == mapping.target }
                && (ctx.isUpdateFunction ||
                        targetModel.constructor.parameters.all { it.nameStr != mapping.target })) {
                logger.error(
                    "Missing ${mapping.target} property or constructor parameter in ${targetModel.type}",
                    ksAnnotation
                )
                result = false
                continue
            }


            //检查显示声明的source属性是否存在
            if (sourceModel !is MapModel
                && sourceModel !is ResultSetModel
                && !sourceModel.properties.any { it.hasGetter && it.name == mapping.source }
            ) {
                logger.error(
                    "Missing property ${mapping.source} in ${sourceModel.type.name}",
                    ksAnnotation
                )
                result = false
                continue
            }
            explicitMappings += mapping
        }
        return result
    }

    private fun mappingCheck(): Boolean {
        setMapping()
        if (!excludeMapping()) {
            return false
        }
        if (!ctx.isUpdateFunction && !constructorParameterCheck()) {
            return false
        }
        return propertyCheck()
    }

    /**
     * 从映射关系中排除指定属性
     * @return Boolean
     */
    private fun excludeMapping(): Boolean {
        var result = true

        fn.getAnnotations<ExcludeMapping>().firstOrNull()?.let {
            for (sourceMapping in it.sourceMappings) {
                var exist = false
                ctx.target2Sources.entries.removeIf { (_, set) ->
                    exist = exist || set.remove(sourceMapping)
                    set.isEmpty()
                }
                if (!exist) {
                    logger.error("Missing target mapping:$sourceMapping", fn)
                    result = false
                }
            }
            for (targetMapping in it.targetMappings) {
                ctx.target2Sources.remove(targetMapping) ?: run {
                    logger.error("Missing source mapping:$targetMapping", fn)
                    result = false
                }
            }
        }
        return result
    }

    /**
     * 检查是否满足构造器属性要求
     * @return Boolean
     */
    private fun constructorParameterCheck(): Boolean {
        val constructorParameters = targetModel.constructor.parameters
        var res = true
        constructorParameters.forEach {
            ctx.target2Sources[it.nameStr]?.run {
                forEach { name ->
                    if (!sourceModel.getProperty(name)!!.matchTargetParameter(it)) {
                        res = false
                    }
                }
            } ?: run {
                logger.error(
                    "cannot match constructor, missing attribute ${it.nameStr} in ${sourceModel.type.name}",
                    fn
                )
                res = false
            }
        }
        return res
    }

    private fun propertyCheck(): Boolean {
        var res = true
        for ((targetPropName, sourcePropNames) in ctx.target2Sources) {
            //target2Sources中key除了属性还包括构造器参数，返回null时targetProp应该是构造器参数名称，直接跳过
            val targetProp = targetModel.getProperty(targetPropName) ?: continue
            sourcePropNames.forEach { name ->
                if (!sourceModel.getProperty(name)!!.matchTargetProperty(targetProp)) {
                    res = false
                }
            }
        }
        return res
    }

    /**
     * 设置映射关系
     */
    private fun setMapping() {
        val target2SourceSet: MutableMap<String, MutableSet<String>>
        if (sourceModel is MapModel || sourceModel is ResultSetModel) {
            target2SourceSet = targetModel.properties
                .filter { it.hasSetter }.associateTo(mutableMapOf()) {
                    it.name to ctx.sourceNameStyle.mapTo(LinkedHashSet()) { style ->
                        style(it.name)
                    }
                }
            if (!ctx.isUpdateFunction) {
                targetModel.constructor.parameters.forEach {
                    target2SourceSet.getOrPut(it.name!!.asString()) { LinkedHashSet() }
                        .apply {
                            ctx.sourceNameStyle.forEach { style ->
                                add(style(it.name!!.asString()))
                            }
                        }
                }
            }
        } else {
            val targetProperties =
                targetModel.properties.filter { it.hasSetter }
            val sourceProperties =
                sourceModel.properties.filter { it.hasGetter }

            target2SourceSet =
                targetProperties.associateTo(mutableMapOf()) { targetProperty ->
                    targetProperty.name to sourceProperties.filter { it.name == targetProperty.name }
                        .mapTo(LinkedHashSet()) { it.name }
                }
            if (!ctx.isUpdateFunction) {
                targetModel.constructor.parameters.forEach { param ->
                    target2SourceSet.getOrPut(param.name!!.asString()) { LinkedHashSet() }
                        .apply {
                            sourceProperties.filter { it.name == param.name!!.asString() }
                                .forEach { add(it.name) }
                        }
                }
            }

        }
        if (ctx.mappingPolicy == MappingPolicy.ONLY_EXPLICIT) {
            target2SourceSet.clear()
        }
        explicitMappings.forEach {
            target2SourceSet.getOrPut(it.target) { LinkedHashSet() }
                .add(it.source)
        }
        if (ctx.mappingPolicy == MappingPolicy.LAST_DECLARE) {
            target2SourceSet.values.forEach { set ->
                if (set.isNotEmpty()) {
                    set.last().let {
                        set.clear()
                        set.add(it)
                    }
                }
            }
        }
        ctx.target2Sources = target2SourceSet.filterTo(mutableMapOf()) { it.value.isNotEmpty() }
    }

    companion object {
        operator fun invoke(fn: KSFunctionDeclaration): Boolean {
            return FunctionValidation(fn).test()
        }
    }
}