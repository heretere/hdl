package com.heretere.hdl.dependency;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

/**
 * Abstract implementation of a dependency loader.
 *
 * @param <D> Type of dependency implementation.
 * @see com.heretere.hdl.dependency.maven.MavenDependencyLoader
 */
public abstract class DependencyLoader<@NotNull D extends Dependency> {
    public static final @NotNull String DEFAULT_SEPARATOR = "|";

    private final @NotNull Path basePath;
    private final @NotNull List<@NotNull D> dependencies;

    protected DependencyLoader(final @NotNull Path basePath) {
        this.basePath = basePath;
        this.dependencies = Lists.newArrayList();
        this.openClassLoaderJava9();
    }

    protected DependencyLoader(
        final @NotNull Path basePath,
        final @NotNull String storageDestination
    ) {
        this(basePath.resolve(storageDestination));
    }

    /**
     * Gives this module access to the URLClassLoader.
     * Thanks lucko :)
     * Modified from: lucko/LuckPerms repo on GitHub (ReflectiveClassLoader)
     */
    @SuppressWarnings("JavaReflectionInvocation")
    private void openClassLoaderJava9() {
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            try {
                final Class<?> moduleClass = Class.forName("java.lang.Module");
                final Method getModuleMethod = Class.class.getMethod("getModule");
                final Method addOpensMethod = moduleClass.getMethod("addOpens", String.class, moduleClass);

                final Object urlClassLoaderModule = getModuleMethod.invoke(URLClassLoader.class);
                final Object thisModule = getModuleMethod.invoke(DependencyLoader.class);

                addOpensMethod.invoke(urlClassLoaderModule, URLClassLoader.class.getPackage().getName(), thisModule);
            } catch (Exception ignored) {
                //Will throw error on <Java9
            }
            return null;
        });
    }

    protected @NotNull Path getBasePath() {
        return this.basePath;
    }

    protected @NotNull List<@NotNull D> getDependencies() {
        return this.dependencies;
    }

    public void addDependency(final @NotNull D dependency) {
        this.dependencies.add(dependency);
    }

    public abstract void loadDependenciesFrom(@NotNull Object object);

    public abstract void downloadDependencies() throws IOException;

    public abstract void relocateDependencies() throws IllegalAccessException, InstantiationException,
        InvocationTargetException, IOException, NoSuchMethodException, ClassNotFoundException;

    public abstract void loadDependencies(@NotNull URLClassLoader classLoader) throws NoSuchMethodException,
        MalformedURLException, InvocationTargetException, IllegalAccessException;
}
