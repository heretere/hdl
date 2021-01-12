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

package com.heretere.hdl.dependency.maven;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.heretere.hdl.dependency.DependencyLoader;
import com.heretere.hdl.dependency.DependencyProvider;
import com.heretere.hdl.relocation.annotation.RelocationInfo;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.Set;

public final class MavenDependencyProvider extends DependencyProvider<@NotNull MavenDependencyInfo> {
    private final @NotNull Set<@NotNull MavenRepositoryInfo> repos;
    private final @NotNull Set<@NotNull RelocationInfo> relocations;

    private MavenDependencyProvider(final @NotNull Builder builder) {
        super(builder);
        this.repos = ImmutableSet.copyOf(builder.repos);
        this.relocations = ImmutableSet.copyOf(builder.relocations);
    }

    /**
     * @return {@link Builder}
     */
    @Contract("-> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * @return {@link ImmutableSet}
     */
    public @NotNull Set<MavenRepositoryInfo> getRepositories() {
        return this.repos;
    }

    /**
     * @return {@link ImmutableSet}
     */
    public @NotNull Set<RelocationInfo> getRelocations() {
        return this.relocations;
    }

    public static final class Builder extends DependencyProvider.Builder<@NotNull MavenDependencyInfo> {

        private final @NotNull Set<@NotNull MavenRepositoryInfo> repos;
        private final @NotNull Set<@NotNull RelocationInfo> relocations;

        private Builder() {
            super();
            this.repos = Sets.newHashSet();
            this.relocations = Sets.newHashSet();
        }

        /**
         * {@inheritDoc}
         *
         * @see MavenDependencyInfo
         */
        @Override
        public @NotNull Builder dependency(final @NotNull MavenDependencyInfo dependency) {
            super.dependency(dependency);
            return this;
        }

        /**
         * Adds a maven dependency into the configurations of this builder.
         *
         * @param separator  The separator to use instead of '.' or '/'.
         * @param groupId    The group id of the dependency.
         * @param artifactId The artifact id of the dependency.
         * @param version    The version of the dependency.
         * @return The same instance.
         */
        public @NotNull Builder dependency(
            final @NotNull String separator,
            final @NotNull String groupId,
            final @NotNull String artifactId,
            final @NotNull String version
        ) {
            return this.dependency(MavenDependencyInfo.of(separator, groupId, artifactId, version));
        }

        /**
         * {@link Builder#dependency(String, String, String, String)}.
         */
        public @NotNull Builder dependency(
            final @NotNull String separator,
            final @NotNull String singleLineDependency
        ) {
            return this.dependency(MavenDependencyInfo.of(separator, singleLineDependency));
        }

        /**
         * {@link Builder#dependency(String, String, String, String)}.
         */
        public @NotNull Builder dependency(
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

        /**
         * {@link Builder#dependency(String, String, String, String)}.
         */
        public @NotNull Builder dependency(
            final @NotNull String singleLineDependency
        ) {
            return this.dependency(MavenDependencyInfo.of(DependencyLoader.DEFAULT_SEPARATOR, singleLineDependency));
        }

        /**
         * @param repository    The repository.
         * @return              The same instance.
         *
         * @see MavenRepositoryInfo
         */
        public @NotNull Builder repository(final @NotNull MavenRepositoryInfo repository) {
            this.repos.add(repository);
            return this;
        }

        /**
         *
         * @param url   String URL representation of a maven repository.
         * @return      The same instance.
         */
        public @NotNull Builder repository(final @NotNull String url) {
            return this.repository(MavenRepositoryInfo.of(url));
        }

        /**
         *
         * @param url   URL representation of a maven repository.
         * @return      The same instance.
         */
        public @NotNull Builder repository(final @NotNull URL url) {
            return this.repository(MavenRepositoryInfo.of(url));
        }

        /**
         * @param relocation The relocation.
         * @return           The same instance.
         *
         * @see RelocationInfo
         */
        public @NotNull Builder relocation(final @NotNull RelocationInfo relocation) {
            this.relocations.add(relocation);
            return this;
        }

        /**
         * @param from      Original package destination name.
         * @param to        Target package destination name.
         * @param separator The separator to use instead of '.' or '/'.
         * @return The same instance.
         */
        public @NotNull Builder relocation(
            final @NotNull String from,
            final @NotNull String to,
            final @NotNull String separator
        ) {
            return this.relocation(RelocationInfo.of(from, to, separator));
        }

        /**
         * {@link Builder#relocation(String, String, String)}.
         */
        public @NotNull Builder relocation(
            final @NotNull String from,
            final @NotNull String to
        ) {
            return this.relocation(RelocationInfo.of(from, to, DependencyLoader.DEFAULT_SEPARATOR));
        }

        /**
         * {@inheritDoc}
         */
        @Override public @NotNull MavenDependencyProvider build() {
            super.build();
            return new MavenDependencyProvider(this);
        }
    }
}
