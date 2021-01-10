package com.heretere.hdl.relocation.annotation;

import com.heretere.hdl.dependency.DependencyProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class RelocationRule {
    private final String from, to, separator;

    private RelocationRule(
            @NotNull final String from,
            @NotNull final String to,
            @NotNull final String separator
    ) {
        this.from = from;
        this.to = to;
        this.separator = separator;
    }

    @NotNull
    public String getFrom() {
        return this.from;
    }

    @NotNull
    public String getSeparator() {
        return this.separator;
    }

    @NotNull
    public String getTo() {
        return this.to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelocationRule that = (RelocationRule) o;
        return this.from.equals(that.from) && this.to.equals(that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.from, this.to);
    }

    public static RelocationRule of(
            @NotNull final String from,
            @NotNull final String to,
            @NotNull final String separator
    ) {
        return new RelocationRule(from, to, separator);
    }

    public static RelocationRule of(
            @NotNull final String from,
            @NotNull final String to
    ) {
        return new RelocationRule(from, to, DependencyProvider.DEFAULT_SEPARATOR);
    }

    public static RelocationRule of(Relocation relocation) {
        return new RelocationRule(relocation.from(),relocation.to(),relocation.separator());
    }

}
