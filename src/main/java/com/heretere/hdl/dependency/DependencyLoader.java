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
import com.heretere.hdl.dependency.maven.MavenDependencyLoader;
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
 * @see MavenDependencyLoader
 */
public abstract class DependencyLoader<@NotNull T extends Dependency> {
    public static final @NotNull String DEFAULT_SEPARATOR = "|";

    private final @NotNull Path basePath;
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

    protected @NotNull Path getBasePath() {
        return this.basePath;
    }

    protected @NotNull List<@NotNull T> getDependencies() {
        return this.dependencies;
    }

    public void addDependency(final @NotNull T dependency) {
        this.dependencies.add(dependency);
    }

    public abstract void loadDependenciesFrom(@NotNull Object object);

    public abstract void downloadDependencies() throws IOException;

    public abstract void loadDependencies(@NotNull URLClassLoader classLoader) throws NoSuchMethodException,
        MalformedURLException, InvocationTargetException, IllegalAccessException;
}
