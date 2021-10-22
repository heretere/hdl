plugins {
    idea
    base
    `maven-publish`
    id("io.freefair.lombok") version "6.2.0" apply false
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

allprojects {
    group = "com.heretere.hdl"
    version = "2.0.0"
    description = "Runtime dependency loading library"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply<IdeaPlugin>()
    apply<JavaPlugin>()
    apply<io.freefair.gradle.plugins.lombok.LombokPlugin>()
    apply<MavenPublishPlugin>()

    tasks.withType(JavaCompile::class) {
        options.encoding = java.nio.charset.StandardCharsets.UTF_8.name()
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    tasks.withType(Javadoc::class) {
        options.encoding = java.nio.charset.StandardCharsets.UTF_8.name()
    }

    extensions.configure(JavaPluginExtension::class) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(8));
//        withSourcesJar()
//        withJavadocJar()
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])

                pom {
                    name.set("Heretere's Dependency Loader")
                    url.set("https://github.com/heretere/hdl")
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
}
