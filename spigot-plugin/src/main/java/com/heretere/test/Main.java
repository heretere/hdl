package com.heretere.test;

import org.bukkit.plugin.java.JavaPlugin;

import com.heretere.hdl.impl.DependencyLoader;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        new DependencyLoader(this.getDataFolder().toPath(), this.getClassLoader()).loadDependencies();
    }
}
