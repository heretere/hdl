package com.heretere.hdl;

import com.google.common.collect.Maps;
import com.heretere.hdl.dependency.Dependency;
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
    private @NotNull final Path basePath;
    private @NotNull final Map<@NotNull Class<?>, @NotNull DependencyLoader<?>> dependencyLoaders;

    protected DependencyEngine(@NotNull final Path basePath) {
        this.basePath = basePath;
        this.dependencyLoaders = Maps.newIdentityHashMap();
    }

    private void addDefaultDependencyLoaders() throws NoSuchMethodException, IOException, ClassNotFoundException,
        InvocationTargetException, IllegalAccessException {
        this.addDependencyLoader(MavenDependencyLoader.class, new MavenDependencyLoader(this.basePath));
    }

    public <L extends DependencyLoader<?>>
    void addDependencyLoader(@NotNull final Class<? extends L> clazz, @NotNull final L dependencyLoader) {
        this.dependencyLoaders.put(clazz, dependencyLoader);
    }

    public void addDependencyLoader(@NotNull final DependencyLoader<?> dependencyHandler) {
        this.addDependencyLoader(dependencyHandler.getClass(), dependencyHandler);
    }

    private @NotNull CompletableFuture<Void> loadAllDependencies(
            @NotNull final Object object,
            @NotNull final Executor executor
    ) {
        return CompletableFuture.runAsync(() -> {
            AtomicReference<Optional<Exception>> exceptionReference = new AtomicReference<>(Optional.empty());

            this.dependencyLoaders
                    .values()
                    .stream()
                    .sorted(Comparator.comparingInt(loader -> loader.getClass().getAnnotation(LoaderPriority.class).value()))
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
        },executor);
    }

    public @NotNull CompletableFuture<Void> loadAllDependencies(
            @NotNull final Class<?> clazz,
            @NotNull final Executor executor
    ) {
        return this.loadAllDependencies((Object) clazz, executor);
    }

    public @NotNull CompletableFuture<Void> loadAllDependencies(@NotNull final Class<?> clazz) {
        return this.loadAllDependencies(clazz, ForkJoinPool.commonPool());
    }

    public @NotNull CompletableFuture<Void> loadAllDependencies(
            @NotNull final DependencyProvider<?> provider,
            @NotNull final Executor executor
    ) {
        return this.loadAllDependencies((Object) provider, executor);
    }

    public @NotNull CompletableFuture<Void> loadAllDependencies(
            @NotNull final DependencyProvider<?> provider
    ) {
        return this.loadAllDependencies(provider, ForkJoinPool.commonPool());
    }

    public static @NotNull DependencyEngine createNew(@NotNull final Path basePath) {
        return DependencyEngine.createNew(basePath,true);
    }

    public static @NotNull DependencyEngine createNew(@NotNull final Path basePath, final boolean addDefaultLoader) {
        return DependencyEngine.createNew(basePath,addDefaultLoader,Throwable::printStackTrace);
    }

    public static @NotNull DependencyEngine createNew(
            @NotNull final Path basePath,
            final boolean addDefaultLoader,
            @NotNull final Consumer<@NotNull DependencyLoadException> exceptionConsumer) {
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
}
