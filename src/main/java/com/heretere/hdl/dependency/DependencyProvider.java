package com.heretere.hdl.dependency;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.heretere.hdl.relocation.annotation.RelocationRule;

import java.util.List;
import java.util.Set;

public class DependencyProvider<T extends Dependency> {

    public static final String DEFAULT_SEPARATOR = "|";

    private DependencyProvider(Builder<T> builder) {

    }

    public static abstract class Builder<T extends Dependency> {
        private String fallbackSeparator = DEFAULT_SEPARATOR;

        private final Set<String> repositories;
        private final List<T> dependencies;
        private final Set<RelocationRule> relocations;

        public Builder() {
            this.repositories = Sets.newHashSet();
            this.dependencies = Lists.newArrayList();
            this.relocations = Sets.newHashSet();
        }

        public Builder<T> fallbackSeparator(String separator) {
            this.fallbackSeparator = separator;
            return this;
        }

        public Builder<T> addRepository(String repository) {
            this.repositories.add(repository);
            return this;
        }

        public Builder<T> addDependency(T dependency) {
            this.dependencies.add(dependency);
            return this;
        }

        public DependencyProvider<T> build() {
            return new DependencyProvider<>(this);
        }

    }


}
