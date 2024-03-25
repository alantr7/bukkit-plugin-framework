package com.github.alantr7.bukkitplugin.annotations.processor.processing.executable;

public class ProvidedValue {

    private static final ProvidedValue EMPTY = new ProvidedValue(null);

    private final Object value;

    private ProvidedValue(Object value) {
        this.value = value;
    }

    public Object get() {
        return value;
    }

    public boolean isPresent() {
        return this != EMPTY;
    }

    public static ProvidedValue of(Object object) {
        return new ProvidedValue(object);
    }

    public static ProvidedValue skip() {
        return EMPTY;
    }

}
