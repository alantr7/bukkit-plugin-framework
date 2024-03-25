package com.github.alantr7.bukkitplugin.annotations.cacher.map;

import java.util.ArrayList;
import java.util.List;

public class MappedField {

    public final String name;

    public final String type;

    public List<String> annotations = new ArrayList<>();

    public MappedField(String name, String type) {
        this.name = name;
        this.type = type;
    }

}
