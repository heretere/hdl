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

package com.heretere.hdl.dependency.maven;

import com.google.common.collect.Sets;
import com.heretere.hdl.dependency.DependencyLoader;
import com.heretere.hdl.dependency.annotation.LoaderPriority;
import com.heretere.hdl.dependency.maven.annotation.MavenDependency;
import com.heretere.hdl.dependency.maven.annotation.MavenRepository;
import com.heretere.hdl.exception.InvalidDependencyException;
import com.heretere.hdl.relocation.Relocator;
import com.heretere.hdl.relocation.annotation.Relocation;
import com.heretere.hdl.relocation.annotation.RelocationInfo;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * The dependency loader responsible for loading maven dependencies.
 */
@LoaderPriority
public final class MavenDependencyLoader extends DependencyLoader<@NotNull MavenDependencyInfo> {
    /**
     * This is used as the user agent for requesting the direct download jar link.
     */
    private static final SimpleImmutableEntry<String, String> REQUEST_USER_AGENT =
        new SimpleImmutableEntry<>(
            "User-Agent",
            "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 " +
                "Firefox/3.5.2" +
                " (" +
                ".NET CLR 3.5.30729)"
        );

    /**
     * The repos attached to these dependencies.
     */
    private final @NotNull Set<@NotNull MavenRepositoryInfo> repos;
    /**
     * Any relocations that should be ran on the incoming dependencies.
     */
    private final @NotNull Set<@NotNull RelocationInfo> relocations;

    /**
     * Creates a new maven dependency loader with the specified base path.
     *
     * @param basePath The base path used to resolve relative file names.
     */
    public MavenDependencyLoader(final @NotNull Path basePath) {
        this(basePath, "maven");
    }

    /**
     * Creates a new maven dependency loader with the specified base path and destination directory name.
     *
     * @param basePath           The base path used to resolve relative file names.
     * @param storageDestination relative location for maven dependencies to be stored.
     */
    public MavenDependencyLoader(
        final @NotNull Path basePath,
        final @NotNull String storageDestination
    ) {
        super(basePath, storageDestination);
        this.repos = Sets.newHashSet(MavenRepositoryInfo.of("https://repo1.maven.org/maven2/"));
        this.relocations = Sets.newHashSet();
    }

    private void loadDependenciesFrom(final @NotNull MavenDependencyProvider dependencyProvider) {
        this.repos.addAll(dependencyProvider.getRepositories());
        this.relocations.addAll(dependencyProvider.getRelocations());
        dependencyProvider.getDependencies().forEach(super::addDependency);
    }

    private void loadDependenciesFrom(final @NotNull Class<@NotNull ?> clazz) {
        if (clazz.isAnnotationPresent(MavenRepository.class)) {
            this.repos.add(MavenRepositoryInfo.of(clazz.getAnnotation(MavenRepository.class)));
        }

        if (clazz.isAnnotationPresent(MavenRepository.List.class)) {
            Arrays.stream(clazz.getAnnotation(MavenRepository.List.class).value())
                  .map(MavenRepositoryInfo::of)
                  .forEach(this.repos::add);
        }

        if (clazz.isAnnotationPresent(Relocation.class)) {
            this.relocations.add(RelocationInfo.of(clazz.getAnnotation(Relocation.class)));
        }

        if (clazz.isAnnotationPresent(Relocation.List.class)) {
            this.relocations.addAll(
                Arrays.stream(clazz.getAnnotation(Relocation.List.class).value())
                      .map(RelocationInfo::of)
                      .collect(Collectors.toList())
            );
        }

        if (clazz.isAnnotationPresent(MavenDependency.class)) {
            super.addDependency(MavenDependencyInfo.of(clazz.getAnnotation(MavenDependency.class)));
        }

        if (clazz.isAnnotationPresent(MavenDependency.List.class)) {
            Arrays.stream(clazz.getAnnotation(MavenDependency.List.class).value())
                  .map(MavenDependencyInfo::of)
                  .forEach(super::addDependency);
        }
    }

