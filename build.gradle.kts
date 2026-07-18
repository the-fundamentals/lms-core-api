plugins {
	java
	id("org.springframework.boot") version "4.1.0"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.diffplug.spotless") version "8.8.0"
	id("org.openapi.generator") version "7.23.0"
}

group = "tech.sangdang"
version = "0.0.1-SNAPSHOT"
description = "lms-core-api"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

spotless {
	java {
		removeUnusedImports()
		googleJavaFormat("1.35.0")
	}
}

openApiGenerate {
	inputSpec.set("$projectDir/src/main/resources/openapi.yml")
	validateSpec.set(true)
	generatorName.set("spring")
	outputDir.set("$projectDir/build/generated/openapi")
	packageName.set("tech.sangdang.lmscoreapi.generated")
	apiPackage.set("tech.sangdang.lmscoreapi.generated.api")
	modelPackage.set("tech.sangdang.lmscoreapi.generated.model")
	configOptions.set(mapOf(
		"performBeanValidation" to "true",
		"useSpringBuiltInValidation" to "true",
		"documentationProvider" to "springdoc",
		"generateBuilders" to "true",
		"generateGenericResponseEntity" to "true",
		"interfaceOnly" to "true",
		"useTags" to "true",
		"library" to "spring-boot",
		"useSpringBoot4" to "true",
		"useJackson3" to "true",
		"useJspecify" to "true",
		"basePackage" to "tech.sangdang.lmscoreapi.generated",
		"apiPackage" to "tech.sangdang.lmscoreapi.generated.api",
		"modelPackage" to "tech.sangdang.lmscoreapi.generated.model"
	))
}

sourceSets {
	main {
		java {
			srcDirs("build/generated/openapi/src/main/java")
		}
	}
}

repositories {
	mavenCentral()
}

val mapstructVersion: String by project
val springdocVersion: String by project
val openapiJacksonNullableVersion: String by project

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")
	implementation("org.openapitools:jackson-databind-nullable:$openapiJacksonNullableVersion")
	compileOnly("org.projectlombok:lombok")
	compileOnly("org.mapstruct:mapstruct:$mapstructVersion")
	annotationProcessor("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
	annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	runtimeOnly("org.postgresql:postgresql")
	testCompileOnly("org.projectlombok:lombok")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testAnnotationProcessor("org.projectlombok:lombok")
	testAnnotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
	testAnnotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.compileJava {
	dependsOn(tasks.openApiGenerate)
}