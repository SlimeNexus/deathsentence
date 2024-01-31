import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.5.11" apply false
    id("xyz.jpenilla.run-paper") version "2.2.2"
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
        implementation("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
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
    jar {
        from(sourceSets.main.get().output)

        // Include NMS classes in final jar.
        // Not pretty, but it works.
        rootProject.project(":nms").subprojects.forEach {
            from(it.sourceSets.main.get().output)
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
