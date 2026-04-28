import org.gradle.api.Project.DEFAULT_BUILD_DIR_NAME
import org.gradle.kotlin.dsl.support.uppercaseFirstChar

plugins {
    id("java")
    alias(libs.plugins.shadow)
}

allprojects {
    group = "net.mcmetrics"
    version = System.getenv("VERSION") ?: "dev"

    repositories {
        mavenLocal() // For when I do local SDK development alongside plugin development
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/groups/public/")
        maven("https://maven.hoglin.gg/releases/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://maven.fabricmc.net/")
    }
}

subprojects {
    apply(plugin = "java")

    dependencies {
        implementation(rootProject.libs.hoglin)
        compileOnly(rootProject.libs.lombok)
        annotationProcessor(rootProject.libs.lombok)
    }

    if (name != "fabric") {
        apply(plugin = rootProject.libs.plugins.shadow.get().pluginId)

        tasks.build {
            dependsOn("shadowJar")
        }

        tasks.shadowJar {
            archiveFileName.set("MCMetrics-${this.project.name.uppercaseFirstChar()}-${this.project.version}.jar")
            mergeServiceFiles()
        }
    }
}

val pluginProjectNames = listOf("paper", "bungee", "velocity", "fabric")
val pluginProjects = subprojects.filter { it.name in pluginProjectNames }

tasks.register("copyBuiltJars") {
    val jarTasks = pluginProjects.map { project ->
        when {
            project.plugins.hasPlugin("fabric-loom") -> project.tasks.findByName("remapJar")
            project.plugins.hasPlugin("com.gradleup.shadow") -> project.tasks.findByName("shadowJar")
            else -> project.tasks.jar
        }
    }
    dependsOn(jarTasks)
    doLast {
        val releaseDir = file("$DEFAULT_BUILD_DIR_NAME/release")
        releaseDir.mkdirs()

        pluginProjects.forEach { project ->
            val jarFile = when {
                project.plugins.hasPlugin("fabric-loom") -> project.tasks.findByName("remapJar")!!.outputs.files.singleFile
                project.plugins.hasPlugin("com.gradleup.shadow") -> project.tasks.findByName("shadowJar")!!.outputs.files.singleFile
                else -> project.tasks.jar.get().outputs.files.singleFile
            }

            if (jarFile.exists()) {
                copy {
                    from(jarFile)
                    into(releaseDir)

                    rename {
                        "MCMetrics-${project.name.uppercaseFirstChar()}.jar"
                    }
                }
            }
        }
    }
}

tasks.build {
    dependsOn(subprojects.map { it.tasks.build })
    finalizedBy("copyBuiltJars")
}