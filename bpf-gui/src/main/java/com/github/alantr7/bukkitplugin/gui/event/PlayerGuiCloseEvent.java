package com.github.alantr7.bukkitplugin.gui.event;

import com.github.alantr7.bukkitplugin.gui.GUI;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerGuiCloseEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Getter
    private final Player player;

    @Getter
    private final GUI gui;

    public PlayerGuiCloseEvent(Player player, GUI gui) {
        this.player = player;
        this.gui = gui;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

}
