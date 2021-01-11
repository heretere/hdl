package com.heretere.hdl;

import com.google.common.collect.Maps;
import com.heretere.hdl.dependency.DependencyLoader;
import com.heretere.hdl.dependency.DependencyProvider;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class DependencyEngine {
    private final @NotNull Path basePath;
    private final @NotNull Map<@NotNull Class<@NotNull ?>, @NotNull DependencyLoader<@NotNull ?>> dependencyLoaders;

    protected DependencyEngine(final @NotNull Path basePath) {
        this.basePath = basePath;
        this.dependencyLoaders = Maps.newIdentityHashMap();
    }

    public static @NotNull DependencyEngine createNew(final @NotNull Path basePath) {
        return DependencyEngine.createNew(basePath, true);
    }

    public static @NotNull DependencyEngine createNew(
        final @NotNull Path basePath,
        final boolean addDefaultLoader
    ) {
        return DependencyEngine.createNew(basePath, addDefaultLoader, Throwable::printStackTrace);
    }

    public static @NotNull DependencyEngine createNew(
        final @NotNull Path basePath,
        final boolean addDefaultLoader,
        final @NotNull Consumer<@NotNull DependencyLoadException> exceptionConsumer
    ) {
        DependencyEngine engine = new DependencyEngine(basePath);

        if (!addDefaultLoader) {
            return engine;
        }

        try {
            engine.addDefaultDependencyLoaders();
            return engine;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException |
            IOException e) {
            exceptionConsumer.accept(new DependencyLoadException(e));
            return engine;
        }
    }

    private void addDefaultDependencyLoaders() throws NoSuchMethodException, IOException, ClassNotFoundException,
        InvocationTargetException, IllegalAccessException {
        this.addDependencyLoader(MavenDependencyLoader.class, new MavenDependencyLoader(this.basePath));
    }

    public <@NotNull L extends DependencyLoader<@NotNull ?>> void addDependencyLoader(
        final @NotNull Class<@NotNull ? extends L> clazz,
        final @NotNull L dependencyLoader
    ) {
        this.dependencyLoaders.put(clazz, dependencyLoader);
    }

    public void addDependencyLoader(final @NotNull DependencyLoader<@NotNull ?> dependencyHandler) {
        this.addDependencyLoader(dependencyHandler.getClass(), dependencyHandler);
    }

    private @NotNull CompletableFuture<Void> loadAllDependencies(
        final @NotNull Object object,
        final @NotNull Executor executor
    ) {
        return CompletableFuture.runAsync(() -> {
            AtomicReference<Optional<Exception>> exceptionReference = new AtomicReference<>(Optional.empty());

            this.dependencyLoaders
                .values()
                .stream()
                .sorted(Comparator.comparingInt(loader -> loader.getClass()
                                                                .getAnnotation(LoaderPriority.class)
                                                                .value()))
                .forEach(loader -> {
                    if (exceptionReference.get().isPresent()) {
                        return;
                    }

                    loader.loadDependenciesFrom(object);

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
        }, executor);
    }

    public @NotNull CompletableFuture<Void> loadAllDependencies(
        final @NotNull Class<@NotNull ?> clazz,
        final @NotNull Executor executor
    ) {
        return this.loadAllDependencies((Object) clazz, executor);
    }

    public @NotNull CompletableFuture<Void> loadAllDependencies(final @NotNull Class<@NotNull ?> clazz) {
        return this.loadAllDependencies(clazz, ForkJoinPool.commonPool());
    }

    public @NotNull CompletableFuture<Void> loadAllDependencies(
        final @NotNull DependencyProvider<?> provider,
        final @NotNull Executor executor
    ) {
        return this.loadAllDependencies((Object) provider, executor);
    }

    public @NotNull CompletableFuture<Void> loadAllDependencies(
        final @NotNull DependencyProvider<?> provider
    ) {
        return this.loadAllDependencies(provider, ForkJoinPool.commonPool());
    }
}
