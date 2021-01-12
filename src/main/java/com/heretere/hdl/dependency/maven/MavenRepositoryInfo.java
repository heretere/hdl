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

package com.heretere.hdl.dependency.maven;

import com.heretere.hdl.dependency.maven.annotation.MavenRepository;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.Objects;

/**
 * Implementation representing {@link MavenRepository}.
 *
 * @see MavenRepository
 */
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
        return this.url;
    }

    @Contract("null -> false")
    @Override public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final MavenRepositoryInfo that = (MavenRepositoryInfo) o;
        return this.url.equals(that.url);
    }

    @Override public int hashCode() {
        return Objects.hash(this.url);
    }
}
