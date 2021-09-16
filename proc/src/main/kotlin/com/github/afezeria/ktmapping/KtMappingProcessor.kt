package com.github.afezeria.ktmapping

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration


/**
 *
 * @date 2021/7/17
 */
class KtMappingProcessor(val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    init {
        logger = environment.logger
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        initGlobalVar(resolver)

        for (declaration in resolver.getSymbolsWithAnnotation(Mapper::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()) {

            val fileSpec = BuildMapper(declaration) ?: continue

            environment.codeGenerator.createNewFile(
                //第二个参数好像不传也不会报错
//                Dependencies(true, *mapperDependencies.toTypedArray()),
                Dependencies(false),
                packageName = declaration.packageName.asString(),
                fileName = declaration.simpleName.asString() + "Impl"
            ).bufferedWriter().use {
                fileSpec.writeTo(it)
            }
        }

        return emptyList()
    }

    fun initGlobalVar(resolver: Resolver) {

        gresolver = resolver
        mapDeclaration = gresolver.getClassDeclarationByName("kotlin.collections.Map")!!
        resultSetDeclaration = gresolver.getClassDeclarationByName("java.sql.ResultSet")!!
        iterableDeclaration = gresolver.getClassDeclarationByName("kotlin.collections.Iterable")!!
        mappingAnnotationDeclaration =
            requireNotNull(gresolver.getClassDeclarationByName<Mapping>()) { "未引入ktmapping" }
        requireNotNull(gresolver.getClassDeclarationByName(iocAnnotation.canonicalName)) {
            "未引入spring-context"
        }
        resultSetCanProvideTypeSet =
            resultSetCanProvideClasses.map { resolver.getClassDeclarationByName(it.qualifiedName!!)!! }
                .toSet()

    }

    class Provider : SymbolProcessorProvider {
        override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
            return KtMappingProcessor(environment)
        }
    }
}
