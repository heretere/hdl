package com.heretere.hdl.dependency.maven;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.heretere.hdl.dependency.AbstractDependency;
import com.heretere.hdl.dependency.Dependency;
import com.heretere.hdl.dependency.RelocatableDependency;
import com.heretere.hdl.dependency.maven.annotation.Maven;
import com.heretere.hdl.exception.InvalidDependencyException;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This is the backend class for {@link Maven} dependencies.
 * It's responsible for providing basic information needed to download and relocate a dependency jar.
 */
public final class MavenDependency extends AbstractDependency implements RelocatableDependency {
    /**
     * This is used to validate a single line dependency.
     * A single line dependency should have a size of 3 always.
     */
    private static final int DEPENDENCY_SPLIT_SIZE = 3;
    /**
     * This is used to make sure a dependency string is valid before we try to do extra processing on it.
     */
    private static final @NotNull Pattern DEPENDENCY_PATTERN = Pattern.compile("^([a-zA-Z0-9.\\-_])+$");

    public MavenDependency(
            final @NotNull String separator,
            final @NotNull String groupId,
            final @NotNull String artifactId,
            final @NotNull String version
    ) {
        super(separator,groupId,artifactId,version);
        MavenDependency.parse(separator, groupId, artifactId, version);
    }

    public MavenDependency(
            final @NotNull String separator,
            final @NotNull String singleLineDependency
    ) {
        super(separator, singleLineDependency);
    }

    private static Info parse(
            final @NotNull String separator,
            final @NotNull String groupId,
            final @NotNull String artifactId,
            final @NotNull String version
    ) {
        if (separator.isEmpty() || MavenDependency.hasInvalidCharacters(separator)) {
            throw new InvalidDependencyException(String.format(
                    "Separator can't be empty or contain '.' or '/'. Please use a different separator. separator = '%s'.",
                    separator
            ));
        }

        if (groupId.isEmpty() || MavenDependency.hasInvalidCharacters(groupId)) {
            throw new InvalidDependencyException(String.format(
                    "Group id can't be empty or contain '.' or '/'. " +
                            "Please use the separator string instead. groupId = '%s', separator = '%s'.",
                    groupId,
                    separator
            ));
        }

        if (artifactId.isEmpty() || MavenDependency.hasInvalidCharacters(artifactId)) {
            throw new InvalidDependencyException(String.format(
                    "Artifact id can't be empty or contain '.' or '/'. " +
                            "Please use the separator string instead. artifactId = '%s', separator = '%s'.",
                    artifactId,
                    separator
            ));
        }

        if (version.isEmpty()) {
            throw new InvalidDependencyException(String.format(
                    "Version can't be empty. Please fix the version string. version = '%s'.",
                    version
            ));
        }

        String validateGroupId = StringUtils.replace(groupId, separator, ".");
        String validateArtifactId = StringUtils.replace(artifactId, separator, ".");

        if (MavenDependency.patternMismatchString(validateGroupId)) {
            throw new InvalidDependencyException(String.format(
                    "Group id contains invalid characters. groupId = '%s'.",
                    validateGroupId
            ));
        }

        if (MavenDependency.patternMismatchString(validateArtifactId)) {
            throw new InvalidDependencyException(String.format(
                    "Artifact id contains invalid characters. artifactId = '%s'.",
                    validateArtifactId
            ));
        }

        if (MavenDependency.patternMismatchString(version)) {
            throw new InvalidDependencyException(String.format(
                    "Version contains invalid characters. version = '%s'.",
                    version
            ));
        }
        return new AbstractDependency.Info(groupId, artifactId, version);
    }

    private static boolean hasInvalidCharacters(final @NotNull String validate) {
        return StringUtils.containsAny(validate, new char[]{'.', '/'});
    }

    private static boolean patternMismatchString(final @NotNull String validate) {
        return !DEPENDENCY_PATTERN.matcher(validate).matches();
    }

    @Override
    protected Info parseSingleLineDependency(@NotNull String separator, @NotNull String singleLineDependency) {
        if (singleLineDependency.isEmpty()) {
            throw new InvalidDependencyException(String.format(
                    "Invalid single line dependency passed. dependency = '%s'.",
                    singleLineDependency
            ));
        }

        List<String> values = Lists.newArrayListWithCapacity(MavenDependency.DEPENDENCY_SPLIT_SIZE);
        Splitter.on(':').split(singleLineDependency).forEach(values::add);

        if (values.size() != MavenDependency.DEPENDENCY_SPLIT_SIZE
                || values.get(0) == null
                || values.get(1) == null
                || values.get(2) == null) {
            throw new InvalidDependencyException(String.format(
                    "Couldn't process single line dependency please make sure it's properly formatted. dependency = '%s'.",
                    singleLineDependency
            ));
        }

        String groupId = values.get(0);
        String artifactId = values.get(1);
        String version = values.get(2);

        MavenDependency.parse(separator, groupId, artifactId, version);

        return new Info(groupId, artifactId, version);
    }

    @Override
    protected Info parseDependency(@NotNull String separator,
                                   @NotNull String groupId,
                                   @NotNull String artifactId,
                                   @NotNull String version) {
        return MavenDependency.parse(separator, groupId, artifactId, version);
    }

    @Override
    public @NotNull URL getManualDownloadURL(final @NotNull String baseURL) throws MalformedURLException {
        return this.getDownloadURL(baseURL);
    }

    @Override
    public @NotNull URL getDownloadURL(final @NotNull String baseURL) throws MalformedURLException {
        final String version = super.dependencyInfo.getVersion();
        final String artifactId = super.dependencyInfo.getArtifactId();
        return new URL(String.format(
                "%s%s/%s/%s/%s-%s.jar",
                baseURL + (StringUtils.endsWith(baseURL, "/") ? "" : "/"),
                StringUtils.replace(super.dependencyInfo.getGroupId(), ".", "/"),
                artifactId,
                version,
                artifactId,
                version
        ));
    }

    @Override
    public @NotNull String getDownloadedFileName() {
        return this.getName() + ".jar";
    }

    @Override
    public @NotNull String getName() {
        return super.dependencyInfo.getArtifactId() + "-" + super.dependencyInfo.getVersion();
    }

    @Override
    public @NotNull String getRelocatedFileName() {
        return this.getName() + "-relocated.jar";
    }

    public static class Builder {

        private String separator;
        private String version;
        private String artifactId;
        private String groupId;

        private Builder() {

        }

        public Builder separator(@NotNull String separator) {
            this.separator = separator;
            return this;
        }

        public @NotNull Builder version(@NotNull String version) {
            this.version = version;
            return this;
        }

        public @NotNull Builder artifactId(@NotNull String artifactId) {
            this.artifactId = artifactId;
            return this;
        }

        public @NotNull Builder groupId(@NotNull String groupId) {
            this.groupId = groupId;
            return this;
        }

        public @Nullable MavenDependency build() {
            return new MavenDependency(separator,groupId,artifactId,version);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

}
