package com.heretere.hdl.dependency;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Base implementation for declaring dependencies with a builder pattern as opposed to annotations.
 *
 * @param <D> Type of dependency implementation.
 *
 * @see com.heretere.hdl.dependency.maven.MavenDependencyProvider
 */
public class DependencyProvider<@NotNull D extends Dependency> {
    private final @NotNull List<@NotNull D> dependencies;

    protected DependencyProvider(final @NotNull Builder<@NotNull D> builder) {
        this.dependencies = ImmutableList.copyOf(builder.dependencies);
    }

    /**
     * @return {@link ImmutableList}
     */
    public @NotNull List<@NotNull D> getDependencies() {
        return this.dependencies;
    }

    /**
     * Base implementation for building a {@link DependencyProvider}.
     * @param <D>
     */
    public static class Builder<D extends Dependency> {
        private final List<@NotNull D> dependencies;
        private boolean isBuilt;

        protected Builder() {
            this.dependencies = Lists.newArrayList();
        }

        /**
         * @param dependency The dependency.
         * @return The same instance.
         */
        public @NotNull Builder<@NotNull D> dependency(final @NotNull D dependency) {
            this.dependencies.add(dependency);
            return this;
        }

        /**
         * Composes this builder with designated configurations into a {@link DependencyProvider}.
         *
         * @throws IllegalStateException if this method has already been invoked on this builder.
         * @return {@link DependencyProvider} based of builder configurations.
         */
        @Contract("-> new")
        public @NotNull DependencyProvider<@NotNull D> build() {
            if (this.isBuilt) {
                throw new IllegalStateException("DependencyProvider already built");
            }
            this.isBuilt = true;
            return new DependencyProvider<>(this);
        }

    }

}
