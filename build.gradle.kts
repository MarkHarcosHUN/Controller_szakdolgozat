import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    val ktlintVersion = "9.2.1"

    application
    kotlin("jvm") version "1.3.71"
    id("org.jlleitschuh.gradle.ktlint") version ktlintVersion
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("org.jlleitschuh.gradle.ktlint-idea") version ktlintVersion
}

group = "uni-sopron"
version = "0.1"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(group = "io.moquette", name = "moquette-broker", version = "0.12.1")
    implementation(group = "org.eclipse.paho", name = "org.eclipse.paho.client.mqttv3", version = "1.2.2")
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
