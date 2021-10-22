plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.16.0"
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.0")
    implementation("com.google.guava:guava:31.0.1-jre")
    implementation("org.eclipse.aether:aether-api:1.1.0");
    implementation("org.eclipse.aether:aether-spi:1.1.0");
    implementation("org.eclipse.aether:aether-util:1.1.0");
    implementation("org.eclipse.aether:aether-impl:1.1.0");
    implementation("org.eclipse.aether:aether-connector-basic:1.1.0");
    implementation("org.eclipse.aether:aether-transport-wagon:1.1.0");
    implementation("org.eclipse.aether:aether-transport-classpath:1.1.0");
    implementation("org.eclipse.aether:aether-transport-file:1.1.0");
    implementation("org.eclipse.aether:aether-transport-http:1.1.0");
    implementation("org.apache.maven:maven-core:3.8.3")
    implementation(project(":common"))
}

gradlePlugin {
    plugins {
        create("HDLPlugin") {
            id = "com.heretere.hdl.plugin"
            implementationClass = "com.heretere.hdl.plugin.HDLPlugin"
        }
    }
}

publishing {
    repositories {
        mavenLocal()
    }
}

pluginBundle {
    website = "https://github.com/heretere/hdl"
    vcsUrl = "https://github.com/heretere/hdl"
    description = "Runtime maven dependency loader"

    (plugins) {
        "HDLPlugin" {
            displayName = "Heretere's Dependency Loader"
            tags = listOf("java", "maven", "runtime")
        }
    }
}
