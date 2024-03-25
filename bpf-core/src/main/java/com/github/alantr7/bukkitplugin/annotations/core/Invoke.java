package com.github.alantr7.bukkitplugin.annotations.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Invoke {

    Schedule value();

    enum Schedule {

        BEFORE_PLUGIN_ENABLE,
        AFTER_PLUGIN_ENABLE,

        BEFORE_PLUGIN_DISABLE,
        AFTER_PLUGIN_DISABLE,

        ASAP

    }

}
