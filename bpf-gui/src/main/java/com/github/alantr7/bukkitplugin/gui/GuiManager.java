package com.github.alantr7.bukkitplugin.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class GuiManager {

    final HashMap<UUID, GUI> openGuis = new HashMap<>();

    final GuiListener guiListener;

    public GuiManager() {
        this.guiListener = new GuiListener(this);
    }

    public GUI getOpenInventory(Player player) {
        return getOpenInventory(player.getUniqueId());
    }

    public GUI getOpenInventory(UUID uuid) {
        return openGuis.get(uuid);
    }

    public void disposeInventory(Player player) {
        disposeInventory(player.getUniqueId());
    }

    public void disposeInventory(UUID uuid) {
        openGuis.remove(uuid).dispose();
    }

    void removeInventory(UUID uuid) {
        openGuis.remove(uuid);
    }

    public List<Player> getPlayersWithActiveGUIs(Class<? extends GUI> guis) {
        List<Player> players = new ArrayList<>();

        openGuis.forEach((uuid, gui) -> {
            if (gui.getClass() == guis) {

                Player player = Bukkit.getPlayer(uuid);
                if (player != null)
                    players.add(player);

            }
        });

        return players;
    }

    @SafeVarargs
    public final Map<Class<? extends GUI>, Collection<Player>> getPlayersWithActiveGUIs(Class<? extends GUI>... guis) {
        if (guis.length == 0)
            return Collections.emptyMap();

        var map = new HashMap<Class<? extends GUI>, Collection<Player>>();
        openGuis.forEach((uuid, gui) -> {
            for (var gui1 : guis) {
                if (gui1 != gui.getClass())
                    continue;

                var player = Bukkit.getPlayer(uuid);
                if (player != null)
                    map.computeIfAbsent(gui1, v -> new LinkedList<>()).add(player);
            }
        });

        return map;
    }

    public GuiListener getEventListener() {
        return guiListener;
    }

    public void closeAll() {
        for (UUID uuid : openGuis.keySet()) {
            GUI gui = openGuis.get(uuid);
            gui.close();
        }

        openGuis.clear();
    }

}
