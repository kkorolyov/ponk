plugins {
	kotlin("jvm") version "1.+"
	id("org.jetbrains.dokka") version "1.+"
	application
	idea
}

tasks.wrapper {
	distributionType = Wrapper.DistributionType.ALL
}

dependencyLocking {
	lockAllConfigurations()
}

group = "dev.kkorolyov"
description = "Simple pong-like"

repositories {
	mavenCentral()

	maven {
		url = uri("https://maven.pkg.github.com/kkorolyov/flopple")
		credentials {
			username = System.getenv("GITHUB_ACTOR")
			password = System.getenv("GITHUB_TOKEN")
		}
	}
	maven {
		url = uri("https://maven.pkg.github.com/kkorolyov/pancake")
		credentials {
			username = System.getenv("GITHUB_ACTOR")
			password = System.getenv("GITHUB_TOKEN")
		}
	}

}
dependencies {
	val floppleVersion: String by project
	implementation("dev.kkorolyov:flopple:$floppleVersion")

	val pancakeVersion: String by project
	implementation("dev.kkorolyov.pancake:pancake-platform:$pancakeVersion")
	implementation("dev.kkorolyov.pancake:pancake-core:$pancakeVersion")
	runtimeOnly("dev.kkorolyov.pancake:javafx-application:$pancakeVersion")
	runtimeOnly("dev.kkorolyov.pancake:javafx-audio:$pancakeVersion")

	val log4jVersion: String by project
	val jacksonVersion: String by project
	implementation("org.apache.logging.log4j:log4j-slf4j18-impl:$log4jVersion")
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")

	// cannot use openjfx plugin as it also does "patch-module"
	val os: String = org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem().run {
		when {
			isWindows -> "win"
			isMacOsX -> "mac"
			isLinux -> "linux"
			else -> "unknown"
		}
	}
	val javaFxVersion: String by project
	implementation("org.openjfx:javafx-base:$javaFxVersion:$os")
	implementation("org.openjfx:javafx-graphics:$javaFxVersion:$os")
	implementation("org.openjfx:javafx-media:$javaFxVersion:$os")
}

java {
	sourceCompatibility = JavaVersion.VERSION_14
	targetCompatibility = JavaVersion.VERSION_14

	modularity.inferModulePath.set(true)
}

tasks.compileJava {
	options.compilerArgs.addAll(
		listOf(
			"--patch-module", "dev.kkorolyov.ponk=${sourceSets.main.get().output.asPath}"
		)
	)
}
tasks.compileKotlin {
	kotlinOptions {
		jvmTarget = "14"
	}
}

application {
	mainModule.set("dev.kkorolyov.ponk")
	mainClass.set("dev.kkorolyov.ponk.LauncherKt")
}
