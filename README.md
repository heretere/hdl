# Heretere's Dependency Loader

<p align="left">
    <a href="https://jitpack.io/#heretere/hdl">
        <img alt="JitPack" src="https://img.shields.io/jitpack/v/github/heretere/hdl?style=for-the-badge">
    </a>
    <a href="#" onclick="return false;">
        <img alt="Lines of code" src="https://img.shields.io/tokei/lines/github/heretere/hdl?style=for-the-badge">
    </a>
    <a href="#" onclick="return false;">
        <img alt="GitHub code size in bytes" src="https://img.shields.io/github/languages/code-size/heretere/hdl?style=for-the-badge">
    </a>
    <a href="https://github.com/heretere/hdl/blob/main/LICENSE">
        <img alt="GitHub license" src="https://img.shields.io/github/license/heretere/hdl?style=for-the-badge">
    </a>
</p>

### Description

Heretere's Dependency Loader is a runtime dependency loader for spigot plugins. You can easily declare maven
dependencies using annotations. You can easily define repository urls and jar relocations with annotations as well.

### Why?

Certain websites have capped jar sizes, this library allows you to supply a much smaller jar size without sacrificing
the amount of libraries you use in your project.

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
import com.heretere.hdl.dependency.maven.annotation.Maven;
import com.heretere.hdl.dependency.maven.annotation.MavenRepo;
import com.heretere.hdl.relocation.annotation.Relocation;

@MavenRepo("https://jitpack.io")
@Maven("com|github|heretere:hch:v1.0.10")
@Relocation(from = "com|heretere|hch", to = "com|heretere|hdl|hch")
public class Test extends DependencyPlugin {
    @Override public void load() {

    }

    @Override public void enable() {

    }

    @Override public void disable() {

    }
}
```

You don't have to extend DependencyPlugin all you need to do is create a new instance of
com.heretere.hdl.DependencyEngine and supply your class to load dependencies.

---

# Usage

##### Maven

```xml

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

```xml

<dependency>
    <groupId>com.github.heretere</groupId>
    <artifactId>hdl</artifactId>
    <version>Version</version>
</dependency>
```

##### Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```

```groovy
dependencies {
    implementation 'com.github.heretere:hdl:Version'
}
```