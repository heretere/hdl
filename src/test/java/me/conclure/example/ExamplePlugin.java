/*
 * MIT License
 *
 * Copyright (c) 2021 Justin Heflin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package me.conclure.example;

import com.heretere.hdl.DependencyEngine;
import com.heretere.hdl.DependencyPlugin;
import com.heretere.hdl.dependency.DependencyProvider;
import com.heretere.hdl.dependency.maven.MavenDependencyProvider;
import com.heretere.hdl.dependency.maven.annotation.MavenDependency;
import com.heretere.hdl.dependency.maven.annotation.MavenRepository;
import com.heretere.hdl.relocation.annotation.Relocation;

import java.util.concurrent.CompletableFuture;

@MavenRepository("https://jitpack.io")
@MavenDependency("com|github|heretere:hch:v1.0.10")
@MavenDependency(groupId = "org|tomlj", artifactId = "tomlj", version = "1.0.0")
@MavenDependency(value = "org{}antlr:antlr4-runtime:4.7.2", separator = "{}")
@Relocation(from = "com|heretere|hch", to = "com|myplugin|libs|hch")
@Relocation(from = "org|tomlj", to = "com|myplugin|libs|tomlj")
@Relocation(from = "org{}antlr", to = "com{}myplugin{}libs{}antlr", separator = "{}")
public class ExamplePlugin extends DependencyPlugin {
    @Override
    protected void load() {
        DependencyProvider<?> dependencyProvider =
            MavenDependencyProvider.builder()
                                   .repository("https://repo.aikar.co/content/groups/aikar/")
                                   .repository("https://hub.spigotmc.org/nexus/content/groups/public/")
                                   .dependency("co|aikar:taskchain-bukkit:3.7.2")
                                   .dependency("#", "me#mattstudios#utils:matt-framework-gui:2.0.2")
                                   .relocation(
                                       "co|aikar|taskchain",
                                       "me|conclure|example|taskchain"
                                   )
                                   .build();

        DependencyEngine dependencyEngine = this.getDependencyEngine();
        CompletableFuture.allOf(
            dependencyEngine.loadAllDependencies(DependencyHolder.class),
            dependencyEngine.loadAllDependencies(dependencyProvider)
        ).join();
    }

    @Override
    protected void enable() {

    }

    @Override
    protected void disable() {

    }
}
