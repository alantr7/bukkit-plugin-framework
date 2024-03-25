package com.github.alantr7.bukkitplugin.annotations.generative;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotated fields must be declared final
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface Permission {

    String description() default "";

    Allowed allowed();

    enum Allowed {
        OP, TRUE, FALSE
    }

}
