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

package com.heretere.hdl.relocation;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.heretere.hdl.dependency.DependencyLoader;
import com.heretere.hdl.dependency.RelocatableDependency;
import com.heretere.hdl.dependency.maven.MavenDependency;
import com.heretere.hdl.dependency.maven.MavenDependencyLoader;
import com.heretere.hdl.relocation.annotation.Relocation;
import com.heretere.hdl.relocation.annotation.RelocationRule;
import com.heretere.hdl.relocation.classloader.IsolatedClassLoader;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Set;

public final class Relocator {
    private static final MavenDependency ASM;
    private static final MavenDependency ASM_COMMONS;
    private static final MavenDependency JAR_RELOCATOR;

    static {
        ASM = new MavenDependency("|", "org|ow2|asm:asm:7.0");
        ASM_COMMONS = new MavenDependency("|", "org|ow2|asm:asm-commons:7.0");
        JAR_RELOCATOR = new MavenDependency("|", "me|lucko:jar-relocator:1.4");
    }

    private final @NotNull Path basePath;

    private @Nullable IsolatedClassLoader isolatedClassLoader;
    private @NotNull final Constructor<?> jarRelocatorConstructor;
    private @NotNull final Method jarRelocatorRunMethod;
    private @NotNull final Constructor<?> relocationConstructor;


    public Relocator(@NotNull final Path basePath) throws IOException, ClassNotFoundException, NoSuchMethodException,
        InvocationTargetException, IllegalAccessException {
        this.basePath = basePath;
        AccessController.doPrivileged((PrivilegedAction<?>) () -> this.isolatedClassLoader = new IsolatedClassLoader());

        DependencyLoader<MavenDependency> dependencyHandler =
            new MavenDependencyLoader(this.basePath.resolve("relocator"));
        dependencyHandler.addDependency(ASM);
        dependencyHandler.addDependency(ASM_COMMONS);
        dependencyHandler.addDependency(JAR_RELOCATOR);

        dependencyHandler.downloadDependencies();
        dependencyHandler.loadDependencies(this.isolatedClassLoader);

        Class<?> jarRelocatorClass = this.isolatedClassLoader.loadClass("me.lucko.jarrelocator.JarRelocator");
        Class<?> relocationClass = this.isolatedClassLoader.loadClass("me.lucko.jarrelocator.Relocation");

        this.jarRelocatorConstructor = jarRelocatorClass.getConstructor(
            File.class,
            File.class,
            Collection.class
        );
        this.jarRelocatorRunMethod = jarRelocatorClass.getMethod("run");

        this.relocationConstructor = relocationClass.getConstructor(
            String.class,
            String.class,
            Collection.class,
            Collection.class
        );
    }

    public void relocate(
            @NotNull final Collection<@NotNull RelocationRule> relocations,
            @NotNull final RelocatableDependency dependency
    ) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Set<Object> rules = Sets.newLinkedHashSet();

        for (RelocationRule relocation : relocations) {
            rules.add(this.relocationConstructor.newInstance(
                StringUtils.replace(relocation.getFrom(), relocation.getSeparator(), "."),
                StringUtils.replace(relocation.getTo(), relocation.getSeparator(), "."),
                Lists.newArrayList(),
                Lists.newArrayList()
            ));
        }

        this.jarRelocatorRunMethod.invoke(this.jarRelocatorConstructor.newInstance(
            this.basePath.resolve(dependency.getDownloadedFileName()).toFile(),
            this.basePath.resolve(dependency.getRelocatedFileName()).toFile(),
            rules
        ));
    }
}
