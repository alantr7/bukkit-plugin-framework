package com.github.alantr7.bukkitplugin.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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

    public List<Player> getPlayersWithGUI(Class<? extends GUI> guis) {

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
