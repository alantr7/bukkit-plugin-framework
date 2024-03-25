package com.github.alantr7.bukkitplugin.annotations.generative;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface Command {

    String name();

    String description();

    String permission() default "";

}
