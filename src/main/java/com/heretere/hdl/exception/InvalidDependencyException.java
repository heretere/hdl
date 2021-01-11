package com.heretere.hdl.exception;

import org.jetbrains.annotations.NotNull;

/**
 * When a dependency isn't able to process this error is thrown.
 */
public class InvalidDependencyException extends RuntimeException {

    public InvalidDependencyException() {
        super();
    }

    public InvalidDependencyException(@NotNull final String message, @NotNull final Throwable cause) {
        super(message, cause);
    }

    public InvalidDependencyException(@NotNull final Throwable cause) {
        super(cause);
    }

    public InvalidDependencyException(@NotNull final String message) {
        super(message);
    }
}
