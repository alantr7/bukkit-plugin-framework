package com.github.alantr7.bukkitplugin.annotations.cacher.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassMap {

    public final String name;

    public String superclass;

    public String[] interfaces;

    public ClassMap(String name) {
        this.name = name;
    }

    public Map<String, MappedField> fields = new HashMap<>();

    public Map<String, MappedMethod> methods = new HashMap<>();

    public List<String> annotations = new ArrayList<>();

    @Override
    public String toString() {
        return "ClassMap{" +
                "name='" + name + '\'' +
                ", superclass='" + superclass + '\'' +
                '}';
    }
}
