package com.github.alantr7.bukkitplugin.commands.registry;

import com.github.alantr7.bukkitplugin.commands.executor.CommandContext;
import com.github.alantr7.bukkitplugin.commands.factory.CommandBuilder;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.Map;
import java.util.function.Function;

public class Command {

    @Getter
    private final String name;

    @Getter
    private final int matches;

    @Getter
    private final Parameter[] parameters;

    @Getter
    private final String permission;

    private final Map<String, Integer> variableParametersIndices;

    @Getter
    private final Function<CommandContext, Object> handler;

    public Command(String name, Parameter[] parameters, String permission, int matches, Map<String, Integer> variableIndices, Function<CommandContext, Object> handler) {
        this.name = name;
        this.parameters = parameters;
        this.permission = permission;
        this.matches = matches;
        this.variableParametersIndices = variableIndices;
        this.handler = handler;
    }

    public int getVariableParameterIndex(String name) {
        return variableParametersIndices.getOrDefault(name, -1);
    }

    public void execute(CommandContext context) {
        handler.apply(context);
    }

    public boolean isPermitted(CommandSender sender) {
        return sender instanceof ConsoleCommandSender || this.permission == null || sender.hasPermission(this.permission);
    }

    public static CommandBuilder name(String name) {
        return CommandBuilder.using(name);
    }

}
