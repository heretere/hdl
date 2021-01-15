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

package com.heretere.hdl.spigot.builder;

import com.heretere.hdl.dependency.builder.DependencyBuilder;
import com.heretere.hdl.spigot.SpigotDependencyInfo;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Used to create new {@link SpigotDependencyProvider} instances based on the passed in builder methods.
 */
public final class SpigotDependencyProviderBuilder implements DependencyBuilder<SpigotDependencyInfo> {
    /**
     * The dependencies attached to this dependency provider.
     */
    private final @NotNull Set<@NotNull SpigotDependencyInfo> dependencies;

    /**
     * Creates a new builder instance.
     */
    public SpigotDependencyProviderBuilder() {
        this.dependencies = new HashSet<>();
    }

    /**
     * /**
     * Creates a new builder instance.
     *
     * @return A new {@link SpigotDependencyProviderBuilder} instance.
     */
    @Contract("-> new")
    public static SpigotDependencyProviderBuilder builder() {
        return new SpigotDependencyProviderBuilder();
    }

    @Contract("_ -> this")
    @Override public @NotNull SpigotDependencyProviderBuilder dependency(
        final @NotNull SpigotDependencyInfo dependency
    ) {
        this.dependencies.add(dependency);
        return this;
    }

    /**
     * Adds a new dependency with the specified info.
     *
     * @param pluginName The name of the plugin when loaded on the server.
     * @param pluginId   The id of the plugin on the spigotmc.org forums.
     * @return this
     */
    @Contract("_,_ -> this")
    public @NotNull SpigotDependencyProviderBuilder dependency(
        final @NotNull String pluginName,
        final int pluginId
    ) {
        this.dependencies.add(SpigotDependencyInfo.of(pluginName, pluginId));
        return this;
    }

    @Contract("-> new")
    @Override public @NotNull SpigotDependencyProvider build() {
        return new SpigotDependencyProvider(this.dependencies);
    }
}
