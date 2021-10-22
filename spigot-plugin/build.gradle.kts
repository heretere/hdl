plugins {
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("com.heretere.hdl.plugin") version "2.0.0"
}

repositories {
    mavenCentral()
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://repo.minebench.de/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
}

dependencies {
    implementation(project(":core"))
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    hdl("co.aikar:acf-paper:0.5.0-SNAPSHOT")
}
