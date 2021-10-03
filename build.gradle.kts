import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
}

allprojects {

    apply {
        plugin("kotlin")
    }

    repositories {
        mavenCentral()
        maven("https://maven.google.com/")
    }

    group = "com.github.afezeria.ktmapping"
    version = "0.0.1"

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            javaParameters = true
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
            jvmTarget = "11"
        }
    }


    dependencies {
        val implementation by configurations
        implementation(kotlin("stdlib"))
    }
}