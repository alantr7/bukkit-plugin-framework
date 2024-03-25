package com.github.alantr7.bukkitplugin.annotations.processor.meta;

public class Type {

    private final String name;

    private boolean isLoaded;

    private Class<?> reflection;

    public Type(String name) {
        this.name = name;
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
