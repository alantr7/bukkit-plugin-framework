package com.github.alantr7.bukkitplugin.annotations.plugin;

import com.github.alantr7.bukkitplugin.annotations.generative.Permission;

public record PermissionDefinition(String node, Permission.Allowed allowed) {

}
