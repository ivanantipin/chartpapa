//apply(plugin = "kotlinx-serialization")

dependencies {
    implementation("org.slf4j:slf4j-api:1.7.28")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("ch.qos.logback:logback-core:1.2.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("javolution:javolution:5.5.1")
    implementation("org.springframework:spring-jdbc:5.1.9.RELEASE")
    implementation("org.springframework:spring-web:5.1.9.RELEASE")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin_version}")
    implementation("net.lingala.zip4j:zip4j:1.3.2")
    implementation("org.asynchttpclient:async-http-client:2.5.2")
    implementation("org.apache.commons:commons-io:1.3.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.9")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
    implementation("org.xerial:sqlite-jdbc:3.25.2")
    implementation("com.opencsv:opencsv:5.3")
    implementation("org.apache.commons:commons-csv:1.8")
    implementation("com.github.wendykierp:JTransforms:3.1")
    implementation("org.jetbrains.exposed:exposed-core:${Versions.exposed_version}")
    implementation("org.jetbrains.exposed:exposed-dao:${Versions.exposed_version}")
    implementation("org.jetbrains.exposed:exposed-jdbc:${Versions.exposed_version}")
    implementation("io.ktor:ktor-client-apache:1.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.4")
    implementation("io.ktor:ktor-client-jackson:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
    testImplementation("junit:junit:4.13")
}