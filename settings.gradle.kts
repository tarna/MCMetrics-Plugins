rootProject.name = "MCMetrics"

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        mavenCentral()
        gradlePluginPortal()
    }
}

include("common")
include("paper")
include("velocity")
include("datagenerator")
include("bungee")
include("fabric")