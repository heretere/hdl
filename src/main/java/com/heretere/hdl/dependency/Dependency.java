package com.heretere.hdl.dependency;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Base dependency abstraction used to define basic information for dependency retrieval.
 */
public interface Dependency {
    /**
     * If there is an issue downloading the dependency this is used to get the manual download location.
     *
     * @param baseURL the baseURL of a dependency url
     * @return The manual download url.
     * @throws MalformedURLException If the URL wasn't formatted correctly.
     */
    @NotNull URL getManualDownloadURL(@NotNull String baseURL) throws MalformedURLException;

    /**
     * Used to get the download url of a dependency.
     *
     * @param baseURL the baseURL of a dependency url
     * @return The download url.
     * @throws MalformedURLException If the URL wasn't formatted correctly.
     */
    @NotNull URL getDownloadURL(@NotNull String baseURL) throws MalformedURLException;

    /**
     * Used to get where the dependency should be stored on download.
     * The path is relative.
     *
     * @return The downloaded location of this dependency.
     */
    @NotNull String getDownloadedFileName();

    /**
     * Used to get the name of the dependency for logging information.
     *
     * @return The name of this dependency.
     */
    @NotNull String getName();
}
