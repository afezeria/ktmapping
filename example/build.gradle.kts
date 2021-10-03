import  org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("com.google.devtools.ksp") version "1.5.31-1.0.0"
    id("org.springframework.boot") version "2.5.4"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("plugin.spring") version "1.5.31"
    id("com.bnorm.power.kotlin-power-assert") version "0.10.0"
}

configure<com.bnorm.power.PowerAssertGradleExtension> {
    functions = listOf(
        "kotlin.assert",
        "kotlin.test.assertTrue",
        "kotlin.test.assertEquals",
        "kotlin.test.assertContentEquals",
        "kotlin.test.assertContains"
    )
}

dependencies {
    testImplementation(projects.ktmapping)
    kspTest(projects.proc)

    testImplementation(projects.proc)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-jdbc")
    testImplementation(kotlin("test-common"))
    testImplementation(kotlin("test-annotations-common"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("com.google.devtools.ksp:symbol-processing-api:1.5.31-1.0.0")
    testImplementation("io.github.java-diff-utils:java-diff-utils:4.11")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.4")
    testImplementation("org.postgresql:postgresql:42.2.24.jre7")
    testImplementation("com.h2database:h2:1.4.200")
}

tasks.test {
    useJUnitPlatform()
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}
