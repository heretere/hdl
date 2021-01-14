# Heretere's Dependency Loader

<p align="left">
    <a href="#" onclick="return false;">
        <img alt="Maven Central" src="https://img.shields.io/maven-central/v/com.heretere.hdl/hdl-core?style=for-the-badge">
    </a>
    <a href="#" onclick="return false;">
        <img alt="Lines of code" src="https://img.shields.io/tokei/lines/github/heretere/hdl?style=for-the-badge">
    </a>
    <a href="#" onclick="return false;">
        <img alt="GitHub code size in bytes" src="https://img.shields.io/github/languages/code-size/heretere/hdl?style=for-the-badge">
    </a>
    <a href="https://github.com/heretere/hdl/blob/master/LICENSE">
        <img alt="GitHub license" src="https://img.shields.io/github/license/heretere/hdl?style=for-the-badge">
    </a>
</p>

### Description

Heretere's Dependency Loader is a runtime dependency loader for spigot plugins. You can easily declare maven
dependencies using annotations. You can easily define a repository URL and jar relocations with annotations as well.

### Why?

Certain websites have capped jar sizes, this library allows you to supply a much smaller jar size without sacrificing
the amount of libraries you use in your project.

### Documentation

- [Javadoc](https://heretere.github.io/hdl/v1.3.1/)
- [Wiki](https://github.com/heretere/hdl/wiki)

---

# Features

- Automatically downloads, relocates, and loads maven dependencies at runtime
- Easily create your own runtime dependency types on top of Maven
- Downloads are cached to your plugin's data folder or to a different specified directory

#### Currently, Supported Dependency Types

- Maven
- ~~GitHub~~ (Planned)

---

# Examples

Here is an example main class to show you want it looks like to declare config dependencies.

```java
import com.heretere.hdl.dependency.maven.annotation.MavenDependencytion.Maven;
import com.heretere.hdl.dependency.maven.annotation.MavenRepository;
import com.heretere.hdl.relocation.annotation.Relocation;

// MavenCentral is included by default so you don't need to declare it
@MavenRepository("https://jitpack.io")
// Transitive dependencies are not supported so you need to declare any dependencies to be downloaded
@MavenDependency("com|github|heretere:hch:v1.0.10")
// You can also declare dependencies in block style
@MavenDependency(groupId = "org|tomlj", artifactId = "tomlj", version = "1.0.0")
// You can define your own custom separator as well, the separator can't contain a . or /
// This is to ensure compatibility with maven and gradle relocation
@MavenDependency(value = "org{}antlr:antlr4-runtime:4.7.2", separator = "{}")
// Make sure to define your relocations in your maven/gradle file as well.
@Relocation(from = "com|heretere|hch", to = "com|myplugin|libs|hch")
@Relocation(from = "org|tomlj", to = "com|myplugin|libs|tomlj")
// You can define your own custom separator as well
@Relocation(from = "org{}antlr", to = "com{}myplugin{}libs{}antlr", separator = "{}")

// DependencyPlugin extends JavaPlugin, it ensures that any dependencies are downloaded and loaded before your
// methods are called.
public final class Test extends DependencyPlugin {
    @Override public void load() {

    }

    @Override public void enable() {

    }

    @Override public void disable() {

    }
}
```

You don't have to extend DependencyPlugin all you need to do is create a new instance of
com.heretere.hdl.DependencyEngine and supply your class to load dependencies. Check more
examples [here](https://gist.github.com/heretere/594cac7163afdf266a043452a0d9bb02).

---

# Usage

HDL is located on maven central.

##### Maven

```xml
<!-- Just the Core -->
<dependency>
    <groupId>com.heretere.hdl</groupId>
    <artifactId>hdl-core</artifactId>
    <version>Version</version>
</dependency>

        <!--Spigot Version-->
<dependency>
<groupId>com.heretere.hdl</groupId>
<artifactId>hdl-spigot</artifactId>
<version>Version</version>
</dependency>
```

##### Gradle

```groovy
repositories {
    mavenCentral()
}
```

```groovy
//Just the Core
dependencies {
    implementation 'com.heretere.hdl:hdl-core:Version'
}
//Spigot Version
dependencies {
    implementation 'com.heretere.hdl:hdl-spigot:Version'
}
```
