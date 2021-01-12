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

package com.heretere.hdl.dependency.maven.builder;

import com.google.common.collect.ImmutableSet;
import com.heretere.hdl.dependency.builder.DependencyProvider;
import com.heretere.hdl.dependency.maven.MavenDependencyInfo;
import com.heretere.hdl.dependency.maven.MavenRepositoryInfo;
import com.heretere.hdl.relocation.annotation.RelocationInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class MavenDependencyProvider implements DependencyProvider<MavenDependencyInfo> {
    private final @NotNull Set<@NotNull MavenRepositoryInfo> repositories;
    private final @NotNull Set<@NotNull MavenDependencyInfo> dependencies;
    private final @NotNull Set<@NotNull RelocationInfo> relocations;

    MavenDependencyProvider(
        final @NotNull Set<@NotNull MavenRepositoryInfo> repositories,
        final @NotNull Set<@NotNull MavenDependencyInfo> dependencies,
        final @NotNull Set<@NotNull RelocationInfo> relocations
    ) {
        this.repositories = repositories;
        this.dependencies = dependencies;
        this.relocations = relocations;
    }

    @Override public @NotNull Set<@NotNull MavenDependencyInfo> getDependencies() {
        return this.dependencies;
    }

    /**
     * @return {@link ImmutableSet}
     */
    public @NotNull Set<@NotNull MavenRepositoryInfo> getRepositories() {
        return this.repositories;
    }

    /**
     * @return {@link ImmutableSet}
     */
    public @NotNull Set<@NotNull RelocationInfo> getRelocations() {
        return this.relocations;
    }
}
