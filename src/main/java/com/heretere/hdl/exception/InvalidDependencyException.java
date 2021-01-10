package com.heretere.hdl.exception;

import org.jetbrains.annotations.NotNull;

/**
 * When a dependency isn't able to process this error is thrown.
 */
public class InvalidDependencyException extends RuntimeException {

    public InvalidDependencyException(String message) {
        super(message);
    }
}
