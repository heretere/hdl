package com.heretere.hdl.plugin.tasks;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.tasks.Jar;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
public class HDLPackageRuntime extends DefaultTask {
    private final Configuration hdlRuntime;

    @TaskAction
    public void packageRuntime() {
        val jarTask = (Jar) super.getProject().getTasks().getByName("jar");

        jarTask.from(
            hdlRuntime
                .resolve()
                .stream()
                .map(file -> file.isDirectory() ? file : super.getProject().zipTree(file))
                .toArray()
        );

    }
}
