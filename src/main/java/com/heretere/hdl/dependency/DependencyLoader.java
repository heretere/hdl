/*
 * MIT License
 *
 * Copyright (c) 2021 Justin Heflin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

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
 * @param <T> Type of dependency implementation.
 * @see com.heretere.hdl.dependency.maven.MavenDependencyLoader
 */
public abstract class DependencyLoader<@NotNull T extends Dependency> {
    /**
     * The default separator used to have compatibility with gradle/maven relocation.
     */
    public static final @NotNull String DEFAULT_SEPARATOR = "|";

    /**
     * The base path for files in this dependency loader.
     */
    private final @NotNull Path basePath;
    /**
     * The dependencies that this dependency loader handles.
     */
    private final @NotNull List<@NotNull T> dependencies;

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

    /**
     * @return The base directory for dependencies in this dependency loader.
     */
    protected @NotNull Path getBasePath() {
        return this.basePath;
    }

    /**
     * @return A list of dependencies contained in this dependency loader.
     */
    protected @NotNull List<@NotNull T> getDependencies() {
        return this.dependencies;
    }

    /**
     * Adds a dependency to be handled by this dependency loader.
     *
     * @param dependency the dependency to add.
     */
    public void addDependency(final @NotNull T dependency) {
        this.dependencies.add(dependency);
    }

    /**
     * Used to load dependencies from a dependency provider or a class.
     *
     * @param object The object to load dependencies from.
     */
    public abstract void loadDependenciesFrom(@NotNull Object object);

    /**
     * Downloads handled dependencies.
     *
     * @throws IOException If there is an error storing the dependencies.
     */
    public abstract void downloadDependencies() throws IOException;

    /**
     * Loads the dependencies to be used by the plugin.
     *
     * @param classLoader The class loader to add the dependencies to.
     * @throws NoSuchMethodException     If the method in the class loader isn't found.
     * @throws MalformedURLException     If there was an error parsing the dependency url.
     * @throws InvocationTargetException If there was an error running a reflect method.
     * @throws IllegalAccessException    If access is denied for the loader.
     */
    public abstract void loadDependencies(@NotNull URLClassLoader classLoader) throws NoSuchMethodException,
        MalformedURLException, InvocationTargetException, IllegalAccessException;
}
