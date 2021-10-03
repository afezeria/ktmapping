dependencies {

    implementation(kotlin("reflect"))
    implementation(projects.ktmapping)

    implementation("com.squareup:kotlinpoet:1.10.1")
    implementation("com.squareup:kotlinpoet-metadata:1.10.1")
    implementation("com.squareup:kotlinpoet-ksp:1.10.1")
    implementation("com.squareup:kotlinpoet-metadata-specs:1.9.0")
    implementation("com.squareup:kotlinpoet-classinspector-elements:1.9.0")

    implementation("com.google.devtools.ksp:symbol-processing-api:1.5.31-1.0.0")

}