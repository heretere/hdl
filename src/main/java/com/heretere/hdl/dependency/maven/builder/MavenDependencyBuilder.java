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

import com.google.common.collect.Sets;
import com.heretere.hdl.dependency.DependencyLoader;
import com.heretere.hdl.dependency.builder.DependencyBuilder;
import com.heretere.hdl.dependency.builder.DependencyProvider;
import com.heretere.hdl.dependency.maven.MavenDependencyInfo;
import com.heretere.hdl.dependency.maven.MavenRepositoryInfo;
import com.heretere.hdl.relocation.annotation.RelocationInfo;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.Set;

public final class MavenDependencyBuilder implements DependencyBuilder<MavenDependencyInfo> {
    private final @NotNull Set<@NotNull MavenRepositoryInfo> repositories;
    private final @NotNull Set<@NotNull MavenDependencyInfo> dependencies;
    private final @NotNull Set<@NotNull RelocationInfo> relocations;

    public MavenDependencyBuilder() {
        this.repositories = Sets.newHashSet();
        this.dependencies = Sets.newHashSet();
        this.relocations = Sets.newHashSet();
    }

    @Contract("_ -> this")
    @Override public @NotNull MavenDependencyBuilder dependency(
        final @NotNull MavenDependencyInfo dependency
    ) {
        this.dependencies.add(dependency);
        return this;
    }


    @Contract("_,_,_ -> this")
    public @NotNull MavenDependencyBuilder dependency(
        final @NotNull String groupId,
        final @NotNull String artifactId,
        final @NotNull String version
    ) {
        return this.dependency(MavenDependencyInfo.of(
            DependencyLoader.DEFAULT_SEPARATOR,
            groupId,
            artifactId,
            version
        ));
    }

    @Contract("_,_,_,_ -> this")
    public @NotNull MavenDependencyBuilder dependency(
        final @NotNull String separator,
        final @NotNull String groupId,
        final @NotNull String artifactId,
        final @NotNull String version
    ) {
        return this.dependency(MavenDependencyInfo.of(separator, groupId, artifactId, version));
    }

    @Contract("_,_ -> this")
    public @NotNull MavenDependencyBuilder dependency(
        final @NotNull String separator,
        final @NotNull String singleLineDependency
    ) {
        return this.dependency(MavenDependencyInfo.of(separator, singleLineDependency));
    }

    @Contract("_ -> this")
    public @NotNull MavenDependencyBuilder dependency(
        final @NotNull String singleLineDependency
    ) {
        return this.dependency(MavenDependencyInfo.of(DependencyLoader.DEFAULT_SEPARATOR, singleLineDependency));
    }

    /**
     * @param repository The repository.
     * @return The same instance.
     * @see MavenRepositoryInfo
     */
    @Contract("_ -> this")
    public MavenDependencyBuilder repository(final @NotNull MavenRepositoryInfo repository) {
        this.repositories.add(repository);
        return this;
    }

    /**
     * @param url String URL representation of a maven repository.
     * @return The same instance.
     */
    @Contract("_ -> this")
    public MavenDependencyBuilder repository(final @NotNull String url) {
        return this.repository(MavenRepositoryInfo.of(url));
    }

    /**
     * @param url URL representation of a maven repository.
     * @return The same instance.
     */
    @Contract("_ -> this")
    public MavenDependencyBuilder repository(final @NotNull URL url) {
        return this.repository(MavenRepositoryInfo.of(url));
    }

    /**
     * @param relocation The relocation.
     * @return The same instance.
     * @see RelocationInfo
     */
    @Contract("_ -> this")
    public MavenDependencyBuilder relocation(final @NotNull RelocationInfo relocation) {
        this.relocations.add(relocation);
        return this;
    }

    /**
     * @param from      Original package destination name.
     * @param to        Target package destination name.
     * @param separator The separator to use instead of '.' or '/'.
     * @return The same instance.
     */
    @Contract("_,_,_ -> this")
    public MavenDependencyBuilder relocation(
        final @NotNull String from,
        final @NotNull String to,
        final @NotNull String separator
    ) {
        return this.relocation(RelocationInfo.of(from, to, separator));
    }

    @Contract("_,_ -> this")
    public MavenDependencyBuilder relocation(
        final @NotNull String from,
        final @NotNull String to
    ) {
        return this.relocation(RelocationInfo.of(from, to, DependencyLoader.DEFAULT_SEPARATOR));
    }

    @Contract("-> new")
    @Override public @NotNull DependencyProvider<MavenDependencyInfo> build() {
        return new MavenDependencyProvider(this.repositories, this.dependencies, this.relocations);
    }

    @Contract("-> new")
    public static MavenDependencyBuilder builder() {
        return new MavenDependencyBuilder();
    }
}
