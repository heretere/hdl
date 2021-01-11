package com.heretere.hdl.dependency.maven;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.heretere.hdl.dependency.DependencyLoader;
import com.heretere.hdl.dependency.RelocatableDependency;
import com.heretere.hdl.dependency.maven.annotation.MavenDependency;
import com.heretere.hdl.exception.InvalidDependencyException;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This is the backend class for {@link com.heretere.hdl.dependency.maven.annotation.MavenDependency} dependencies.
 * It's responsible for providing basic information needed to download and relocate a dependency jar.
 */
public final class MavenDependencyInfo implements RelocatableDependency {
    /**
     * This is used to validate a single line dependency.
     * A single line dependency should have a size of 3 always.
     */
    private static final int DEPENDENCY_SPLIT_SIZE = 3;
    /**
     * This is used to make sure a dependency string is valid before we try to do extra processing on it.
     */
    private static final @NotNull Pattern DEPENDENCY_PATTERN = Pattern.compile("^([a-zA-Z0-9.\\-_])+$");

    /**
     * The group id of this maven dependency.
     */
    private @Nullable String groupId;
    /**
     * The artifact id of this maven dependency.
     */
    private @Nullable String artifactId;
    /**
     * The version of this maven dependency.
     */
    private @Nullable String version;

    private MavenDependencyInfo(
            @NotNull final String separator,
            @NotNull final String groupId,
            @NotNull final String artifactId,
            @NotNull final String version
    ) {
        this.validateAndSetValues(separator, groupId, artifactId, version);
    }

    private MavenDependencyInfo(
            @NotNull final String separator,
            @NotNull final String singleLineDependency
    ) {
        if (singleLineDependency.isEmpty()) {
            throw new InvalidDependencyException(String.format(
                "Invalid single line dependency passed. dependency = '%s'.",
                singleLineDependency
            ));
        }

        List<String> values = Lists.newArrayListWithCapacity(MavenDependencyInfo.DEPENDENCY_SPLIT_SIZE);
        Splitter.on(':').split(singleLineDependency).forEach(values::add);

        if (values.size() != MavenDependencyInfo.DEPENDENCY_SPLIT_SIZE
            || values.get(0) == null
            || values.get(1) == null
            || values.get(2) == null) {
            throw new InvalidDependencyException(String.format(
                "Couldn't process single line dependency please make sure it's properly formatted. dependency = '%s'.",
                singleLineDependency
            ));
        }

        this.validateAndSetValues(separator, values.get(0), values.get(1), values.get(2));
    }

    private static boolean hasInvalidCharacters(@NotNull final String validate) {
        return StringUtils.containsAny(validate, new char[]{'.', '/'});
    }

    private static boolean patternMismatchString(@NotNull final String validate) {
        return !DEPENDENCY_PATTERN.matcher(validate).matches();
    }


    private void validateAndSetValues(
            @NotNull final String separator,
            @NotNull final String groupId,
            @NotNull final String artifactId,
            @NotNull final String version
    ) {
        if (separator.isEmpty() || MavenDependencyInfo.hasInvalidCharacters(separator)) {
            throw new InvalidDependencyException(String.format(
                "Separator can't be empty or contain '.' or '/'. Please use a different separator. separator = '%s'.",
                separator
            ));
        }

        if (groupId.isEmpty() || MavenDependencyInfo.hasInvalidCharacters(groupId)) {
            throw new InvalidDependencyException(String.format(
                "Group id can't be empty or contain '.' or '/'. " +
                    "Please use the separator string instead. groupId = '%s', separator = '%s'.",
                groupId,
                separator
            ));
        }

        if (artifactId.isEmpty() || MavenDependencyInfo.hasInvalidCharacters(artifactId)) {
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

        if (MavenDependencyInfo.patternMismatchString(validateGroupId)) {
            throw new InvalidDependencyException(String.format(
                "Group id contains invalid characters. groupId = '%s'.",
                validateGroupId
            ));
        }

        if (MavenDependencyInfo.patternMismatchString(validateArtifactId)) {
            throw new InvalidDependencyException(String.format(
                "Artifact id contains invalid characters. artifactId = '%s'.",
                validateArtifactId
            ));
        }

        if (MavenDependencyInfo.patternMismatchString(version)) {
            throw new InvalidDependencyException(String.format(
                "Version contains invalid characters. version = '%s'.",
                version
            ));
        }

        this.groupId = validateGroupId;
        this.artifactId = validateArtifactId;
        this.version = version;
    }

    @Override
    public @NotNull URL getManualDownloadURL(@NotNull final String baseURL) throws MalformedURLException {
        return this.getDownloadURL(baseURL);
    }

    @Override
    public @NotNull URL getDownloadURL(@NotNull final String baseURL) throws MalformedURLException {
        return new URL(String.format(
            "%s%s/%s/%s/%s-%s.jar",
            baseURL + (StringUtils.endsWith(baseURL, "/") ? "" : "/"),
            StringUtils.replace(this.groupId, ".", "/"),
            this.artifactId,
            this.version,
            this.artifactId,
            this.version
        ));
    }

    @Override
    public @NotNull String getDownloadedFileName() {
        return this.getName() + ".jar";
    }

    @Override
    public @NotNull String getName() {
        return this.artifactId + "-" + this.version;
    }

    @Override
    public @NotNull String getRelocatedFileName() {
        return this.getName() + "-relocated.jar";
    }

    @Nullable
    public String getVersion() {
        return version;
    }

    @Nullable
    public String getArtifactId() {
        return artifactId;
    }

    @Nullable
    public String getGroupId() {
        return groupId;
    }

    @Contract("_,_,_,_ -> new")
    public static @NotNull MavenDependencyInfo of(
            @NotNull final String separator,
            @NotNull final String groupId,
            @NotNull final String artifactId,
            @NotNull final String version
    ) {
        return new MavenDependencyInfo(separator,groupId,artifactId,version);
    }

    @Contract("_,_ -> new")
    public static @NotNull MavenDependencyInfo of(
            @NotNull final String separator,
            @NotNull final String singleLineDependency
    ) {
        return new MavenDependencyInfo(separator,singleLineDependency);
    }

    @Contract("_ -> new")
    public static @NotNull MavenDependencyInfo of(
            @NotNull final MavenDependency dependency
            ) {
        return dependency.value().isEmpty() ?
                MavenDependencyInfo.of(
                        dependency.separator(),
                        dependency.groupId(),
                        dependency.artifactId(),
                        dependency.version()
                ):
                MavenDependencyInfo.of(
                        dependency.separator(),
                        dependency.value()
                );
    }
}
