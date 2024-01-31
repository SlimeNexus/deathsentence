import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.5.11" apply false
    id("xyz.jpenilla.run-paper") version "2.2.2"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "nexus.slime"
version = "1.0-SNAPSHOT"

allprojects {
    apply(plugin = "java")

    repositories {
        maven("https://repo.papermc.io/repository/maven-public/")
        mavenCentral()
    }

    dependencies {
        compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    }
}

// ---------------
// Depend on NMS projects

dependencies {
    rootProject.project(":nms").subprojects.forEach {
        implementation(project(it.path))
    }
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }

    jar {
        archiveClassifier = "incomplete"
    }

    shadowJar {
        archiveClassifier = null

        // Not very pretty, but oh well.
        // If anyone has a better idea of how to make paperweight userdev
        // work with this repo structure, *please* tell me :D
        rootProject.project(":nms")
            .subprojects
            .mapNotNull { it.tasks.findByName("reobfJar") }
            .forEach {
                dependsOn(it)
                from(it.outputs)
            }
    }
}

// ---------------
// Add version to plugin.yml and config.yml

tasks.processResources {
    val props = mapOf("version" to version)

    inputs.properties(props)
    filteringCharset = "UTF-8"

    filesMatching("plugin.yml") {
        expand(props)
    }

    filesMatching("config.yml") {
        filter(ReplaceTokens::class, mapOf("tokens" to props))
    }
}
