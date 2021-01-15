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

package com.heretere.hdl.spigot;

import com.heretere.hdl.DependencyEngine;
import com.heretere.hdl.exception.DependencyLoadException;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Used to automatically load dependencies for a plugin.
 * It extends JavaPlugin.
 */
public abstract class DependencyPlugin extends JavaPlugin {
    /**
     * The dependency engine instance.
     */
    private final @NotNull DependencyEngine dependencyEngine;

    /**
     * Creates a new {@link DependencyPlugin} instance.
     */
    protected DependencyPlugin() {
        this.dependencyEngine = DependencyEngine.createNew(this.getDataFolder().toPath().resolve("dependencies"));
        this.dependencyEngine.addDependencyLoader(new SpigotDependencyLoader(this.getDataFolder().toPath()));
    }

    @Override public final void onLoad() {
        super.onLoad();

        this.dependencyEngine.loadAllDependencies(this.getClass())
                             .exceptionally(e -> {
                                 this.dependencyEngine.getErrors().add(e);
                                 return null;
                             })
                             .join();

        if (!this.dependencyEngine.getErrors().isEmpty()) {
            final Set<Throwable> genericErrors = new HashSet<>();
            final Set<DependencyLoadException> dependencyErrors = new HashSet<>();

            this.dependencyEngine.getErrors().forEach(error -> {
                if (error instanceof DependencyLoadException) {
                    dependencyErrors.add((DependencyLoadException) error);
                } else {
                    genericErrors.add(error);
                }
            });

            this.fail(genericErrors, dependencyErrors);
        } else {
            this.load();
        }
    }

    @Override public final void onEnable() {
        super.onEnable();

        if (this.dependencyEngine.getErrors().isEmpty()) {
            this.enable();
        }
    }

    @Override public final void onDisable() {
        super.onDisable();

        if (this.dependencyEngine.getErrors().isEmpty()) {
            this.disable();
        }
    }

    /**
     * Called when the dependency engine fails to load the supplied dependencies.
     * Supplies a set of errors that occurred during the process.
     * You should provide a manual process to downloading the dependencies here.
     *
     * @param genericErrors    The generic errors that occurred during the dependency loading process.
     * @param dependencyErrors The errors related to a specific dependency that occurred during the loading process.
     */
    protected abstract void fail(
        @NotNull Set<@NotNull Throwable> genericErrors,
        @NotNull Set<@NotNull DependencyLoadException> dependencyErrors
    );

    /**
     * Similar to {@link JavaPlugin#onLoad()}, but called after dependencies on loaded.
     */
    protected abstract void load();

    /**
     * Similar to {@link JavaPlugin#onEnable()}, but called after dependencies on loaded.
     */
    protected abstract void enable();

    /**
     * Similar to {@link JavaPlugin#onDisable()}, but called after dependencies on loaded.
     */
    protected abstract void disable();

    /**
     * @return The current dependency engine instance for this dependency plugin.
     */
    public @NotNull DependencyEngine getDependencyEngine() {
        return this.dependencyEngine;
    }
}
