package com.github.alantr7.bukkitplugin.annotations.plugin;

public record CommandDefinition(String name, String description, String[] aliases, String permission) {
}
