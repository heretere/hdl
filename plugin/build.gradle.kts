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

afterEvaluate {
    publishing {
        publications {
            withType(MavenPublication::class) {
                setArtifacts(listOf(tasks["jar"], tasks["sourcesJar"], tasks["javadocJar"]))

                pom {
                    name.set("Heretere's Dependency Loader")
                    url.set("https://github.com/heretere/hdl")
                    description.set(project.description)
                    packaging = "jar"

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }
                    developers {
                        developer {
                            id.set("Heretere")
                            name.set("Justin Heflin")
                            url.set("https://justinheflin.com")
                            email.set("justin.heflin@protonmail.com")
                        }
                    }
                    scm {
                        connection.set("scm:git@github.com:heretere/hdl.git")
                        developerConnection.set("scm:git@github.com:heretere/hdl.git")
                        url.set("https://github.com/heretere/hdl")
                    }
                }
            }
        }
    }

    extensions.configure(SigningExtension::class) {
        if (project.hasProperty("signing.gnupg.keyName")) {
            useGpgCmd()
            sign(publishing.publications["pluginMaven"], publishing.publications["HDLPluginPluginMarkerMaven"])
        }
    }

    tasks["publishPluginMavenPublicationToMavenLocal"].dependsOn("signHDLPluginPluginMarkerMavenPublication")
    tasks["publishHDLPluginPluginMarkerMavenPublicationToMavenLocal"].dependsOn("signPluginMavenPublication")
}

pluginBundle {
    website = "https://github.com/heretere/hdl"
    vcsUrl = "https://github.com/heretere/hdl"
    description = "Runtime maven dependency loader"

    (plugins) {
        "HDLPlugin" {
            displayName = "Heretere's Dependency Loader"
            description = "Download dependencies at runtime instead of shading to match file size limitations"
            tags = listOf("java", "maven", "runtime")
        }
    }
}
