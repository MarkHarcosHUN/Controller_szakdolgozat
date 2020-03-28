import com.github.jengelman.gradle.plugins.shadow.tasks.*
import org.jlleitschuh.gradle.ktlint.reporter.*

plugins {
    application
    kotlin("jvm") version "1.3.71"
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("org.jlleitschuh.gradle.ktlint-idea") version "9.2.1"
}

group = "uni-sopron"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    reporters {
        reporter(ReporterType.PLAIN)
        reporter(ReporterType.CHECKSTYLE)
    }
}

application {
    mainClassName = "gateway.controller.MainKt"
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    val shadowJarTask = named<ShadowJar>("shadowJar")
    val relocate = register<ConfigureShadowRelocation>("relocateShadowJar") {
        target = shadowJarTask.get()
    }

    shadowJar {
        destinationDirectory.set(File(projectDir, "./build/"))
        mergeServiceFiles()
        dependsOn(relocate)
    }
}
