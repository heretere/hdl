package com.heretere.hdl.exception;

import org.jetbrains.annotations.NotNull;

/**
 * When a dependency isn't able to process this error is thrown.
 */
public class InvalidDependencyException extends RuntimeException {

    public InvalidDependencyException() {
        super();
    }

    public InvalidDependencyException(
        final @NotNull String message,
        final @NotNull Throwable cause
    ) {
        super(message, cause);
    }

    public InvalidDependencyException(final @NotNull Throwable cause) {
        super(cause);
    }

    public InvalidDependencyException(final @NotNull String message) {
        super(message);
    }
}
