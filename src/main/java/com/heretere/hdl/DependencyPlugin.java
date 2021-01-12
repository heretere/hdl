package com.heretere.hdl;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

/**
 * Used to automatically load dependencies for a plugin.
 * It extends JavaPlugin.
 */
public abstract class DependencyPlugin extends JavaPlugin {
    /**
     * The dependency engine instance.
     */
    private final @NotNull DependencyEngine dependencyEngine;

    protected DependencyPlugin() {
        this.dependencyEngine = DependencyEngine.createNew(this.getDataFolder().toPath().resolve("dependencies"));
    }

    @Override public final void onLoad() {
        super.onLoad();

        this.dependencyEngine.loadAllDependencies(this.getClass())
                             .exceptionally(e -> {
                                 this.getLogger().log(Level.SEVERE, "An error occurred while loading dependencies", e);
                                 return null;
                             })
                             .join();

        this.load();
    }

    @Override public final void onEnable() {
        super.onDisable();

        this.enable();
    }

    @Override public final void onDisable() {
        super.onDisable();

        this.disable();
    }

    protected abstract void load();

    protected abstract void enable();

    protected abstract void disable();

    /**
     * @return The current dependency engine instance for this dependency plugin.
     */
    public @NotNull DependencyEngine getDependencyEngine() {
        return this.dependencyEngine;
    }
}
