plugins {
    id ("application")
}

dependencies {
    implementation (project(":back"))
    implementation (project(":transaqgate"))
}

//application {
//    mainClassName = "com.firelib.prod.RunRunKt"
//    //"-Dcom.sun.management.jmxremote.port=9999", "-Dcom.sun.management.jmxremote.authenticate=false", "-Dcom.sun.management.jmxremote.ssl=false"
//    applicationDefaultJvmArgs = ["-Xms128m", "-Xmx1024m"]
//}