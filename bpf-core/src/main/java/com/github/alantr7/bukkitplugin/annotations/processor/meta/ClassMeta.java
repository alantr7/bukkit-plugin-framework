package com.github.alantr7.bukkitplugin.annotations.processor.meta;

import com.github.alantr7.bukkitplugin.annotations.processor.reader.MetaLoader;

public class ClassMeta implements Element {

    private final MetaLoader loader;

    private final String name;

    private final Type superClass;

    private final Type[] interfaces;

    private final Type[] annotations;

    private final FieldMeta[] fields;

    private final MethodMeta[] methods;

    public ClassMeta(MetaLoader loader, String name, Type superClass, Type[] interfaces, Type[] annotations, FieldMeta[] fields, MethodMeta[] methods) {
        this.loader = loader;
        this.name = name;
        this.superClass = superClass;
        this.interfaces = interfaces;
        this.annotations = annotations;
        this.fields = fields;
        this.methods = methods;
    }

    public String getSimpleName() {
        return name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : name;
    }

    public String getPackageName() {
        return name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : "";
    }

    public String getQualifiedName() {
        return name;
    }

    public Type[] getAnnotations() {
        return annotations;
    }

    public FieldMeta[] getFields() {
        return fields;
    }

    public MethodMeta[] getMethods() {
        return methods;
    }

    @Override
    public Element getParent() {
        return null;
    }

    public Type getSuperClass() {
        return superClass;
    }

    public Type[] getInterfaces() {
        return interfaces;
    }

    public Type getType() {
        return loader.getType(name);
    }

    @Override
    public Kind getKind() {
        return Kind.CLASS;
    }

}
