package com.github.afezeria.ktmapping

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 *
 * @date 2021/8/31
 */
class MapperValidation private constructor(private val ksClassDeclaration: KSClassDeclaration) {
    fun test(): Boolean {
        var result = true
        ksClassDeclaration.run {
            if (typeParameters.isNotEmpty()) {
                logger.error("mapper接口不能有类型参数", this)
                result = false
            }
            if (superTypes.count() > 0) {
                logger.error("mapper接口不能有类型参数", this)
                result = false
            }
            if (classKind != ClassKind.INTERFACE) {
                logger.error("@Mapper注解只能用于接口", this)
                result = false
            }
        }
        return result
    }

    companion object {
        operator fun invoke(ksClassDeclaration: KSClassDeclaration): Boolean {
            return MapperValidation(ksClassDeclaration).test()
        }
    }
}