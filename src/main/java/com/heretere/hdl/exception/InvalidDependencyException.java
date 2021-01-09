package com.heretere.hdl.exception;

import org.jetbrains.annotations.NotNull;

/**
 * When a dependency isn't able to process this error is thrown.
 */
public class InvalidDependencyException extends RuntimeException {

    /**
     * @param message The message to attach to the exception.
     */
    public InvalidDependencyException(final @NotNull String message) {
        super(message);
    }

}
