package com.github.alantr7.bukkitplugin.annotations.processor.meta;

import com.github.alantr7.bukkitplugin.annotations.processor.UserAnnotationManager;
import com.github.alantr7.bukkitplugin.annotations.processor.reader.MetaLoader;
import lombok.Getter;

public class MethodMeta implements Element {

    private final MetaLoader loader;

    private final String enclosingClass;

    @Getter
    private final String name;

    @Getter
    private final Type returnType;

    @Getter
    private final Type[] annotations;

    @Getter
    private final ParameterMeta[] parameters;

    public MethodMeta(MetaLoader loader, String enclosingClass, String name, Type returnType, Type[] annotations, ParameterMeta[] parameters) {
        this.loader = loader;
        this.enclosingClass = enclosingClass;
        this.name = name;
        this.returnType = returnType;
        this.annotations = annotations;
        this.parameters = parameters;
    }

    @Override
    public Element getParent() {
        return loader.getClass(enclosingClass);
    }

    @Override
    public Kind getKind() {
        return Kind.METHOD;
    }

}
