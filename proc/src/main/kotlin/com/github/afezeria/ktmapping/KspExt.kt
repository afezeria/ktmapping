package com.github.afezeria.ktmapping

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import kotlin.reflect.KClass

fun KSTypeReference.className(nullable: Boolean? = null): TypeName = resolve().className(nullable)

fun KSType.className(nullable: Boolean? = null): TypeName = run {
    this.declaration.containingFile?.let { mapperDependencies += it }
    ClassName(
        declaration.packageName.asString(),
        declaration.simpleName.asString(),
    ).let {
        if (arguments.isEmpty()) {
            it
        } else {
            it.parameterizedBy(arguments.map { it.type!!.className() })
        }
    }.copy(nullable ?: isMarkedNullable)
}

val KSClassDeclaration.defaultConstructor: KSFunctionDeclaration
    get() {
        return primaryConstructor ?: getConstructors().sortedBy { it.parameters.size }.first()
    }

fun isIterable(type: KSTypeReference): Boolean {
    return iterableDeclaration.asStarProjectedType().makeNullable()
        .isAssignableFrom(type.resolve())
}

fun isMap(type: KSType): Boolean {
    return mapDeclaration.asStarProjectedType().makeNullable().isAssignableFrom(type)
}

fun isResultSet(type: KSType): Boolean {
    return resultSetDeclaration.asType(emptyList()).makeNullable().isAssignableFrom(type)
}

inline fun <reified T : Any> KSTypeReference.isType(): Boolean {
    return this.resolve().declaration == gresolver.getClassDeclarationByName<T>()
}

inline fun <reified T : Any> KSClassDeclaration.isType(): Boolean {
    return this == gresolver.getClassDeclarationByName<T>()
}

inline fun <reified T : Annotation> KSAnnotation.toAnnotation(): T? {
    return if (annotationType.resolve().declaration == gresolver.getClassDeclarationByName<T>()) {
        T::class.constructors.first().callBy(
            Mapping::class.constructors.first().parameters.mapNotNull { param ->
                arguments.find { it.name!!.asString() == param.name }
                    ?.let {
                        param to it.value
                    }
            }.toMap()
        )
    } else {
        null
    }
}

inline fun <reified T : Annotation> KSAnnotated.getAnnotations(): List<T> {
    return getAnnotations(T::class)
}

fun <T : Annotation> KSAnnotated.getAnnotations(clazz: KClass<T>): List<T> {
    return annotations
        .filter {
            it.annotationType.resolve().declaration == gresolver.getClassDeclarationByName(clazz.qualifiedName!!)
        }
        .map { ann ->
            println()
            val map = clazz.constructors.first().parameters.mapNotNull { param ->
                ann.arguments.find { it.name!!.asString() == param.name }
                    ?.value
                    ?.let {
                        val value = if (it is ArrayList<*> && it.isNotEmpty()) {
                            val first = it.first()
                            val items =
                                if (first is KSType && (first.declaration as KSClassDeclaration).classKind == ClassKind.ENUM_ENTRY) {
                                    it.map { getEnumEntry(it as KSType) }
                                } else {
                                    it
                                }
                            val array = java.lang.reflect.Array.newInstance(
                                items[0].javaClass,
                                items.size
                            ) as Array<Any>
                            items.forEachIndexed { index, enum -> array[index] = enum }
                            array
                        } else if (it is KSType && (it.declaration as KSClassDeclaration).classKind == ClassKind.ENUM_ENTRY) {
                            getEnumEntry(it)
                        } else {
                            it
                        }
                        param to value
                    }
            }.toMap()
            try {
                clazz.constructors.first().callBy(map)
            } catch (e: Exception) {
                println()
                throw e
            }
        }.toList()
}

fun getEnumEntry(type: KSType): Enum<*> {
    val enumClazz =
        Class.forName(
            (type.declaration as KSClassDeclaration).parentDeclaration!!.qualifiedName!!.asString()
        ) as Class<Enum<*>>
    val enumConstants = enumClazz.enumConstants
    return enumConstants.find { it.name == type.name }!!
}


fun KSPropertyDeclaration.isLateinit() = this.modifiers.contains(Modifier.LATEINIT)
fun KSPropertyDeclaration.isNullable() =
    this.type.resolve().isMarkedNullable
            || this.type.resolve().nullability != Nullability.NOT_NULL

val KSType.name
    get() = declaration.simpleName.asString()
val KSType.qualifierName
    get() = declaration.qualifiedName!!.asString()

fun main() {
}

val KSValueParameter.nameStr
    get() = name!!.asString()
val KSValueParameter.ksType
    get() = type.resolve()
val KSPropertyDeclaration.nameStr
    get() = this.simpleName.asString()

fun getMapValueType(type: KSType): KSType? {
    fun helper(declaration: KSClassDeclaration, type: List<KSType> = emptyList()): KSType? {
        if (!isMap(declaration.asType(emptyList()))) {
            return null
        }
        if (declaration == mapDeclaration) {
            return type[1].makeNullable()
        }
        val name2Idx =
            declaration.typeParameters
                .mapIndexed { i, param -> param.name.asString() to i }
                .toMap()
        return declaration.superTypes.mapNotNull {
            val map = it.resolve().arguments.map { typeArgument ->
                typeArgument.type!!.resolve().let { argType ->
                    val argumentDeclaration = argType.declaration
                    if (argumentDeclaration is KSTypeParameter) {
                        type[name2Idx[argumentDeclaration.name.asString()]!!]
                    } else {
                        argType
                    }
                }
            }
            val superClassDeclaration = it.resolve().declaration.let { superDeclaration ->
                if (superDeclaration is KSTypeAlias) {
                    superDeclaration.type.resolve().declaration
                } else {
                    superDeclaration
                }
            } as KSClassDeclaration
            val mapValueType = helper(superClassDeclaration, map)
            mapValueType
        }.firstOrNull()
    }

    return helper(
        type.getKSClassDeclaration(),
        type.arguments.map { it.type!!.resolve() })
}

val KSType.classDeclaration: KSClassDeclaration
    get() {
        val declaration = declaration
        return if (declaration is KSTypeAlias) {
            declaration.type.resolve().declaration
        } else {
            declaration
        } as KSClassDeclaration
    }

fun KSType.getKSClassDeclaration(): KSClassDeclaration {
    val declaration = declaration
    return if (declaration is KSTypeAlias) {
        declaration.type.resolve().declaration
    } else {
        declaration
    } as KSClassDeclaration
}