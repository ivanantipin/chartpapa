plugins {
    kotlin("jvm") version "${Versions.kotlin_version}"
    id("org.jetbrains.kotlin.kapt") version "${Versions.kotlin_version}"
    id("idea")
    id("org.ajoberstar.grgit") version "5.0.0"

}

fun version() : String{
    return grgit.tag.list().sortedBy { it.commit.dateTime }.last().name
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


    group = "firelib.techbot"
    version = "${version()}"

    repositories {
        mavenCentral()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
        google()
        mavenLocal()
        maven(url = "https://jitpack.io")
    }
}