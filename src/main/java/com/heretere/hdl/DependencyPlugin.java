package com.heretere.hdl;

import org.bukkit.plugin.java.JavaPlugin;

public abstract class DependencyPlugin extends JavaPlugin {

    protected DependencyPlugin() {

    }

    @Override public final void onLoad() {
        super.onLoad();
        DependencyEngine dependencyEngine = new DependencyEngine(this.getDataFolder().toPath().resolve("dependencies"));

        dependencyEngine.loadAllDependencies(this.getClass());

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

    public abstract void load();

    public abstract void enable();

    public abstract void disable();
}
