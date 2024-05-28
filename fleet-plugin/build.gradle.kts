plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.20"
    id("org.jetbrains.fleet-plugin") version "0.4.198"
    kotlin("plugin.serialization") version "1.9.20"
}

repositories {
    mavenCentral()
    // needed to retrieve `rhizomedb-compiler-plugin` and `noria-compiler-plugin`
    maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
}

version = "0.1.0"

fleetPlugin {

     id = "pro.bravit.fleetPlugin.funCounter"

     metadata {
         readableName = "Count Functions"
         description = "This plugin contributes an action that displays a number of top-level functions in a Kotlin source file"
     }

    fleetRuntime {
        version = "1.35.115"
    }

    pluginDependencies {
        plugin("fleet.kotlin")
    }
}
