plugins {
    id("com.google.cloud.tools.jib") version "3.3.0"
}

jib{
    to.image="ivanantipin/techbot:$version"

    container{
        mainClass = "com.firelib.techbot.MainKt"
        jvmFlags = listOf("-Xms128m", "-Xmx1024m", "-Xlog:gc*:/ddisk/globaldatabase/gc.log:time")
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("info.picocli:picocli:4.5.2")
    implementation ("io.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:6.0.6")
    implementation("io.ktor:ktor-client-cio-jvm:${Versions.ktor_version}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin_version}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
    implementation ( "org.springframework:spring-jdbc:5.1.9.RELEASE")
    implementation ( "org.springframework:spring-web:5.1.9.RELEASE")
    implementation ("org.xerial:sqlite-jdbc:${Versions.sqlite}")
    implementation (project(":back"))
    // https://mvnrepository.com/artifact/io.mockk/mockk
    implementation ("org.jetbrains.exposed:exposed-core:${Versions.exposed_version}")
    implementation ("org.jetbrains.exposed:exposed-dao:${Versions.exposed_version}")
    implementation ("org.jetbrains.exposed:exposed-jdbc:${Versions.exposed_version}")
    testImplementation("junit:junit:4.13")
    testImplementation("io.mockk:mockk:1.13.2")
}