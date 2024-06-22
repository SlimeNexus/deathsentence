import org.apache.tools.ant.filters.ReplaceTokens
import java.io.ByteArrayOutputStream

plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.7.1" apply false
    id("xyz.jpenilla.run-paper") version "2.3.0"
    id("io.github.goooler.shadow") version "8.1.7"
}

group = "nexus.slime"
version = getVersionFromEnvironment()

allprojects {
    apply(plugin = "java")

    repositories {
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        mavenCentral()
    }

    dependencies {
        compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
        compileOnly("me.clip:placeholderapi:2.11.6")
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }
}

// ---------------
// Depend on NMS projects

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

// ---------------
// Produce a good project version

fun getVersionFromEnvironment(): String {
    // If we're built from CI, we use that version
    val release = System.getenv("BUILD_RELEASE_VERSION")

    if (release != null) {
        return release
    }

    // Otherwise, we generate the next dev version
    // Example: Latest git tag: v0.0.3 --> Version: 0.0.4-DEV
    val output = ByteArrayOutputStream()

    try {
        exec {
            commandLine("git", "tag", "--sort=taggerdate")
            standardOutput = output
        }
    } catch (ignored: GradleException) {
        // If git isn't found, we just leave the output blank
    }

    val latestTag = output.toString()
        .lines()
        .lastOrNull { it.isNotBlank() }
        ?.removePrefix("v") ?: "0.0.0"

    val patchNumber = latestTag.substringAfterLast('.').toInt()
    return latestTag.replaceAfterLast('.', (patchNumber + 1).toString()) + "-DEV"
}
