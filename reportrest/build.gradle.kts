plugins {
	id("org.springframework.boot") version "3.1.0"
	id("io.spring.dependency-management") version "1.1.0"
	kotlin("plugin.spring") version "1.8.21"
}

dependencies {
	implementation(project(":back"))
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("javax.validation:validation-api:2.0.1.Final")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")
	implementation("org.jetbrains.exposed:exposed-core:${Versions.exposed_version}")
	implementation("org.jetbrains.exposed:exposed-dao:${Versions.exposed_version}")
	implementation("org.jetbrains.exposed:exposed-jdbc:${Versions.exposed_version}")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
