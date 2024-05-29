package com.github.alantr7.bukkitplugin;

import com.github.alantr7.bukkitplugin.annotations.core.Invoke;
import com.github.alantr7.bukkitplugin.annotations.processor.UserAnnotationManager;
import com.github.alantr7.bukkitplugin.beans.BeanManager;
import com.github.alantr7.bukkitplugin.modules.ModuleManager;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class BukkitPlugin extends JavaPlugin {

    private final ModuleManager moduleManager = new ModuleManager(this);

    private final BeanManager beanManager = new BeanManager(this);

    public final void onEnable() {
        moduleManager.init();
        moduleManager.annotations();

        beanManager.init();
        moduleManager.enable();

        beanManager.executeTasks(Invoke.Schedule.BEFORE_PLUGIN_ENABLE);
        onPluginEnable();
        beanManager.executeTasks(Invoke.Schedule.AFTER_PLUGIN_ENABLE);
    }

    public final void onDisable() {
        moduleManager.disable();

        beanManager.executeTasks(Invoke.Schedule.BEFORE_PLUGIN_DISABLE);
        onPluginDisable();
        beanManager.executeTasks(Invoke.Schedule.AFTER_PLUGIN_DISABLE);
    }

    protected abstract void onPluginEnable();

    protected abstract void onPluginDisable();

    public UserAnnotationManager getAnnotationManager() {
        return beanManager.getAnnotationManager();
    }

    public <T> T getSingleton(Class<T> clazz) {
        return beanManager.getSingleton(clazz);
    }

}