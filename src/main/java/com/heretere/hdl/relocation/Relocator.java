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
import com.heretere.hdl.dependency.maven.MavenDependencyInfo;
import com.heretere.hdl.dependency.maven.MavenDependencyLoader;
import com.heretere.hdl.relocation.annotation.RelocationInfo;
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

/**
 * This class is used for relocating the packages inside of a downloaded jar.
 */
public final class Relocator {
    /**
     * The ASM Dependency information.
     */
    private static final MavenDependencyInfo ASM;
    /**
     * The ASM commons dependency information.
     */
    private static final MavenDependencyInfo ASM_COMMONS;
    /**
     * The jar relocator dependency information.
     */
    private static final MavenDependencyInfo JAR_RELOCATOR;

    static {
        ASM = MavenDependencyInfo.of("|", "org|ow2|asm:asm:7.0");
        ASM_COMMONS = MavenDependencyInfo.of("|", "org|ow2|asm:asm-commons:7.0");
        JAR_RELOCATOR = MavenDependencyInfo.of("|", "me|lucko:jar-relocator:1.4");
    }

    /**
     * The base directory for the relocator dependencies.
     */
    private final @NotNull Path basePath;
    /**
     * The constructor for the jar relocator class.
     */
    private final @NotNull Constructor<?> jarRelocatorConstructor;
    /**
     * The method to run the jar relocator.
     */
    private final @NotNull Method jarRelocatorRunMethod;
    /**
     * The relocation constructor.
     */
    private final @NotNull Constructor<?> relocationConstructor;
    /**
     * The isolated class loader instance. This is used to separate the classes used at only for relocating from
     * everywhere else.
     */
    private @Nullable IsolatedClassLoader isolatedClassLoader;


    /**
     * Creates a new relocator instance.
     *
     * @param basePath The base path for relocations.
     * @throws IOException               If access was denied for creating files.
     * @throws ClassNotFoundException    If the necessary classes weren't found after downloading.
     * @throws NoSuchMethodException     If the necessary methods weren't found after downloading.
     * @throws InvocationTargetException If there was an error while relocating the jar files.
     * @throws IllegalAccessException    If the relocator was denied access to any of the methods.
     */
    public Relocator(final @NotNull Path basePath) throws IOException, ClassNotFoundException, NoSuchMethodException,
        InvocationTargetException, IllegalAccessException {
        this.basePath = basePath;
        AccessController.doPrivileged((PrivilegedAction<?>) () -> this.isolatedClassLoader = new IsolatedClassLoader());

        final DependencyLoader<MavenDependencyInfo> dependencyHandler =
            new MavenDependencyLoader(this.basePath.resolve("relocator"));
        dependencyHandler.addDependency(ASM);
        dependencyHandler.addDependency(ASM_COMMONS);
        dependencyHandler.addDependency(JAR_RELOCATOR);

        dependencyHandler.downloadDependencies();
        dependencyHandler.loadDependencies(this.isolatedClassLoader);

        final Class<?> jarRelocatorClass = this.isolatedClassLoader.loadClass("me.lucko.jarrelocator.JarRelocator");
        final Class<?> relocationClass = this.isolatedClassLoader.loadClass("me.lucko.jarrelocator.Relocation");

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

    /**
     * Relocates the packages in a jar file based on the specified relocations.
     *
     * @param relocations A collection of {@link RelocationInfo} used for relocation information.
     * @param dependency  The dependency to relocate.
     * @throws IllegalAccessException    If the relocator was denied access to any of the methods.
     * @throws InvocationTargetException If there was an error while relocating the jar files.
     * @throws InstantiationException    If there was an error while creating a new relocation instance.
     */
    public void relocate(
        final @NotNull Collection<@NotNull RelocationInfo> relocations,
        final @NotNull RelocatableDependency dependency
    ) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        final Set<Object> rules = Sets.newLinkedHashSet();

        for (RelocationInfo relocation : relocations) {
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
