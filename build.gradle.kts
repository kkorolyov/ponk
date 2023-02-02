import org.gradle.internal.os.OperatingSystem

plugins {
	kotlin("jvm") version "1.8.10"
	id("com.github.jk1.dependency-license-report") version "2.+"
	id("org.beryx.jlink") version "2.+"
	id("org.ajoberstar.reckon") version "0.+"
}

description = "Simple pong-like"

tasks.wrapper {
	distributionType = Wrapper.DistributionType.ALL
}

reckon {
	stages("rc", "final")
	setScopeCalc(calcScopeFromProp())
	setStageCalc(calcStageFromProp())
}
tasks.reckonTagCreate {
	dependsOn(tasks.check)
}

repositories {
	mavenCentral()

	listOf("flub", "pancake").forEach {
		maven {
			url = uri("https://maven.pkg.github.com/kkorolyov/$it")
			credentials {
				username = System.getenv("GITHUB_ACTOR")
				password = System.getenv("GITHUB_TOKEN")
			}
		}
	}
}
dependencies {
	implementation(libs.bundles.stdlib)
	implementation(libs.bundles.pancake)
	implementation(libs.bundles.log)

	val os = OperatingSystem.current()

	// lwjgl
	val lwjglPlatform = "natives-${if (os.isWindows) "windows" else if (os.isMacOsX) "macos" else "linux"}"
	libs.bundles.lwjgl.get()
		.map { it.module }
		.forEach {
			implementation("${it.group}:${it.name}::$lwjglPlatform")
		}

	// imgui
	implementation(
		if (os.isWindows) libs.imgui.windows
		else if (os.isMacOsX) libs.imgui.macos
		else libs.imgui.linux
	)

	dependencyLocking {
		lockAllConfigurations()

		ignoredDependencies.add("io.github.spair:imgui-java-natives*")
	}
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

jlink {
	forceMerge("slf4j", "log4j", "jackson")

	options.addAll("--compress", "2", "--no-header-files", "--no-man-pages", "--strip-debug")

	jpackage {
		appVersion = (findProperty("jpackage.version") ?: version).toString()

		icon = "pancake.${if (OperatingSystem.current().isWindows) "ico" else "png"}"

		val options = mutableListOf("--license-file", "LICENSE")
		if (OperatingSystem.current().isWindows) options += "--win-dir-chooser"
		installerOptions.addAll(options.toList())

		jvmArgs.addAll(listOf("-splash:\$APPDIR/splash.png"))
	}
}

tasks.jpackageImage {
	dependsOn(tasks.generateLicenseReport)

	doLast {
		copy {
			from(rootDir)
			include("pancake.png")
			rename("pancake", "splash")
			into("$buildDir/jpackage/${project.name}/app")
		}
		copy {
			from("$buildDir/reports")
			into("$buildDir/jpackage/${project.name}/runtime/legal")
		}
	}
}
