package com.github.alantr7.bukkitplugin.annotations.processor.meta;

import com.github.alantr7.bukkitplugin.annotations.processor.reader.MetaLoader;
import lombok.Getter;

public class FieldMeta implements Element {

    private final MetaLoader loader;

    private final String enclosingClass;

    @Getter
    private final String name;

    @Getter
    private final Type type;

    @Getter
    private final Type[] annotations;

    public FieldMeta(MetaLoader loader, String enclosingClass, String name, Type type, Type[] annotations) {
        this.loader = loader;
        this.enclosingClass = enclosingClass;
        this.name = name;
        this.type = type;
        this.annotations = annotations;
    }

    @Override
    public Element getParent() {
        return loader.getClass(enclosingClass);
    }

    @Override
    public Element.Kind getKind() {
        return Kind.FIELD;
    }

}
