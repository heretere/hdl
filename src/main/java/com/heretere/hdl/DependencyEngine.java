package com.heretere.hdl;

import com.google.common.collect.Maps;
import com.heretere.hdl.dependency.DependencyLoader;
import com.heretere.hdl.dependency.annotation.LoaderPriority;
import com.heretere.hdl.dependency.maven.MavenDependencyLoader;
import com.heretere.hdl.exception.DependencyLoadException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class DependencyEngine {
    private final @NotNull Path basePath;
    private final @NotNull Map<@NotNull Class<?>, @NotNull DependencyLoader<?>> dependencyLoaders;

    public DependencyEngine(final @NotNull Path basePath) {
        this.basePath = basePath;
        this.dependencyLoaders = Maps.newIdentityHashMap();

        try {
            this.addDefaultDependencyLoaders();
        } catch (NoSuchMethodException
            | ClassNotFoundException
            | IOException
            | InvocationTargetException
            | IllegalAccessException e) {
            throw new DependencyLoadException(e);
        }
    }

    private void addDefaultDependencyLoaders() throws NoSuchMethodException, IOException, ClassNotFoundException,
        InvocationTargetException, IllegalAccessException {
        this.dependencyLoaders.put(MavenDependencyLoader.class, new MavenDependencyLoader(this.basePath));
    }

    public void addDependencyLoader(final @NotNull DependencyLoader<?> dependencyLoader) {
        this.dependencyLoaders.put(dependencyLoader.getClass(), dependencyLoader);
    }

    public void loadAllDependencies(final @NotNull Class<?> clazz) {
        AtomicReference<Optional<Exception>> exceptionReference = new AtomicReference<>(Optional.empty());

        this.dependencyLoaders
            .values()
            .stream()
            .sorted(Comparator.comparingInt(loader -> loader.getClass().getAnnotation(LoaderPriority.class).value()))
            .forEach(loader -> {
                if (exceptionReference.get().isPresent()) {
                    return;
                }

                loader.loadDependenciesFromClass(clazz);

                try {
                    loader.downloadDependencies();
                    loader.relocateDependencies();
                    loader.loadDependencies((URLClassLoader) this.getClass().getClassLoader());
                } catch (Exception e) {
                    exceptionReference.set(Optional.of(e));
                }
            });

        Optional<Exception> exception = exceptionReference.get();

        if (exception.isPresent()) {
            throw new DependencyLoadException(exception.get());
        }
    }
}
