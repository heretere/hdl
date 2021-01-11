package com.heretere.hdl.relocation.annotation;

import com.heretere.hdl.dependency.DependencyLoader;
import com.heretere.hdl.dependency.maven.annotation.MavenDependency;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Used to define relocations for imported {@link MavenDependency} dependencies.
 * <p>
 * You can't use '.' or '/' for the package names due to maven/gradle relocation changing those at compile time.
 * The separator by default is '|' you can change the separator by changing the separator value in this annotation.
 * <p>
 * eg: example relocations
 *
 * @ ~Relocation(from = "com|google|guava", to = "com|yourpackage|libs|guava")
 * defining your own separator.
 * @ ~Relocation(from = "com{}google{}guava", to = "com{}yourpackage{}libs{}guava", separator = "{}")
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(Relocation.List.class)
public @interface Relocation {
    /**
     * @return The original package location.
     */
    @NotNull String from();

    /**
     * @return Where to move the package to.
     */
    @NotNull String to();

    /**
     * @return The separator to use instead of '.' or '/'.
     */
    @NotNull String separator() default DependencyLoader.DEFAULT_SEPARATOR;

    /**
     * Used to store multiple {@link Relocation} annotations on a single class type.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface List {
        /**
         * @return An array of {@link Relocation} annotations.
         */
        @NotNull Relocation[] value() default {};
    }
}

