import org.gradle.kotlin.dsl.support.uppercaseFirstChar

plugins {
    alias(libs.plugins.fabric.loom)
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)

    modImplementation(libs.cloud.fabric)
    include(libs.cloud.fabric)

    implementation(project(path = ":common", configuration = "shadow"))
    include(project(path = ":common", configuration = "shadow"))
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.build {
    dependsOn(tasks.remapJar)
}

tasks.remapJar {
    archiveFileName.set("MCMetrics-${this.project.name.uppercaseFirstChar()}-${this.project.version}.jar")
}