plugins {
    id ("application")
    id("com.google.cloud.tools.jib") version "3.3.0"
}

jib{
    to.image="ivanantipin/fireprod:$version"

    container{
        mainClass = "com.firelib.techbot.MainKt"
        jvmFlags = listOf("-Xms128m", "-Xmx1024m", "-Xlog:gc*:/ddisk/globaldatabase/gc.log:time")
    }
}


dependencies {
    implementation (project(":back"))
    implementation (project(":transaqgate"))
}
