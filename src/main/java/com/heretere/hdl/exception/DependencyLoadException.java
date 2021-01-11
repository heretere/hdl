package com.heretere.hdl.exception;

import org.jetbrains.annotations.NotNull;

public class DependencyLoadException extends RuntimeException {
    public DependencyLoadException() {
        super();
    }

    public DependencyLoadException(
        final @NotNull String message,
        final @NotNull Throwable cause
    ) {
        super(message, cause);
    }

    public DependencyLoadException(final @NotNull String message) {
        super(message);
    }

    public DependencyLoadException(final @NotNull Throwable cause) {
        super(cause);
    }

}
