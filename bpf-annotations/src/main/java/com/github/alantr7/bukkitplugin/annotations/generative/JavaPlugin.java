package com.github.alantr7.bukkitplugin.annotations.generative;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface JavaPlugin {

    String name();

    String main() default "";

    String description() default "";

    String version() default "0.1.0";

    String apiVersion() default "1.21.5";

}
