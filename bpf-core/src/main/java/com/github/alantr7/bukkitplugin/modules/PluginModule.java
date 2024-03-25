package com.github.alantr7.bukkitplugin.modules;

import com.github.alantr7.bukkitplugin.BukkitPlugin;
import com.github.alantr7.bukkitplugin.annotations.processor.UserAnnotationManager;

public abstract class PluginModule {

    private BukkitPlugin plugin;

    protected void onInit() {
    }

    protected void registerAnnotations(UserAnnotationManager manager) {
    }

    protected abstract void onPluginEnable();

    protected abstract void onPluginDisable();

    protected BukkitPlugin getPlugin() {
        return plugin;
    }

}
