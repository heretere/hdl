package com.heretere.hdl.dependency;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class AbstractDependency implements Dependency {

    protected final AbstractDependency.Info dependencyInfo;

    protected AbstractDependency(
            @NotNull final String separator,
            @NotNull final String groupId,
            @NotNull final String artifactId,
            @NotNull final String version
    ) {
        dependencyInfo = this.parseDependency(separator,groupId,artifactId,version);
    }

    protected AbstractDependency(
            @NotNull final String separator,
            @NotNull final String singleLineDependency
    ) {
        dependencyInfo = this.parseSingleLineDependency(separator,singleLineDependency);
    }

    protected abstract AbstractDependency.Info parseSingleLineDependency(@NotNull final String separator,
                                                                         @NotNull final String singleLineDependency);


    protected abstract AbstractDependency.Info parseDependency(@NotNull final String separator,
                                                               @NotNull final String groupId,
                                                               @NotNull final String artifactId,
                                                               @NotNull final String version);

    @NotNull
    @Override
    public AbstractDependency.Info getDependencyInfo() {
        return this.dependencyInfo;
    }

    @Override
    public int hashCode() {
        return this.dependencyInfo.hashCode();
    }

    protected static class Info implements Dependency.Info {
        protected @NotNull final String groupId;
        protected @NotNull final String artifactId;
        protected @NotNull final String version;

        public Info(
                @NotNull final String groupId,
                @NotNull final String artifactId,
                @NotNull final String version
        ) {

            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }

        @Override
        public @NotNull String getVersion() {
            return this.version;
        }

        @Override
        public @NotNull String getArtifactId() {
            return this.artifactId;
        }

        @Override
        public @NotNull String getGroupId() {
            return this.groupId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Info that = (Info) o;
            return this.groupId.equals(that.groupId) &&
                    this.artifactId.equals(that.artifactId) &&
                    this.version.equals(that.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(groupId, artifactId, version);
        }
    }
}
