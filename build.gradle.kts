plugins {
    kotlin("jvm") version "${Versions.kotlin_version}"
    id("org.jetbrains.kotlin.kapt") version "${Versions.kotlin_version}"
    id("idea")
}

subprojects {

    apply(plugin = "idea")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.kapt")

    tasks {
        compileKotlin {
            kotlinOptions {
                jvmTarget = "13"
            }
        }
        compileTestKotlin {
            kotlinOptions {
                jvmTarget = "13"
            }
        }
    }


    group = "com.funstat"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
        google()
        jcenter()
        mavenLocal()
        maven(url = "https://jitpack.io")
    }
}
repositories {
    mavenCentral()
}