package com.heretere.hdl.dependency;

import org.jetbrains.annotations.NotNull;

/**
 * Used to define dependencies that need to be relocated at runtime.
 */
public interface RelocatableDependency extends Dependency {
    /**
     * Used to get the relocated location of a dependency.
     * This path is relative.
     *
     * @return The relocation jar location of this dependency.
     */
    @NotNull String getRelocatedFileName();
}
