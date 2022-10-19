plugins {
    kotlin("jvm") version Versions.kotlin_version
    id("io.micronaut.application") version "3.6.2"
    //id("org.jetbrains.kotlin.plugin.allopen")
//    id("io.micronaut.application") version "1.2.0"

    kotlin("plugin.serialization") version Versions.kotlin_version
}

apply(plugin="kotlinx-serialization")


//micronaut {
//    runtime("netty")
//    testRuntime("junit5")
//    processing {
//        incremental(true)
//        annotations("firelib.stockviz.api.*")
//    }
//}
//
//kapt {
//    arguments {
//        arg("micronaut.openapi.views.spec", "redoc.enabled=true,rapidoc.enabled=true,swagger-ui.enabled=true,swagger-ui.theme=flattop")
//    }
//}
//


dependencies {
    kapt("io.micronaut.openapi:micronaut-openapi:2.3.0")
    implementation("io.micronaut:micronaut-validation")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin_version}")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut:micronaut-runtime")
    implementation("javax.annotation:javax.annotation-api")
    implementation("io.micronaut:micronaut-http-client")
    // https://mvnrepository.com/artifact/io.micronaut/micronaut-http
    implementation("io.micronaut:micronaut-http")


    implementation("io.swagger.core.v3:swagger-annotations")
    implementation("io.micronaut.sql:micronaut-jdbc-hikari")
    implementation("io.micronaut.sql:micronaut-jooq")
    implementation("io.micronaut.kotlin:micronaut-kotlin-extension-functions")
    runtimeOnly("org.slf4j:slf4j-simple")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
    runtimeOnly("com.h2database:h2")
    implementation(project(":back"))

    implementation("org.jetbrains.exposed:exposed-core:${Versions.exposed_version}")
    implementation ("org.jetbrains.exposed:exposed-dao:${Versions.exposed_version}")
    implementation("org.jetbrains.exposed:exposed-jdbc:${Versions.exposed_version}")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0")


}

//
//application {
//    mainClass.set("com.example.ApplicationKt")
//}
//
//java {
//    sourceCompatibility = JavaVersion.toVersion("13")
//}