    @Override
    public void loadDependenciesFrom(final @NotNull Object object) {
        if (object instanceof Class) {
            this.loadDependenciesFrom((Class<?>) object);
        } else if (object instanceof MavenDependencyProvider) {
            this.loadDependenciesFrom((MavenDependencyProvider) object);
        }
    }

    @Override
    public void downloadDependencies() throws IOException {
        final AtomicReference<Optional<IOException>> exception = new AtomicReference<>(Optional.empty());

        super.getDependencies()
             .parallelStream()
             .forEach(dependency -> {
                 if (exception.get().isPresent()) {
                     return;
                 }


                 final Path downloadLocation = super.getBasePath().resolve(dependency.getDownloadedFileName());

                 if (!Files.exists(downloadLocation)
                     && !Files.exists(super.getBasePath().resolve(dependency.getRelocatedFileName()))) {

                     try {
                         Optional<SimpleImmutableEntry<String, URL>> downloadURL = Optional.empty();
                         for (MavenRepositoryInfo repo : this.repos) {
                             final URL tempURL = dependency.getDownloadURL(repo.getURL());

                             final HttpURLConnection connection = (HttpURLConnection) tempURL.openConnection();
                             connection.setInstanceFollowRedirects(false);
                             connection.setRequestProperty(
                                 MavenDependencyLoader.REQUEST_USER_AGENT.getKey(),
                                 MavenDependencyLoader.REQUEST_USER_AGENT.getValue()
                             );
                             connection.setRequestMethod("HEAD");

                             if (connection.getResponseCode() == HttpURLConnection.HTTP_OK
                                 || connection.getResponseCode() == HttpURLConnection.HTTP_ACCEPTED) {
                                 downloadURL = Optional.of(new SimpleImmutableEntry<>(repo.getURL(), tempURL));
                                 break;
                             }
                         }

                         if (!downloadURL.isPresent()) {
                             throw new InvalidDependencyException(String.format(
                                 "Couldn't download dependency: '%s'.",
                                 dependency.getName()
                             ));
                         }

                         try (InputStream is = downloadURL.get().getValue().openStream()) {
                             Files.createDirectories(downloadLocation.getParent());
                             Files.deleteIfExists(downloadLocation);
                             Files.copy(is, downloadLocation);
                         }
                     } catch (IOException e) {
                         exception.set(Optional.of(e));
                     }
                 }
             });

        final Optional<IOException> optionalException = exception.get();
        if (optionalException.isPresent()) {
            throw optionalException.get();
        }
    }

    @Override
    public void relocateDependencies() throws IllegalAccessException, InstantiationException,
        InvocationTargetException, IOException, NoSuchMethodException, ClassNotFoundException {
        final Relocator relocator = new Relocator(super.getBasePath());

        for (MavenDependencyInfo dependency : super.getDependencies()) {
            final Path relocatedLocation = super.getBasePath().resolve(dependency.getRelocatedFileName());

            if (!Files.exists(relocatedLocation)) {
                relocator.relocate(this.relocations, dependency);

                Files.delete(super.getBasePath().resolve(dependency.getDownloadedFileName()));
            }
        }
    }

    @Override
    public void loadDependencies(final @NotNull URLClassLoader classLoader) throws NoSuchMethodException,
        MalformedURLException, InvocationTargetException, IllegalAccessException {

        final Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            method.setAccessible(true);
            return null;
        });

        for (MavenDependencyInfo dependency : super.getDependencies()) {
            final Path downloadLocation = super.getBasePath().resolve(dependency.getDownloadedFileName());
            final Path relocatedLocation = super.getBasePath().resolve(dependency.getRelocatedFileName());

            if (Files.exists(relocatedLocation)) {
                method.invoke(classLoader, relocatedLocation.toUri().toURL());
            } else {
                method.invoke(classLoader, downloadLocation.toUri().toURL());
            }
        }
    }
}
