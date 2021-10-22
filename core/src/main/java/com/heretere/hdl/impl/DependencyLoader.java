package com.heretere.hdl.impl;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heretere.hdl.common.constants.DefaultRepository;
import com.heretere.hdl.common.json.HDLConfig;
import com.heretere.hdl.common.json.ResolvedDependency;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

public class DependencyLoader {
    private static final Set<ResolvedDependency> runtimeDependencies = new HashSet<>();

    private final ClassLoader classLoader;
    private final URLClassLoaderAccess classLoaderAccess;
    private final Path basePath;

    public DependencyLoader(@NonNull Path basePath) {
        this(basePath, DependencyLoader.class.getClassLoader());
    }

    public DependencyLoader(@NonNull Path basePath, @NonNull ClassLoader classLoader) {
        this.basePath = basePath;

        if (!(classLoader instanceof URLClassLoader)) {
            throw new AssertionError("Classloader must be instanceof URLClassLoader.");
        }

        this.classLoader = classLoader;
        this.classLoaderAccess = URLClassLoaderAccess.create((URLClassLoader) classLoader);
    }

    @SneakyThrows
    public boolean loadDependencies() {
        // val config = new ObjectMapper().readValue(
        // this.classLoader.getResourceAsStream("/hdl_dependencies.json"),
        // HDLConfig.class
        // );

        this.loadPrivateDependencies();

        new ObjectMapper();

        return true;
    }

    private void loadDependency(@NonNull HDLConfig config, @NonNull ResolvedDependency dependency) {
        val defaultRepository = DefaultRepository.fromId(dependency.getRepositoryId());

        final Set<String> urls;

        if (defaultRepository == null) {
            urls = config.getRepositories()
                .stream()
                .filter(repository -> repository.getId().equals(dependency.getRepositoryId()))
                .findFirst()
                .orElseThrow(
                    () -> new RuntimeException("Couldn't find repo for dependency: " + dependency.getFileName())
                )
                .getUrls();
        } else {
            urls = defaultRepository.getMirrors();
        }

    }

    private boolean loadDependencyFromURLString(@NonNull String urlString, @NonNull String fileName) {
        System.out.println("Test");
        val saveLocation = this.basePath.resolve(fileName);

        try {
            if (!Files.exists(saveLocation)) {
                val url = new URL(urlString);

                val connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("HEAD");

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return false;
                }

                Files.createDirectories(this.basePath);
                Files.copy(url.openStream(), saveLocation);
            }

            this.classLoaderAccess.addURL(saveLocation.toUri().toURL());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private void loadPrivateDependencies() {
        this.loadDependencyFromURLString(
            "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.13.0/jackson-databind-2.13.0.jar",
            "jackson-databind-2.13.0.jar"
        );
        this.loadDependencyFromURLString(
            "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.13.0/jackson-core-2.13.0.jar",
            "jackson-core-2.13.0.jar"
        );
        this.loadDependencyFromURLString(
            "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.13.0/jackson-annotations-2.13.0.jar",
            "jackson-annotations-2.13.0.jar"
        );
    }
}
