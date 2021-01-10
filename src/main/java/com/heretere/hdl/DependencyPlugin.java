package com.heretere.hdl;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public abstract class DependencyPlugin extends JavaPlugin {
    private DependencyEngine dependencyEngine;

    protected DependencyPlugin() {
    }

    @Override
    public final void onLoad() {
        super.onLoad();

        dependencyEngine = DependencyEngine.createNew(this.getDataFolder().toPath().resolve("dependencies"));
        dependencyEngine.loadAllDependencies(this.getClass())
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

    @Nullable
    public DependencyEngine getDependencyEngine() {
        return dependencyEngine;
    }
}
