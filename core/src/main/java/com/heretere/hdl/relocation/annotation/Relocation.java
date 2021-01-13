/*
 * MIT License
 *
 * Copyright (c) 2021 Justin Heflin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.heretere.hdl.relocation.annotation;

import com.heretere.hdl.dependency.DependencyLoader;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Used to define relocations for any imported {@link com.heretere.hdl.dependency.maven.annotation.MavenDependency}
 * dependencies.
 * <p>
 * You can't use '.' or '/' for the package names due to maven/gradle relocation changing those at compile time.
 * The separator by default is '|' you can change the separator by changing the separator value in this annotation.
 * <p>
 * eg: example relocations
 *
 * ~Relocation(from = "com|google|guava", to = "com|yourpackage|libs|guava")
 * defining your own separator.
 * ~Relocation(from = "com{}google{}guava", to = "com{}yourpackage{}libs{}guava", separator = "{}")
 *
 * @see RelocationInfo
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

