plugins {
	kotlin("jvm") version "1.+"
	id("com.dua3.javafxgradle7plugin") version "0.+"
	id("org.beryx.jlink") version "2.+"
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
	implementation("dev.kkorolyov.pancake:platform:$pancakeVersion")
	implementation("dev.kkorolyov.pancake:core:$pancakeVersion")
	implementation("dev.kkorolyov.pancake.plugin.app-render:jfx:$pancakeVersion") {
		exclude("org.openjfx")
	}
	implementation("dev.kkorolyov.pancake.plugin.audio:jfx:$pancakeVersion") {
		exclude("org.openjfx")
	}

	val log4jVersion: String by project
	val jacksonVersion: String by project
	implementation("org.apache.logging.log4j:log4j-slf4j18-impl:$log4jVersion")
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
}

java {
	sourceCompatibility = JavaVersion.VERSION_14
	targetCompatibility = JavaVersion.VERSION_14
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

javafx {
	modules("javafx.media")
}

jlink {
	mergedModule {
		requires("java.logging")
		requires("java.desktop")

		requires("org.slf4j")
		requires("org.apache.logging.log4j")

		provides("org.slf4j.spi.SLF4JServiceProvider").with("org.apache.logging.slf4j.SLF4JServiceProvider")
	}
	// otherwise exists as standalone + merged module - make it just merged
	forceMerge("kotlin")
}
