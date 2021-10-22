package com.heretere.hdl.common.json;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.Set;

@Data
@Builder
@Jacksonized
@AllArgsConstructor
public class Repository {
    private String id;
    @Singular
    private Set<String> urls;
}
