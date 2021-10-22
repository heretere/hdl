package com.heretere.hdl.impl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heretere.hdl.common.constants.DefaultRepository;
import com.heretere.hdl.common.json.HDLConfig;
import com.heretere.hdl.common.json.Repository;
import com.heretere.hdl.common.json.ResolvedDependency;
import com.heretere.hdl.impl.exception.DependencyLoadException;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public class DependencyLoader {
    private static final String CENTRAL_URL = DefaultRepository.MAVEN_CENTRAL.getMirrors().iterator().next();
    private static final Set<AbstractMap.SimpleImmutableEntry<String, String>> privateDependencies = new HashSet<>();

    static {
        privateDependencies.add(
            new AbstractMap.SimpleImmutableEntry<>(
                    CENTRAL_URL
                        + "com/fasterxml/jackson/core/jackson-databind/2.13.0/jackson-databind-2.13.0.jar",
                    "jackson-databind-2.13.0.jar"
            )
        );
        privateDependencies.add(
            new AbstractMap.SimpleImmutableEntry<>(
                    CENTRAL_URL
                        + "com/fasterxml/jackson/core/jackson-core/2.13.0/jackson-core-2.13.0.jar",
                    "jackson-core-2.13.0.jar"
            )
        );
        privateDependencies.add(
            new AbstractMap.SimpleImmutableEntry<>(
                    CENTRAL_URL
                        + "com/fasterxml/jackson/core/jackson-annotations/2.13.0/jackson-annotations-2.13.0.jar",
                    "jackson-annotations-2.13.0.jar"
            )
        );
    }

    private final ClassLoader classLoader;
    private final URLClassLoaderAccess classLoaderAccess;
    private final Path basePath;
    @Getter
    private final Set<Throwable> errors;

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
        this.errors = new HashSet<>();
    }

    public boolean loadDependencies() {
        try {
            this.loadPrivateDependencies();
        } catch (Exception e) {
            this.errors.add(
                new DependencyLoadException(
                        null,
                        null,
                        "Failed to load base dependencies."
                            + " Do you have an internet connection?"
                )
            );
        }

        HDLConfig config = null;
        if (this.errors.isEmpty()) {
            try {
                config = new ObjectMapper().readValue(
                    this.classLoader.getResourceAsStream("hdl_dependencies.json"),
                    HDLConfig.class
                );
            } catch (IOException e) {
                this.errors.add(e);
            }
        }

        val finalConfig = config;

        if (this.errors.isEmpty() && finalConfig != null) {
            finalConfig
                .getDependencies()
                .stream()
                .parallel()
                .forEach(
                    dependency -> {
                        try {
                            this.loadDependency(
                                dependency,
                                finalConfig.getRepositories().get(dependency.getRepositoryId())
                            );
                        } catch (DependencyLoadException e) {
                            this.errors.add(e);
                        } catch (Exception e) {
                            this.errors.add(
                                new DependencyLoadException(
                                        dependency,
                                        finalConfig.getRepositories()
                                            .get(dependency.getRepositoryId()),
                                        "Failed to load dependency " + dependency
                                )
                            );
                        }
                    }
                );
        }

        return this.errors.isEmpty();
    }

    private void loadDependency(
            @NonNull ResolvedDependency dependency,
            @NonNull Repository repository
    ) {
        val defaultRepository = DefaultRepository.fromId(dependency.getRepositoryId());

        final Set<String> urls;

        if (defaultRepository == null) {
            urls = repository.getUrls();
        } else {
            urls = defaultRepository.getMirrors();
        }

        if (
            urls
                .stream()
                .filter(
                    url -> this.loadDependencyFromURLString(url + dependency.getRelativeUrl(), dependency.getFileName())
                )
                .findFirst()
                .orElse(null) == null
        ) {
            throw new DependencyLoadException(
                    dependency,
                    repository,
                    "Failed to load dependency " + dependency
            );
        }
    }

    private boolean loadDependencyFromURLString(
            @NonNull String urlString,
            @NonNull String fileName
    ) {
        if (!this.errors.isEmpty()) {
            return false;
        }

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
            this.errors.add(e);
        }

        return this.errors.isEmpty();
    }

    private void loadPrivateDependencies() {
        privateDependencies
            .stream()
            .parallel()
            .forEach(dependency -> this.loadDependencyFromURLString(dependency.getKey(), dependency.getValue()));
    }
}
