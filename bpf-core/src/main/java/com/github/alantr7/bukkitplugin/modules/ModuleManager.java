package com.github.alantr7.bukkitplugin.modules;

import com.github.alantr7.bukkitplugin.BukkitPlugin;

import java.util.HashMap;
import java.util.Map;

public class ModuleManager {

    private final BukkitPlugin plugin;

    private final Map<String, PluginModule> LOADED_MODULES = new HashMap<>();

    private static final Map<String, String> MODULE_PATHS = new HashMap<>();

    public ModuleManager(BukkitPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        prepareModules();
        MODULE_PATHS.forEach(this::initializeModule);
    }

    public void annotations() {
        LOADED_MODULES.forEach((name, module) -> module.registerAnnotations(plugin.getAnnotationManager()));
    }

    public void enable() {
        LOADED_MODULES.forEach((name, module) -> module.onPluginEnable());
    }

    public void disable() {
        LOADED_MODULES.forEach((name, module) -> module.onPluginDisable());
    }

    public PluginModule[] getLoadedModules() {
        return LOADED_MODULES.values().toArray(new PluginModule[0]);
    }

    private void prepareModules() {
        String packagePath = BukkitPlugin.class.getPackageName();
        MODULE_PATHS.put("gui",  packagePath + ".gui.GuiModule");
        MODULE_PATHS.put("commands",  packagePath + ".commands.CommandsModule");
    }

    private void initializeModule(String name, String path) {
        Class<?> moduleClass;
        try {
            moduleClass = Class.forName(path);
        } catch (Exception ignored) {
            plugin.getLogger().info("Module '%s' is not present.".formatted(name));
            return;
        }

        PluginModule instance;
        try {
            instance = (PluginModule) moduleClass.getConstructor().newInstance();
            var pluginField = PluginModule.class.getDeclaredField("plugin");
            pluginField.setAccessible(true);
            pluginField.set(instance, plugin);
            pluginField.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        instance.onInit();

        plugin.getLogger().info("Initialized '%s' module.".formatted(name));
        LOADED_MODULES.put(name, instance);
    }

}
