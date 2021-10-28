plugins {
    `java-library`
}

dependencies {
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.13.0")
    api(project(":common")) {
        exclude(group = "com.fasterxml.jackson.core", module = "jackson-databind")
    }
}
