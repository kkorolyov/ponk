import org.apache.tools.ant.taskdefs.condition.Os

plugins {
	kotlin("jvm") version "1.+"
	id("org.openjfx.javafxplugin") version "0.+"
	id("org.javamodularity.moduleplugin") version "1.+"
	id("org.beryx.jlink") version "2.+"
	id("org.ajoberstar.reckon") version "0.+"
}

tasks.wrapper {
	distributionType = Wrapper.DistributionType.ALL
}

reckon {
	scopeFromProp()
	snapshotFromProp()
}
tasks.reckonTagCreate {
	dependsOn(tasks.check)
}

dependencyLocking {
	lockAllConfigurations()
}

description = "Simple pong-like"

repositories {
	mavenCentral()

	maven {
		url = uri("https://maven.pkg.github.com/kkorolyov/flub")
		mavenContent {
			releasesOnly()
		}
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
	maven {
		url = uri("https://oss.sonatype.org/content/repositories/snapshots")
		mavenContent {
			includeGroup("no.tornado")
		}
	}
}
dependencies {
	implementation(libs.bundles.app) {
		exclude("org.openjfx")
	}
	implementation(libs.bundles.log)
}

tasks.compileKotlin {
	kotlinOptions {
		jvmTarget = tasks.compileJava.get().targetCompatibility
	}
}
javafx {
	version = tasks.compileJava.get().targetCompatibility
	modules("javafx.fxml", "javafx.web", "javafx.swing")
}

application {
	mainModule.set("dev.kkorolyov.ponk")
	mainClass.set("dev.kkorolyov.ponk.LauncherKt")
}

jlink {
	forceMerge("kotlin", "slf4j", "log4j", "jackson")

	jpackage {
		appVersion = (findProperty("jpackage.version") ?: version).toString()

		icon = if (Os.isFamily(Os.FAMILY_WINDOWS)) "pancake.ico" else "pancake.png"

		val options = mutableListOf("--license-file", "LICENSE")
		if (Os.isFamily(Os.FAMILY_WINDOWS)) options += "--win-dir-chooser"

		installerOptions = options.toList()
	}
}
