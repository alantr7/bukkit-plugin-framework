package com.github.alantr7.bukkitplugin.gui;

import com.github.alantr7.bukkitplugin.modules.PluginModule;
import lombok.Getter;
import org.bukkit.Bukkit;

public class GuiModule extends PluginModule {

    @Getter
    static GuiManager manager;

    @Override
    protected void onPluginEnable() {
        manager = new GuiManager();
        Bukkit.getPluginManager().registerEvents(manager.guiListener, getPlugin());
    }

    @Override
    protected void onPluginDisable() {
        manager.closeAll();
    }

}
