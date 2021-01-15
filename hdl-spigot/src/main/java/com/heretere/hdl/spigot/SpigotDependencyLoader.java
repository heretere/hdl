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

import com.heretere.hdl.dependency.DependencyLoader;
import com.heretere.hdl.dependency.annotation.LoaderPriority;
import com.heretere.hdl.exception.DependencyLoadException;
import com.heretere.hdl.spigot.annotation.SpigotDependency;
import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.logging.Level;

/**
 * This class is used to load spigot dependencies based on the passed in dependency info.
 */
@LoaderPriority(1)
public final class SpigotDependencyLoader extends DependencyLoader<SpigotDependencyInfo> {
    /**
     * This is used as the user agent for requesting the direct download jar link.
     */
    private static final @NotNull SimpleImmutableEntry<@NotNull String, @NotNull String> REQUEST_USER_AGENT =
        new SimpleImmutableEntry<>(
            "User-Agent",
            "HDL"
        );

    /**
     * Max time to wait for connection to API.
     */
    private static final int TIMEOUT_MS = 5000;

    /**
     * The string to be formatted into a url to download a spigot dependency.
     */
    private static final @NotNull String SPIGET_API_DOWNLOAD = "https://api.spiget.org/v2/resources/%d/download";

    /**
     * Used to detect if a response is a redirect. This in the minimum constraint.
     */
    private static final int REDIRECT_CODE_MIN = 300;

    /**
     * Used to detect if a response is a redirect. This in the maximum constraint.
     */
    private static final int REDIRECT_CODE_MAX = 310;

    /**
     * Creates a new {@link SpigotDependencyLoader} from the provided inputs.
     * Base path should be a path to the spigot plugins' folder.
     *
     * @param basePath The path to the plugins' folder.
     */
    public SpigotDependencyLoader(final @NotNull Path basePath) {
        super(basePath);
    }

    @Override public void loadDependenciesFrom(final @NotNull Object object) {
        if (object instanceof Class) {
            final Class<?> clazz = (Class<?>) object;

            if (clazz.isAnnotationPresent(SpigotDependency.class)) {
                final SpigotDependency dependency = clazz.getAnnotation(SpigotDependency.class);
                super.addDependency(SpigotDependencyInfo.of(dependency.name(), dependency.id()));
            }

            if (clazz.isAnnotationPresent(SpigotDependency.List.class)) {
                final SpigotDependency.List dependencies = clazz.getAnnotation(SpigotDependency.List.class);

                Arrays.stream(dependencies.value())
                      .forEach(dependency ->
                                   super.addDependency(SpigotDependencyInfo.of(
                                       dependency.name(),
                                       dependency.id()
                                   )));
            }
        }
    }

    private @NotNull HttpURLConnection followRedirect(final @NotNull HttpURLConnection in) throws IOException {
        return this.prepareConnection(new URL(in.getHeaderField("Location")));
    }

    private @NotNull HttpURLConnection prepareConnection(
        final @NotNull URL url
    ) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setInstanceFollowRedirects(true);
        connection.setConnectTimeout(SpigotDependencyLoader.TIMEOUT_MS);
        connection.setReadTimeout(SpigotDependencyLoader.TIMEOUT_MS);
        connection.setRequestProperty(
            SpigotDependencyLoader.REQUEST_USER_AGENT.getKey(),
            SpigotDependencyLoader.REQUEST_USER_AGENT.getValue()
        );

        if (connection.getResponseCode() >= SpigotDependencyLoader.REDIRECT_CODE_MIN
            && connection.getResponseCode() <= SpigotDependencyLoader.REDIRECT_CODE_MAX) {
            connection = this.followRedirect(connection);
        }

        return connection;
    }

    @Override public void downloadDependencies() {
        super.getDependencies()
             .parallelStream()
             .forEach(dependency -> {
                 if (Bukkit.getPluginManager().isPluginEnabled(dependency.getName())
                     || Files.exists(super.getBasePath().resolve(dependency.getDownloadedFileName()))) {
                     return;
                 }

                 try {
                     final HttpURLConnection connection =
                         this.prepareConnection(new URL(String.format(
                             SpigotDependencyLoader.SPIGET_API_DOWNLOAD,
                             dependency.getId()
                         )));

                     if (connection.getResponseCode() != HttpURLConnection.HTTP_OK ||
                         !connection.getHeaderField("content-disposition").contains(".jar")) {
                         throw new DependencyLoadException(
                             dependency,
                             "Couldn't load dependency please download from: " + dependency.getManualDownloadURL("")
                         );
                     }

                     Files.copy(
                         connection.getInputStream(),
                         super.getBasePath().resolve(dependency.getDownloadedFileName())
                     );
                 } catch (Exception e) {
                     super.addError(new DependencyLoadException(dependency, e));
                 }
             });
    }

    @Override public void loadDependencies(final @NotNull URLClassLoader classLoader) {
        super.getDependencies().forEach(dependency -> {
            if (Bukkit.getPluginManager().isPluginEnabled(dependency.getName())) {
                return;
            }

            try {
                final Plugin plugin = Bukkit.getPluginManager()
                                            .loadPlugin(super.getBasePath()
                                                             .resolve(dependency.getDownloadedFileName())
                                                             .toFile());

                if (plugin != null) {
                    plugin.getLogger().log(
                        Level.INFO,
                        () -> "Loading " + plugin.getName() + " " + plugin.getDescription().getVersion()
                    );
                    plugin.onLoad();
                }
            } catch (InvalidDescriptionException | InvalidPluginException e) {
                super.getErrors().add(new DependencyLoadException(dependency, e));
            }
        });
    }
}
