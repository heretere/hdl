package com.heretere.hdl.dependency.maven.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to define a maven repo url for the dependencies.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(MavenRepository.List.class)
public @interface MavenRepository {
    /**
     * @return A string that represents the URL to a maven repo.
     */
    @NotNull String value() default "https://repo1.maven.org/maven2/";

    /**
     * Used to store multiple {@link MavenRepository} annotations on a single class type.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface List {
        /**
         * @return An array of {@link MavenRepository} annotations.
         */
        @NotNull MavenRepository @NotNull [] value() default {};
    }
}
