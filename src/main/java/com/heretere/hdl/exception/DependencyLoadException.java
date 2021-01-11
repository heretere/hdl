package com.heretere.hdl.exception;

import org.jetbrains.annotations.NotNull;

public class DependencyLoadException extends RuntimeException {

    public DependencyLoadException() {
        super();
    }

    public DependencyLoadException(@NotNull final String message, @NotNull final Throwable cause) {
        super(message, cause);
    }

    public DependencyLoadException(@NotNull final String message) {
        super(message);
    }

    public DependencyLoadException(@NotNull final Throwable cause) {
        super(cause);
    }

}
