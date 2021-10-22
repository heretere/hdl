package com.heretere.hdl.plugin;

import java.util.Objects;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

import com.heretere.hdl.plugin.tasks.HDLGenerateDependencies;

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

    @Override
    public void apply(@NonNull Project target) {
        val hdlConfig = this.createHDLConfig(target);
        val generateDependencies = target.getTasks()
            .create("hdlGenerateDependencies", HDLGenerateDependencies.class, hdlConfig);
        generateDependencies.setGroup("hdl");

        target.getTasks()
            .getByName("processResources")
            .dependsOn(generateDependencies);
    }

}
