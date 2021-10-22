package com.heretere.hdl.impl.exception;

import com.heretere.hdl.common.json.Repository;
import com.heretere.hdl.common.json.ResolvedDependency;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class DependencyLoadException extends RuntimeException {
    private final transient ResolvedDependency dependency;
    private final transient Repository repository;
    private final String message;
}
