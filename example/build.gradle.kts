plugins {
    id("com.google.devtools.ksp") version "1.5.30-1.0.0-beta08"
    id("org.springframework.boot") version "2.5.4"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("plugin.spring") version "1.5.10"
}

configure<com.bnorm.power.PowerAssertGradleExtension> {
    functions = listOf(
        "kotlin.assert",
        "kotlin.test.assertTrue",
        "kotlin.test.assertEquals",
        "kotlin.test.assertContentEquals",
    )
}

dependencies {
    implementation("org.springframework:spring-context:5.3.9")
    implementation(projects.ktmapping)
    implementation(projects.proc)
    ksp(projects.proc)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-jdbc")
    testImplementation(kotlin("test-common"))
    testImplementation(kotlin("test-annotations-common"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("com.google.devtools.ksp:symbol-processing-api:1.5.30-1.0.0-beta08")
    testImplementation("io.github.java-diff-utils:java-diff-utils:4.10")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.4")
    testImplementation("com.zaxxer:HikariCP:5.0.0")
    testImplementation("org.postgresql:postgresql:42.2.23.jre7")
    testImplementation("com.h2database:h2:1.4.200")
}

tasks.test {
    useJUnitPlatform()
}
