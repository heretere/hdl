package com.heretere.hdl.common.json;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

@AllArgsConstructor
@Builder
@Data
@Jacksonized
public class HDLConfig {
    @Singular
    private Set<Repository> repositories;
    @Singular
    private Set<ResolvedDependency> dependencies;
}
