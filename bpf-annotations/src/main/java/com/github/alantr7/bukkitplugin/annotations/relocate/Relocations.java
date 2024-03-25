package com.github.alantr7.bukkitplugin.annotations.relocate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface Relocations {

    Relocate[] value();

}
