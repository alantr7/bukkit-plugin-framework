package com.github.alantr7.bukkitplugin.annotations.processor.meta;

public interface Element {

    Element getParent();

    Kind getKind();

    enum Kind {
        CLASS, FIELD, METHOD, PARAMETER
    }

}
