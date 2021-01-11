package com.heretere.hdl.dependency.maven.annotation;

import com.heretere.hdl.dependency.DependencyLoader;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to declare a maven dependency, it can be defined in two different ways.
 * <p>
 * The first:
 *
 * @ ~Maven("com.google.guava:guava:30.1-jre")
 * <p>
 * The second:
 * @ ~Maven(groupId = "com.google.guava", artifactId = "guava", version = "30.1-jre")
 *
 * @see com.heretere.hdl.dependency.maven.MavenDependencyInfo
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(MavenDependency.List.class)
public @interface MavenDependency {
    /**
     * Used to declare variables in a single line gradle notation.
     * <p>
     * eg: 'com.google.guava:guava:30.1-jre'
     * If you would like to declare dependencies in a structured manner use the other variables.
     *
     * @return A gradle style single line dependency string.
     */
    @NotNull String value() default "";

    /**
     * Used to define a group id for a maven dependency.
     * <p>
     * eg: 'com.google.guava'.
     *
     * @return the group id.
     */
    @NotNull String groupId() default "";

    /**
     * Used to define an artifact id for a maven dependency.
     * <p>
     * eg: 'guava'.
     *
     * @return the artifact id.
     */
    @NotNull String artifactId() default "";

    /**
     * Used to define the version for a maven dependency.
     * <p>
     * eg: '30.1-jre'.
     *
     * @return the version.
     */
    @NotNull String version() default "";

    /**
     * @return The separator to use instead of '.' or '/'.
     */
    @NotNull String separator() default DependencyLoader.DEFAULT_SEPARATOR;

    /**
     * Used to store multiple {@link MavenDependency} annotations on a single class type.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface List {
        /**
         * @return An array of {@link MavenDependency} annotations.
         */
        @NotNull MavenDependency[] value() default {};
    }

}
