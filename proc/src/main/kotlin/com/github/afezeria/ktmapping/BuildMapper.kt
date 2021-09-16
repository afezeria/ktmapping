package com.github.afezeria.ktmapping

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec

/**
 *
 * @date 2021/8/22
 */
class BuildMapper private constructor(val mapper: KSClassDeclaration) {
    fun build(): FileSpec {

        val fileSpecBuilder = FileSpec.builder(
            mapper.packageName.asString(),
            "${mapper.simpleName.asString()}Impl"
        )
        val classBuilder = TypeSpec.classBuilder(
            ClassName(
                mapper.packageName.asString(),
                mapper.simpleName.asString() + "Impl"
            )
        ).addSuperinterface(
            ClassName(
                mapper.packageName.asString(),
                mapper.simpleName.asString()
            )
        ).addAnnotation(iocAnnotation)
        fileSpecBuilder.indent("    ")

        ctx.fileSpecBuilder = fileSpecBuilder


        mapper.getDeclaredFunctions().mapNotNull {
            val funSpec = BuildFunction(it)
            funSpec
        }.forEach(classBuilder::addFunction)

        ctx.reflectHelperProperties.values.forEach(classBuilder::addProperty)

        return fileSpecBuilder.addType(classBuilder.build()).build()
    }

    companion object {
        operator fun invoke(mapper: KSClassDeclaration): FileSpec? {
            mapperDependencies = mutableSetOf()
//            大概不需要引入也可以
//            mapperDependencies = mutableSetOf(iocAnnotationKSFile)
            return Context(mapper) {
                if (!MapperValidation(mapper)) {
                    return@Context null
                }
                BuildMapper(mapper).build()
            }

        }
    }
}