package com.github.afezeria.ktmapping

import com.github.afezeria.ktmapping.MappingPolicy.LAST_DECLARE
import com.github.afezeria.ktmapping.UpdatePolicy.UPDATE_ALL
import com.github.afezeria.ktmapping.model.ValidModel
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec

/**
 *
 * @date 2021/7/22
 */

val iocAnnotation = ClassName("org.springframework.stereotype", "Component")

lateinit var logger: KSPLogger
lateinit var gresolver: Resolver
lateinit var mapDeclaration: KSClassDeclaration
lateinit var iterableDeclaration: KSClassDeclaration
lateinit var resultSetDeclaration: KSClassDeclaration
lateinit var mappingAnnotationDeclaration: KSClassDeclaration
lateinit var iocAnnotationKSFile: KSFile

var ctx: Context = Context()

lateinit var mapperDependencies: MutableSet<KSFile>

open class Context(
    var updatePolicy: UpdatePolicy = UPDATE_ALL,
    var mappingPolicy: MappingPolicy = LAST_DECLARE,
    var sourceNameStyle: Array<NamingStyle> = arrayOf(NamingStyle.CAMEL_CASE),
) {
    lateinit var node: KSDeclaration
    lateinit var targetModel: ValidModel
    lateinit var sourceModel: ValidModel
    lateinit var fileSpecBuilder: FileSpec.Builder
    var isUpdateFunction: Boolean = false
    var reflectHelperProperties: MutableMap<String, PropertySpec> = mutableMapOf()
    var target2Sources: Map<String, Collection<String>> = emptyMap()


    companion object {
        operator fun <R> invoke(ksClassDeclaration: KSClassDeclaration, body: () -> R): R? {
            ctx = ksClassDeclaration.getAnnotations<MapperConfig>().firstOrNull()
                ?.run {
                    Context(
                        updatePolicy = updatePolicy,
                        mappingPolicy = mappingPolicy,
                        sourceNameStyle = sourceNameStyle
                    )
                } ?: Context()
            ctx.node = ksClassDeclaration
            return body()
        }

        operator fun <R> invoke(ksFunctionDeclaration: KSFunctionDeclaration, body: () -> R): R? {
            val tmp = ctx
            try {
                ctx = Context(
                    updatePolicy = tmp.updatePolicy,
                    mappingPolicy = tmp.mappingPolicy,
                    sourceNameStyle = tmp.sourceNameStyle,
                )
                ctx.node = ksFunctionDeclaration
                ctx.fileSpecBuilder = tmp.fileSpecBuilder
                ctx.reflectHelperProperties = tmp.reflectHelperProperties
                ksFunctionDeclaration.getAnnotations<MapperConfig>().firstOrNull()
                    ?.apply {
                        ctx.updatePolicy = updatePolicy
                        ctx.mappingPolicy = mappingPolicy
                        ctx.sourceNameStyle = sourceNameStyle
                    }
                return body()
            } finally {
                ctx = tmp
            }
        }
    }
}