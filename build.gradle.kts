import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    java
}

repositories {
    mavenCentral()

    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.Minestom:Minestom:c7bb9e14e5")
    compileOnly("com.github.EmortalMC:Immortal:89c6acc127")

    // Compile Minestom into project
    compileOnly("com.github.Minestom:Minestom:4a976a3333")

    // import kotlinx serialization
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    compileOnly("com.github.EmortalMC:NBStom:18bc9744a7")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
}

configurations {
    testImplementation {
        extendsFrom(configurations.compileOnly.get())
    }
}

tasks {
    processResources {
        filesMatching("extension.json") {
            expand(project.properties)
        }
    }

    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        archiveBaseName.set(project.name)
        mergeServiceFiles()
        destinationDirectory.set(File("C:\\Users\\djpen\\Documents\\BlockParty\\extensions"))
        minimize()
    }

    build { dependsOn(shadowJar) }

}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()

compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xinline-classes")
}
