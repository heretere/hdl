package com.heretere.hdl.common.constants;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.heretere.hdl.common.json.Repository;

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
    private final Repository repository;

    DefaultRepository(String id, String... mirrors) {
        this.id = id;
        this.repository = Repository.builder().urls(Arrays.stream(mirrors).collect(Collectors.toList())).build();
    }

    public static DefaultRepository fromId(@NonNull String id) {
        return Arrays.stream(DefaultRepository.values())
            .filter(repository -> repository.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    public static DefaultRepository fromURLString(@NonNull String url) {
        return Arrays.stream(DefaultRepository.values())
            .filter(repository -> repository.getRepository().getUrls().stream().anyMatch(url::equals))
            .findFirst()
            .orElse(null);
    }
}
