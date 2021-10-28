package com.heretere.hdl.impl.bukkit;

import java.nio.file.Path;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.heretere.hdl.common.constants.DefaultRepository;
import com.heretere.hdl.impl.DependencyLoader;
import com.heretere.hdl.impl.exception.DependencyLoadException;

import lombok.val;

public class DependencyPlugin extends JavaPlugin {
    private final DependencyLoader dependencyLoader;
    private final Path dependencyFolder;

    protected DependencyPlugin() {
        this.dependencyFolder = super.getDataFolder().toPath().resolve("dependencies");
        this.dependencyLoader = new DependencyLoader(
                this.dependencyFolder,
                super.getClassLoader()
        );
    }

    @Override
    public final void onLoad() {
        super.getLogger().info("Loading Dependencies...");
        if (this.dependencyLoader.loadDependencies()) {
            super.getLogger().info("Loaded " + this.dependencyLoader.getDependencyCount() + " Dependencies...");
            this.load();
        } else {
            this.dependencyLoader.getErrors().forEach(error -> {
                if (error instanceof DependencyLoadException) {
                    val exception = (DependencyLoadException) error;

                    if (exception.getDependency() != null) {
                        val repoUrl = exception.getRepository() != null
                            ? exception.getRepository().getUrls().get(0)
                            : DefaultRepository.MAVEN_CENTRAL.getRepository().getUrls().get(0);

                        super.getLogger().log(
                            Level.SEVERE,
                            String.format(
                                "Failed to load dependency `%s`."
                                    + " Please download dependency from '%s' and place it in '%s'.",
                                exception.getDependency().getFileName(),
                                repoUrl + exception.getDependency().getRelativeUrl(),
                                super.getDataFolder().toPath().getParent().relativize(this.dependencyFolder)
                            ),
                            exception
                        );
                    } else {
                        super.getLogger().log(
                            Level.SEVERE,
                            "Failed to load dependency.",
                            exception
                        );
                    }
                } else {
                    super.getLogger().log(
                        Level.SEVERE,
                        "Error occurred while loading dependencies.",
                        error
                    );
                }
            });
        }
    }

    @Override
    public final void onEnable() {
        if (this.dependencyLoader.getErrors().isEmpty()) {
            this.enable();
        } else {
            super.getLogger().severe("Failed to load dependencies. Disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public final void onDisable() {
        if (this.dependencyLoader.getErrors().isEmpty()) {
            this.disable();
        }
    }

    protected void load() {
        // onLoad proxy
    }

    protected void enable() {
        // onEnable proxy
    }

    protected void disable() {
        // onDisable proxy
    }
}
