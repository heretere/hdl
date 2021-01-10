package com.heretere.hdl.dependency.maven;

import com.google.common.collect.Sets;
import com.heretere.hdl.dependency.DependencyProvider;
import com.heretere.hdl.dependency.DependencyLoader;
import com.heretere.hdl.dependency.annotation.LoaderPriority;
import com.heretere.hdl.dependency.maven.annotation.Maven;
import com.heretere.hdl.dependency.maven.annotation.MavenRepo;
import com.heretere.hdl.exception.InvalidDependencyException;
import com.heretere.hdl.relocation.Relocator;
import com.heretere.hdl.relocation.annotation.Relocation;
import com.heretere.hdl.relocation.annotation.RelocationRule;
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

@LoaderPriority
public final class MavenDependencyLoader extends DependencyLoader<MavenDependency> {
    private @NotNull final Set<@NotNull String> repos;
    private @NotNull final Set<@NotNull RelocationRule> relocations;

    public MavenDependencyLoader(final @NotNull Path basePath) {
        super(basePath.resolve("maven"));
        this.repos = Sets.newHashSet("https://repo1.maven.org/maven2/");
        this.relocations = Sets.newHashSet();
    }

    private void processMavenAnnotation(final @NotNull Maven maven) {
        if (!maven.value().isEmpty()) {
            super.addDependency(new MavenDependency(maven.separator(), maven.value()));
        } else {
            super.addDependency(new MavenDependency(
                    maven.separator(),
                    maven.groupId(),
                    maven.artifactId(),
                    maven.version()
            ));
        }
    }

    @Override
    public void loadDependenciesFromClass(@NotNull final Class<?> clazz) {
        if (clazz.isAnnotationPresent(MavenRepo.class)) {
            this.repos.add(clazz.getAnnotation(MavenRepo.class).value());
        }

        if (clazz.isAnnotationPresent(MavenRepo.List.class)) {
            Arrays.stream(clazz.getAnnotation(MavenRepo.List.class).value())
                    .forEach(repo -> this.repos.add(repo.value()));
        }

        if (clazz.isAnnotationPresent(Relocation.class)) {
            this.relocations.add(new RelocationRule(clazz.getAnnotation(Relocation.class)));
        }

        if (clazz.isAnnotationPresent(Relocation.List.class)) {
            this.relocations.addAll(
                    Arrays.stream(clazz.getAnnotation(Relocation.List.class).value())
                    .map(RelocationRule::new)
                    .collect(Collectors.toList())
            );
        }

        if (clazz.isAnnotationPresent(Maven.class)) {
            this.processMavenAnnotation(clazz.getAnnotation(Maven.class));
        }

        if (clazz.isAnnotationPresent(Maven.List.class)) {
            Arrays.stream(clazz.getAnnotation(Maven.List.class).value()).forEach(this::processMavenAnnotation);
        }
    }

    @Override
    public void loadDependenciesFromHandler(@NotNull final DependencyProvider<MavenDependency> provider) {

    }

    @Override
    public void downloadDependencies() throws IOException {
        AtomicReference<Optional<IOException>> exception = new AtomicReference<>(Optional.empty());

        super.getDependencies()
                .parallelStream()
                .forEach(dependency -> {
                    if (exception.get().isPresent()) {
                        return;
                    }

                    Path downloadLocation = super.getBasePath().resolve(dependency.getDownloadedFileName());
                    if (!Files.exists(downloadLocation)
                            && !Files.exists(super.getBasePath().resolve(dependency.getRelocatedFileName()))) {

                        try {
                            Optional<SimpleImmutableEntry<String, URL>> downloadURL = Optional.empty();
                            for (String repo : this.repos) {
                                URL tempURL = dependency.getDownloadURL(repo);

                                HttpURLConnection connection = (HttpURLConnection) tempURL.openConnection();
                                connection.setInstanceFollowRedirects(false);
                                connection.setRequestProperty(
                                        "User-Agent",
                                        "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 " +
                                                "Firefox/3.5.2" +
                                                " (" +
                                                ".NET CLR 3.5.30729)"
                                );
                                connection.setRequestMethod("HEAD");

                                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK
                                        || connection.getResponseCode() == HttpURLConnection.HTTP_ACCEPTED) {
                                    downloadURL = Optional.of(new SimpleImmutableEntry<>(repo, tempURL));
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

        Optional<IOException> optionalException = exception.get();
        if (optionalException.isPresent()) {
            throw optionalException.get();
        }
    }

    @Override
    public void relocateDependencies() throws IllegalAccessException, InstantiationException,
            InvocationTargetException, IOException, NoSuchMethodException, ClassNotFoundException {
        Relocator relocator = new Relocator(super.getBasePath());

        for (MavenDependency dependency : super.getDependencies()) {
            Path relocatedLocation = super.getBasePath().resolve(dependency.getRelocatedFileName());

            if (!Files.exists(relocatedLocation)) {
                relocator.relocate(this.relocations, dependency);

                Files.delete(super.getBasePath().resolve(dependency.getDownloadedFileName()));
            }
        }
    }

    @Override
    public void loadDependencies(final @NotNull URLClassLoader classLoader) throws
            NoSuchMethodException,
            MalformedURLException, InvocationTargetException, IllegalAccessException {

        Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            method.setAccessible(true);
            return null;
        });

        for (MavenDependency dependency : super.getDependencies()) {
            Path downloadLocation = super.getBasePath().resolve(dependency.getDownloadedFileName());
            Path relocatedLocation = super.getBasePath().resolve(dependency.getRelocatedFileName());

            if (Files.exists(relocatedLocation)) {
                method.invoke(classLoader, relocatedLocation.toUri().toURL());
            } else {
                method.invoke(classLoader, downloadLocation.toUri().toURL());
            }
        }
    }
}
