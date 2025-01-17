plugins {
    id("xyz.jpenilla.run-paper") version "2.2.3"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.papermc.paperweight.userdev") version "1.5.11"
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.serialization") version "2.1.0"
}

group = "net.onelitefeather"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

val exposedVersion: String by project

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-json:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks {
    test {
        useJUnitPlatform()
    }
    assemble {
        dependsOn(reobfJar)
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "18"
    }
    runServer {
        minecraftVersion("1.20.4")
    }
    shadowJar {
        //archiveFileName.set("${rootProject.name}.${archiveExtension.getOrElse("jar")}")
    }
    processResources {
        dependsOn(project.tasks.generateBukkitPluginDescription)
    }
}

bukkit {
    name = "Alioth"
    author = "MrFireDevil"
    apiVersion = "1.20"
    main = "net.onelitefeather.alioth.Alioth"
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(18))
    }
}