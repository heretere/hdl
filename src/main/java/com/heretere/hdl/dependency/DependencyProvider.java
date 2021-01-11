package com.heretere.hdl.dependency;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DependencyProvider<@NotNull T extends Dependency> {
    protected final @NotNull List<@NotNull T> dependencies;

    protected DependencyProvider(@NotNull final Builder<@NotNull T> builder) {
        this.dependencies = ImmutableList.copyOf(builder.dependencies);
    }

    public @NotNull List<@NotNull T> getDependencies() {
        return this.dependencies;
    }

    public static class Builder<T extends Dependency> {
        protected final List<@NotNull T> dependencies;

        protected Builder() {
            this.dependencies = Lists.newArrayList();
        }

        public @NotNull Builder<@NotNull T> dependency(@NotNull final T dependency) {
            this.dependencies.add(dependency);
            return this;
        }

        public @NotNull DependencyProvider<@NotNull T> build() {
            return new DependencyProvider<>(this);
        }

    }


}
