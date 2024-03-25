package com.github.alantr7.bukkitplugin.annotations.cacher.builder;

import java.util.*;

public class ConstantPoolBuilder {

    private final Map<String, Integer> constantPool = new HashMap<>();

    private int nextIndex = 0;

    public Collection<String> getEntries() {
        var tree = new ArrayList<>(constantPool.entrySet());
        tree.sort(Map.Entry.comparingByValue());

        var array = new LinkedList<String>();
        for (var it : tree) {
            array.add(it.getKey());
        }

        return array;
    }

    public void add(String entry) {
        if (constantPool.containsKey(entry))
            return;

        constantPool.put(entry, nextIndex++);
    }

    public String get(int index) {
        return constantPool.entrySet().stream().filter(entry -> entry.getValue() == index).findFirst().flatMap(entry -> {
            return Optional.of(entry.getKey());
        }).orElse(null);
    }

    public int indexOf(String entry) {
        return constantPool.getOrDefault(entry, -1);
    }

    @Override
    public String toString() {
        return constantPool.keySet().toString();
    }

}
