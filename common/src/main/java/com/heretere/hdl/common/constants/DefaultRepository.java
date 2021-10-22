package com.heretere.hdl.common.constants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import lombok.Getter;
import lombok.NonNull;

@Getter
public enum DefaultRepository {
    MAVEN_LOCAL("MavenLocal"),
    MAVEN_CENTRAL(
            "MavenRepo",
            "https://maven-central.storage.googleapis.com/maven2/",
            "https://repo1.maven.org/maven2/",
            "https://repo.maven.apache.org/maven2/"
    );

    private final String id;
    private final Set<String> mirrors;

    DefaultRepository(String id, String... mirrors) {
        this.id = id;
        this.mirrors = new LinkedHashSet<>();
        this.mirrors.addAll(Arrays.asList(mirrors));
    }

    public static DefaultRepository fromId(@NonNull String id) {
        return Arrays.stream(DefaultRepository.values())
            .filter(repository -> repository.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    public static DefaultRepository fromURLString(@NonNull String url) {
        return Arrays.stream(DefaultRepository.values())
            .filter(repository -> repository.getMirrors().stream().anyMatch(url::equals))
            .findFirst()
            .orElse(null);
    }
}
