package com.github.afezeria.ktmapping.example

import com.github.afezeria.ktmapping.KtMappingProcessor
import com.github.difflib.DiffUtils
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import java.io.File

/**
 *
 * @date 2021/8/3
 */
val javaSources = File("src/test/resources").listFiles { f ->
    f.extension == "java"
}!!.map {
    SourceFile.java(it.name, it.readText())
}

val jars = File("src/test/resources/jars").listFiles()!!

val annotation =
    SourceFile.fromPath(File("../src/main/kotlin/com/github/afezeria/ktmapping/Mapper.kt"))

fun getJavaSource(javaFileName: String): SourceFile {
    return SourceFile.fromPath(File("./src/test/resources/java/$javaFileName"))
}

fun createKotlinCompilation(vararg sources: SourceFile): KotlinCompilation =
    KotlinCompilation().apply {
        this.sources = listOf(annotation, *sources)
        symbolProcessorProviders = listOf(KtMappingProcessor.Provider())
        this.classpaths = jars.toList()
    }

fun KotlinCompilation.printGeneratedFile() {
    kspSourcesDir.walkTopDown()
        .filter { !it.isDirectory }
        .forEach {
            println("-------${it.name}------")
            println(it.readText())
        }
}

fun diff(generateCode: String, expectCode: String) {
    val real = generateCode.lines()
    val expect = expectCode.lines()
    val diff = DiffUtils.diff(real, expect)
    diff.deltas.onEach {
        println(it)
    }.apply {
        if (isNotEmpty()) {
            assert(false)
        }
    }
}

fun KotlinCompilation.getGeneratedCode(interfaceName: String = "InterfaceTest"): String {
    return kspSourcesDir.walkTopDown()
        .find { it.name == "${interfaceName}Impl.kt" }!!
        .readText()
}
