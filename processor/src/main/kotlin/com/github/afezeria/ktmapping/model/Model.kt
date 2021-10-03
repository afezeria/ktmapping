package com.github.afezeria.ktmapping.model

import com.github.afezeria.ktmapping.isMap
import com.github.afezeria.ktmapping.isResultSet
import com.github.afezeria.ktmapping.name
import com.github.afezeria.ktmapping.qualifierName
import com.google.devtools.ksp.symbol.KSType

/**
 *
 * @date 2021/8/11
 */
sealed class Model(
    /**
     * 类型
     */
    val type: KSType,
    /**
     * 在函数中允许作为source
     */
    val allowAsSource: Boolean,
    /**
     * 在函数中允许作为target
     */
    val allowAsTarget: Boolean,
) {
    fun valid(): String? {
        return when (this) {
            is ValidModel -> {
                if ((isSource && !allowAsSource) || (!isSource && !allowAsTarget)) {
                    "cannot use ${type.name} as ${if (isSource) "source" else "target"}"
                } else {
                    null
                }
            }
            is InvalidModel -> {
                "cannot use ${type.name} as source or target"
            }
        }
    }

    companion object {
        operator fun invoke(type: KSType, varName: String, isSource: Boolean): Model {
            return when {
                isMap(type) -> {
                    MapModel(varName, type, isSource)
                }
                isResultSet(type) -> {
                    ResultSetModel(varName, type, isSource)
                }
                type.qualifierName.matches("^(java|kotlin)".toRegex()) -> InvalidModel(type)
                else -> EntityModel(varName, type, isSource)
            }
        }
    }
}


