package com.heretere.hdl.exception;

import org.jetbrains.annotations.NotNull;

public class DependencyLoadException extends RuntimeException {

    public DependencyLoadException(@NotNull final Throwable cause) {
        super(cause);
    }

}
