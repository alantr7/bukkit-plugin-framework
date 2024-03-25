package com.github.alantr7.bukkitplugin.annotations.cacher.map;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MappedMethod {

    public final String name;

    public final String returnType;

    public final Parameter[] parameters;

    public final Map<String, Parameter> parametersMap = new LinkedHashMap<>();

    public List<String> annotations = new ArrayList<>();

    public MappedMethod(String name, String returnType, Parameter[] parameters) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
        for (var parameter : parameters) {
            this.parametersMap.put(parameter.name, parameter);
        }
    }

    @Override
    public String toString() {
        return name + ": " + returnType;
    }

    public static class Parameter {

        public final String type;

        public final String name;

        public List<String> annotations = new ArrayList<>();

        public Parameter(String type, String name) {
            this.type = type;
            this.name = name;
        }

    }

}
