package com.heretere.hdl.common.json;

import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Jacksonized
public class HDLConfig {
    @Singular
    private Map<String, Repository> repositories;
    @Singular
    private Set<ResolvedDependency> dependencies;
}
