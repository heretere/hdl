package com.heretere.hdl.plugin.tasks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.filter.ScopeDependencyFilter;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.tasks.TaskAction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.heretere.hdl.common.constants.DefaultRepository;
import com.heretere.hdl.common.json.HDLConfig;
import com.heretere.hdl.common.json.Repository;
import com.heretere.hdl.common.json.ResolvedDependency;

import lombok.val;

public class HDLGenerateDependencies extends DefaultTask {
    private final Configuration hdlConfiguration;
    private final Path tmpRepo;
    private final List<RemoteRepository> repositories;
    private final RepositorySystem repositorySystem;
    private final HDLConfig.HDLConfigBuilder hdlConfigBuilder;

    @Inject
    public HDLGenerateDependencies(Configuration hdlConfiguration) {
        this.hdlConfiguration = hdlConfiguration;
        this.tmpRepo = super.getProject().getBuildDir().toPath().resolve("hdl").resolve(".m2").resolve("repository");
        this.repositories = Lists.newArrayList();

        val locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

        this.repositorySystem = locator.getService(RepositorySystem.class);
        this.hdlConfigBuilder = HDLConfig.builder();
    }

    private RepositorySystemSession newSession() {
        val session = MavenRepositorySystemUtils.newSession();
        val localRepository = new LocalRepository(this.tmpRepo.toFile());
        session.setLocalRepositoryManager(this.repositorySystem.newLocalRepositoryManager(session, localRepository));
        return session;
    }

    private void initRepos() {
        this.repositories.clear();

        this.getProject()
            .getRepositories()
            .stream()
            .filter(MavenArtifactRepository.class::isInstance)
            .filter(repo -> !repo.getName().equals(DefaultRepository.MAVEN_LOCAL.getId()))
            .map(repo -> {
                val mavenRepo = (MavenArtifactRepository) repo;
                return new RemoteRepository.Builder(mavenRepo.getName(), "default", mavenRepo.getUrl().toString())
                    .build();
            })
            .forEach(this.repositories::add);
    }

    private void getAllDependencies(Artifact artifact) throws DependencyResolutionException {
        val collectRequest = new CollectRequest(new Dependency(artifact, ""), this.repositories);
        val dependencyRequest = new DependencyRequest(
                collectRequest,
                new ScopeDependencyFilter("provided", "system", "test", "import")
        );

        val result = this.repositorySystem.resolveDependencies(this.newSession(), dependencyRequest);
        result.getArtifactResults().forEach(ar -> {
            Artifact resolvedArtifact = ar.getArtifact();
            RemoteRepository remoteRepo = (RemoteRepository) ar.getRepository();

            val url = remoteRepo.getUrl().endsWith("/") ? remoteRepo.getUrl() : (remoteRepo.getUrl() + "/");

            val group = resolvedArtifact.getGroupId().replace(".", "/");
            val artifactId = resolvedArtifact.getArtifactId();
            val version = resolvedArtifact.getBaseVersion();
            val versionExtension = resolvedArtifact.getVersion();

            val jarName = resolvedArtifact.getArtifactId() + "-" + versionExtension + ".jar";
            val relativeUrl = String.format("%s/%s/%s/%s", group, artifactId, version, jarName);

            val defaultRepo = DefaultRepository.fromURLString(url);
            final String repoId;

            if (defaultRepo == null) {
                repoId = remoteRepo.getId();
                this.hdlConfigBuilder.repository(Repository.builder().id(repoId).url(url).build());
            } else {
                repoId = defaultRepo.getId();
                this.hdlConfigBuilder.repository(
                    Repository.builder().id(repoId).urls(defaultRepo.getMirrors()).build()
                );
            }

            this.hdlConfigBuilder.dependency(new ResolvedDependency(relativeUrl, repoId, jarName));
        });

    }

    @TaskAction
    public void generateDependencies() throws IOException, DependencyResolutionException {
        this.initRepos();

        val dependencies = this.hdlConfiguration.getAllDependencies()
            .stream()
            .filter(dependency -> dependency.getGroup() != null && dependency.getVersion() != null)
            .collect(Collectors.toSet());

        for (val dependency : dependencies) {
            assert !(dependency instanceof ProjectDependency) : "HDL doesn't support project dependencies.";
        }

        for (val dependency : dependencies) {
            this.getAllDependencies(
                new DefaultArtifact(dependency.getGroup() + ":" + dependency.getName() + ":" + dependency.getVersion())
            );
        }

        ObjectMapper mapper = new ObjectMapper();
        val resourcesDir = this.getProject().getBuildDir().toPath().resolve("resources/main");
        Files.createDirectories(resourcesDir);
        mapper.writeValue(
            resourcesDir.resolve("hdl_dependencies.json").toFile(),
            this.hdlConfigBuilder.build()
        );
    }
}
