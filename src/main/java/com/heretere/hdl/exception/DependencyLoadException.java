package com.heretere.hdl.exception;

import org.jetbrains.annotations.NotNull;

public class DependencyLoadException extends RuntimeException {
    public DependencyLoadException(final @NotNull Exception e) {
        super(e);
    }
}
