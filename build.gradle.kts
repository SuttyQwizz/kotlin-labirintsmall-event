plugins {
    kotlin("jvm") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    `java`
}

group = "com.example.myplugin"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.md-5.net/content/repositories/snapshots/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")
    implementation(kotlin("stdlib-jdk8"))
}

tasks {
    build {
        dependsOn("shadowJar")
    }
    shadowJar {
        archiveClassifier.set("")
        relocate("org.jetbrains.kotlin", "${project.group}.kotlin")
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileJava {
        options.release.set(8)
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}