# Heretere's Dependency Loader

<p align="left">
    <a href="#" onclick="return false;">
        <img alt="Maven Central" src="https://img.shields.io/maven-central/v/com.heretere.hdl/core?style=for-the-badge">
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

Heretere's Dependency Loader is a runtime maven dependency handler. It has powered using a Gradle plugin to make solving transitive dependencies simple.

### Why?

Certain websites have capped jar sizes, this library allows you to supply a much smaller jar size without sacrificing
the amount of libraries you use in your project.

### Documentation
- [Wiki](https://github.com/heretere/hdl/wiki)

---

# Features

- Automatically downloads, and loads maven dependencies at runtime
- Easily create your own runtime dependency types on top of Maven
- Downloads are cached to your plugin's data folder or to a different specified directory
- Relocation isn't required since files are loaded into an isolated classloader

#### Currently, Supported Dependency Types

- All Maven repos

---

# Examples

## Gradle Plugin

```kotlin
plugins {
	id("com.heretere.hdl.plugin") version "2.0.0"
}

dependencies {
	// instead of implementation("com.google.guava:guava:31.0-jre")
	hdl("com.google.guava:guava:31.0-jre")
}
```

The plugin will automatically include the core dependency so you can invoke it at runtime.

## Main Class

```java
	import com.heretere.hdl.impl.DependencyLoader;

	public class Main {
		public static void main(String[] args) {
			DependencyLoader loader = new DependencyLoader(/*Define a dependency directory path here*/);
			
			boolean success = loader.loadDependencies();
			
			if (!success) {
				throw new RuntimeException("Failed to load dependencies")
				
				//dependencies successfully loaded
			}
		}
	}
```

---

# Usage

Usage without Gradle plugin

<details>
<summary>Maven</summary>

```xml
<!-- Just the Core -->
<dependency>
    <groupId>com.heretere.hdl</groupId>
    <artifactId>core</artifactId>
    <version>Version</version>
</dependency>
```

```xml
<!-- Spigot Version -->
<dependency>
    <groupId>com.heretere.hdl</groupId>
    <artifactId>bukkit</artifactId>
    <version>Version</version>
</dependency>
```
</details>
<details open>
<summary>Gradle</summary>

```groovy
repositories {
    mavenCentral()
}
```

```groovy
//Just the Core
dependencies {
    implementation 'com.heretere.hdl:core:Version'
}
```

```groovy
//Spigot Version
dependencies {
    implementation 'com.heretere.hdl:bukkit:Version'
}
```
</details>
