package com.heretere.hdl.plugin;

import java.util.Objects;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

import com.heretere.hdl.plugin.tasks.HDLGenerateDependencies;
import com.heretere.hdl.plugin.tasks.HDLPackageRuntime;

import lombok.NonNull;
import lombok.val;

public class HDLPlugin implements Plugin<Project> {
    private Configuration createHDLConfig(Project target) {
        target.getPluginManager().apply("java");

        val hdlConfig = target.getConfigurations().create("hdl");
        val compileOnlyConfig = target.getConfigurations().findByName("compileOnly");

        Objects.requireNonNull(compileOnlyConfig, "compileOnly configuration not found... is the java plugin applied?");
        compileOnlyConfig.extendsFrom(hdlConfig);

        return hdlConfig;
    }

    private Configuration addHDLDependency(Project target, HDLExtension extension) {
        val hdlDependencyConfig = target.getConfigurations().create("HDLRuntime");
        val implementationConfig = target.getConfigurations().findByName("implementation");

        Objects.requireNonNull(
            implementationConfig,
            "implementation configuration not found... is the java plugin applied?"
        );

        val dependency = target.getDependencies()
            .add(
                hdlDependencyConfig.getName(),
                extension.isBukkit() ? "com.heretere.hdl:bukkit:+" : "com.heretere.hdl:core:+"
            );

        implementationConfig.getDependencies().add(dependency);
        hdlDependencyConfig.getDependencies().add(dependency);

        return hdlDependencyConfig;
    }

    @Override
    public void apply(@NonNull Project target) {
        val extension = target.getExtensions().create("hdl", HDLExtension.class);
        val hdlConfig = this.createHDLConfig(target);

        target.afterEvaluate(t -> {
            val runtimeConfig = this.addHDLDependency(target, extension);
            val generateDependencies = target.getTasks()
                .create("hdlGenerateDependencies", HDLGenerateDependencies.class, hdlConfig);
            val packageRuntime = target.getTasks()
                .create("hdlPackageRuntime", HDLPackageRuntime.class, runtimeConfig);

            generateDependencies.setGroup("hdl");
            packageRuntime.setGroup("hdl");

            packageRuntime
                .dependsOn(generateDependencies);

            target.getTasks()
                .getByName("jar")
                .dependsOn(packageRuntime);

            val shadowJar = target.getTasks().findByName("shadowJar");

            if (shadowJar != null) {
                shadowJar
                    .dependsOn(packageRuntime);
            }
        });
    }

}
