plugins {
	kotlin("jvm") version "1.+"
	id("org.openjfx.javafxplugin") version "0.+"
	id("org.javamodularity.moduleplugin") version "1.+"
	id("org.beryx.jlink") version "2.+"
	idea
}

tasks.wrapper {
	distributionType = Wrapper.DistributionType.ALL
}

dependencyLocking {
	lockAllConfigurations()
}

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
	implementation(libs.bundles.app)
	implementation(libs.bundles.log)
}

tasks.compileKotlin {
	kotlinOptions {
		jvmTarget = tasks.compileJava.get().targetCompatibility
	}
}

application {
	mainModule.set("dev.kkorolyov.ponk")
	mainClass.set("dev.kkorolyov.ponk.LauncherKt")
}

javafx {
	modules("javafx.media", "javafx.graphics")
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
