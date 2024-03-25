package com.github.alantr7.bukkitplugin.commands.executor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.Language;
import org.intellij.lang.annotations.RegExp;

@FunctionalInterface
public interface Evaluator<T> {

    T evaluate(String input) throws Exception;

    Evaluator<String> STRING = input -> input;

    Evaluator<Integer> INTEGER = Integer::parseInt;

    Evaluator<Player> PLAYER = Bukkit::getPlayer;

}
