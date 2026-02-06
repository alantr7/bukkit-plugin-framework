package com.github.alantr7.bukkitplugin.annotations.processor.meta;

import lombok.Getter;

public class Type {

    private final String name;

    @Getter
    private final String nameWithGenerics;

    private boolean isLoaded;

    private Class<?> reflection;

    public Type(String name) {
        this.name = name.contains("<") && name.contains(">") ? name.substring(0, name.indexOf("<")) : name;
        this.nameWithGenerics = name;
    }

    public String getQualifiedName() {
        return name;
    }

    public Class<?> reflect() throws Exception {
        return reflection != null ? reflection : (reflection = loadClass(true));
    }

    public Class<?> reflectUnsafe() {
        try {
            return reflect();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isPresent() {
        try {
            loadClass(false);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private Class<?> loadClass(boolean printStackTrace) throws Exception {
        return Class.forName(name);
    }

}
