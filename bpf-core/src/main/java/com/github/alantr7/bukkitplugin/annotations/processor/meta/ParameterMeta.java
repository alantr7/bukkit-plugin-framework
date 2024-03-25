package com.github.alantr7.bukkitplugin.annotations.processor.meta;

public class ParameterMeta implements Element {

    private final Type type;

    private final Type[] annotations;

    public ParameterMeta(Type type, Type[] annotations) {
        this.type = type;
        this.annotations = annotations;
    }

    public Type getType() {
        return type;
    }

    public Type[] getAnnotations() {
        return annotations;
    }

    @Override
    public Element getParent() {
        return null;
    }

    @Override
    public Kind getKind() {
        return Kind.PARAMETER;
    }

}
