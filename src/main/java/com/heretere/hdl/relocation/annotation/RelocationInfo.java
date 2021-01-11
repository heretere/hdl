package com.heretere.hdl.relocation.annotation;

import com.heretere.hdl.dependency.DependencyLoader;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class RelocationInfo {
    private final @NotNull String from, to, separator;

    protected RelocationInfo(
        final @NotNull String from,
        final @NotNull String to,
        final @NotNull String separator
    ) {
        this.from = from;
        this.to = to;
        this.separator = separator;
    }

    @Contract("_,_,_ -> new")
    public static @NotNull RelocationInfo of(
        final @NotNull String from,
        final @NotNull String to,
        final @NotNull String separator
    ) {
        return new RelocationInfo(from, to, separator);
    }

    @Contract("_,_-> new")
    public static @NotNull RelocationInfo of(
        final @NotNull String from,
        final @NotNull String to
    ) {
        return new RelocationInfo(from, to, DependencyLoader.DEFAULT_SEPARATOR);
    }

    @Contract("_ -> new")
    public static @NotNull RelocationInfo of(final @NotNull Relocation relocation) {
        return new RelocationInfo(relocation.from(), relocation.to(), relocation.separator());
    }

    public @NotNull String getFrom() {
        return this.from;
    }

    public @NotNull String getSeparator() {
        return this.separator;
    }

    public @NotNull String getTo() {
        return this.to;
    }

    @Contract("null -> false")
    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RelocationInfo that = (RelocationInfo) o;
        return this.from.equals(that.from) && this.to.equals(that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.from, this.to);
    }

}
