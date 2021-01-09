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

public abstract class DependencyLoader<T extends Dependency> {
    private final @NotNull Path basePath;
    private final @NotNull List<@NotNull T> dependencies;

    protected DependencyLoader(final @NotNull Path basePath) {
        this.basePath = basePath;
        this.dependencies = Lists.newArrayList();
        this.openClassLoaderJava9();
    }

    /**
     * Gives this module access to the URLClassLoader.
     * Thanks lucko :)
     * Modified from: lucko/LuckPerms repo on GitHub (ReflectiveClassLoader)
     */
    @SuppressWarnings("JavaReflectionInvocation") private void openClassLoaderJava9() {
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            try {
                Class<?> moduleClass = Class.forName("java.lang.Module");
                Method getModuleMethod = Class.class.getMethod("getModule");
                Method addOpensMethod = moduleClass.getMethod("addOpens", String.class, moduleClass);

                Object urlClassLoaderModule = getModuleMethod.invoke(URLClassLoader.class);
                Object thisModule = getModuleMethod.invoke(DependencyLoader.class);

                addOpensMethod.invoke(urlClassLoaderModule, URLClassLoader.class.getPackage().getName(), thisModule);
            } catch (Exception ignored) {
                //Will throw error on <Java9
            }
            return null;
        });
    }

    public void addDependency(final @NotNull T dependency) {
        this.dependencies.add(dependency);
    }

    public int getSize() {
        return this.dependencies.size();
    }

    public abstract void loadDependenciesFromClass(@NotNull Class<?> clazz);

    public abstract void downloadDependencies() throws IOException;

    public abstract void relocateDependencies() throws IllegalAccessException, InstantiationException,
        InvocationTargetException, IOException, NoSuchMethodException, ClassNotFoundException;

    public abstract void loadDependencies(@NotNull URLClassLoader classLoader) throws NoSuchMethodException,
        MalformedURLException, InvocationTargetException, IllegalAccessException;

    protected @NotNull Path getBasePath() {
        return this.basePath;
    }

    protected @NotNull List<@NotNull T> getDependencies() {
        return this.dependencies;
    }
}
