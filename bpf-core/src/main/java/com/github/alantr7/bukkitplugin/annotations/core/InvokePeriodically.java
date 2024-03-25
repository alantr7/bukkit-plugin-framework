package com.github.alantr7.bukkitplugin.annotations.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface InvokePeriodically {

    long delay() default 0;

    long interval();

    int limit() default 0;

    boolean sync() default true;

}
