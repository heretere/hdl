package com.heretere.hdl.dependency.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is used to define the order dependency loader's should be executed in.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoaderPriority {
    /**
     * Lower values go first.
     * Default value 1000.
     *
     * @return The loader priority.
     */
    int value() default 1000;
}
