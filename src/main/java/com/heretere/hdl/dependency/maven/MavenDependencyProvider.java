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

    private @NotNull final Set<@NotNull MavenRepositoryInfo> repos;
    private @NotNull final Set<@NotNull RelocationInfo> relocations;

    private MavenDependencyProvider(@NotNull final Builder builder) {
        super(builder);
        this.repos = ImmutableSet.copyOf(builder.repos);
        this.relocations = ImmutableSet.copyOf(builder.relocations);
    }

    public @NotNull Set<MavenRepositoryInfo> getRepositories() {
        return this.repos;
    }

    public @NotNull Set<RelocationInfo> getRelocations() {
        return this.relocations;
    }

    public static final class Builder extends DependencyProvider.Builder<@NotNull MavenDependencyInfo> {

        private @NotNull final Set<@NotNull MavenRepositoryInfo> repos;
        private @NotNull final Set<@NotNull RelocationInfo> relocations;

        private Builder() {
            super();
            this.repos = Sets.newHashSet();
            this.relocations = Sets.newHashSet();
        }

        @Override
        public @NotNull Builder dependency(@NotNull final MavenDependencyInfo dependency) {
            super.dependency(dependency);
            return this;
        }

        public @NotNull Builder dependency(
                @NotNull final String separator,
                @NotNull final String groupId,
                @NotNull final String artifactId,
                @NotNull final String version
        ) {
            return this.dependency(MavenDependencyInfo.of(separator,groupId,artifactId,version));
        }

        public @NotNull Builder dependency(
                @NotNull final String separator,
                @NotNull final String singleLineDependency
        ) {
            return this.dependency(MavenDependencyInfo.of(separator,singleLineDependency));
        }

        public @NotNull Builder dependency(
                @NotNull final String groupId,
                @NotNull final String artifactId,
                @NotNull final String version
        ) {
            return this.dependency(MavenDependencyInfo.of(DependencyLoader.DEFAULT_SEPARATOR,groupId,artifactId,version));
        }

        public @NotNull Builder dependency(
                @NotNull final String singleLineDependency
        ) {
            return this.dependency(MavenDependencyInfo.of(DependencyLoader.DEFAULT_SEPARATOR,singleLineDependency));
        }

        public @NotNull Builder repository(@NotNull final MavenRepositoryInfo repository) {
            this.repos.add(repository);
            return this;
        }

        public @NotNull Builder repository(@NotNull final String url) {
            return this.repository(MavenRepositoryInfo.of(url));
        }

        public @NotNull Builder repository(@NotNull final URL url) {
            return this.repository(MavenRepositoryInfo.of(url));
        }

        public @NotNull Builder relocation(@NotNull final RelocationInfo repository) {
            this.relocations.add(repository);
            return this;
        }

        public @NotNull Builder relocation(
                @NotNull final String from,
                @NotNull final String to,
                @NotNull final String separator
        ) {
            return this.relocation(RelocationInfo.of(from,to,separator));
        }

        public @NotNull Builder relocation(
                @NotNull final String from,
                @NotNull final String to
        ) {
            return this.relocation(RelocationInfo.of(from,to,DependencyLoader.DEFAULT_SEPARATOR));
        }

        @Override
        public @NotNull MavenDependencyProvider build() {
            return new MavenDependencyProvider(this);
        }
    }

    @Contract("-> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }
}
