package com.heretere.hdl.common.json;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResolvedDependency {
    private String relativeUrl;
    private String repositoryId;
    private String fileName;
}
