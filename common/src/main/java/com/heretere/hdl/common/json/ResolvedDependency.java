package com.heretere.hdl.common.json;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Jacksonized
public class ResolvedDependency {
    private String relativeUrl;
    private String repositoryId;
    private String fileName;
}
