package com.heretere.hdl;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public abstract class DependencyPlugin extends JavaPlugin {
    private final @NotNull DependencyEngine dependencyEngine;

    protected DependencyPlugin() {
        this.dependencyEngine = DependencyEngine.createNew(this.getDataFolder().toPath().resolve("dependencies"));
    }

    @Override
    public final void onLoad() {
        super.onLoad();

        this.dependencyEngine.loadAllDependencies(this.getClass())
                .exceptionally(e -> {
                    this.getLogger().log(Level.SEVERE,"An error occurred while loading dependencies",e);
                    return null;
                })
                .join();

        this.load();
    }

    @Override
    public final void onEnable() {
        super.onDisable();

        this.enable();
    }

    @Override
    public final void onDisable() {
        super.onDisable();

        this.disable();
    }

    protected abstract void load();

    protected abstract void enable();

    protected abstract void disable();

    public @NotNull DependencyEngine getDependencyEngine() {
        return dependencyEngine;
    }
}
