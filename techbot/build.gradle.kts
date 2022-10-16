plugins {
    id("com.google.cloud.tools.jib") version "3.3.0"
}

jib{
    to.image="ivanantipin/techbot:$version"

    container{
        mainClass = "com.firelib.techbot.MainKt"

    }
}

//application{
//    mainClass.set("com.firelib.techbot.MainKt")
//    applicationDefaultJvmArgs = listOf("-Xms128m", "-Xmx1024m", "-Xloggc:/ddisk/globaldatabase/gc.log", "–XX:+PrintGCDetails", "-XX:+PrintGCDateStamps")
//}


dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("info.picocli:picocli:4.5.2")
    implementation ("io.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:6.0.6")
    implementation("io.ktor:ktor-client-cio:1.4.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin_version}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
    implementation ( "org.springframework:spring-jdbc:5.1.9.RELEASE")
    implementation ( "org.springframework:spring-web:5.1.9.RELEASE")
    implementation ( "org.xerial:sqlite-jdbc:3.25.2")
    implementation (project(":back"))
    implementation ("org.jetbrains.exposed:exposed-core:${Versions.exposed_version}")
    implementation ("org.jetbrains.exposed:exposed-dao:${Versions.exposed_version}")
    implementation ("org.jetbrains.exposed:exposed-jdbc:${Versions.exposed_version}")
    testImplementation("junit:junit:4.13")
}