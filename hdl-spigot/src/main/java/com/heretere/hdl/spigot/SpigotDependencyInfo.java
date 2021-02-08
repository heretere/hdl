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

import com.heretere.hdl.dependency.Dependency;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class is used to store information about a Spigot dependency.
 */
public final class SpigotDependencyInfo implements Dependency {
    /**
     * The name of the plugin when it is loaded.
     */
    private final @NotNull String pluginName;
    /**
     * The id of the plugin on spigot.
     */
    private final int id;

    /**
     * Whether or not this dependency has been loaded.
     */
    private boolean loaded;

    private SpigotDependencyInfo(
            final @NotNull String pluginName,
            final int id
    ) {
        this.pluginName = pluginName;
        this.id = id;
    }

    /**
     * Creates a {@link SpigotDependencyInfo} from the provided params.
     *
     * @param pluginName The name of the plugin when loaded.
     * @param id         the id of the plugin on spigot.
     * @return A new {@link SpigotDependencyInfo} instance.
     */
    @Contract("_,_ -> new")
    public static @NotNull SpigotDependencyInfo of(
            final @NotNull String pluginName,
            final int id
    ) {
        return new SpigotDependencyInfo(pluginName, id);
    }

    @Override public @NotNull String getManualDownloadString() {
        return "https://www.spigotmc.org/resources/" + this.id;
    }

    @Override public @NotNull String getRelativeDownloadString() {
        return "https://api.spiget.org/v2/resources/" + this.id + "/updates/latest";
    }

    @Override public @NotNull String getDownloadedFileName() {
        return this.pluginName + ".jar";
    }

    @Override public @NotNull String getName() {
        return this.pluginName;
    }

    @Override public void setLoaded(final boolean loaded) {
        this.loaded = loaded;
    }

    @Override public boolean isLoaded() {
        return this.loaded;
    }

    /**
     * @return The id of the plugin on spigot.
     */
    public int getId() {
        return this.id;
    }
}
