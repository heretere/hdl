package com.heretere.hdl.dependency.maven;

import com.heretere.hdl.dependency.maven.annotation.MavenRepository;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.Objects;

public final class MavenRepositoryInfo {
    private final @NotNull String url;

    private MavenRepositoryInfo(final @NotNull String url) {
        this.url = url;
    }

    @Contract("_ -> new")
    public static @NotNull MavenRepositoryInfo of(final @NotNull URL url) {
        return new MavenRepositoryInfo(String.valueOf(url));
    }

    @Contract("_ -> new")
    public static @NotNull MavenRepositoryInfo of(final @NotNull MavenRepository repository) {
        return new MavenRepositoryInfo(repository.value());
    }

    @Contract("_ -> new")
    public static @NotNull MavenRepositoryInfo of(final @NotNull String url) {
        return new MavenRepositoryInfo(url);
    }

    public @NotNull String getURL() {
        return url;
    }

    @Contract("null -> false")
    @Override public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MavenRepositoryInfo that = (MavenRepositoryInfo) o;
        return this.url.equals(that.url);
    }

    @Override public int hashCode() {
        return Objects.hash(url);
    }
}
