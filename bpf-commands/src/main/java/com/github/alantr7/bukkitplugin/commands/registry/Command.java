package com.github.alantr7.bukkitplugin.commands.registry;

import com.github.alantr7.bukkitplugin.commands.executor.CommandContext;
import com.github.alantr7.bukkitplugin.commands.factory.CommandBuilder;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.function.Function;

public class Command {

    private final String name;

    private final int matches;

    private final Parameter[] parameters;

    private final Map<String, Integer> variableParametersIndices;

    private final Function<CommandContext, Object> handler;

    public Command(String name, Parameter[] parameters, int matches, Map<String, Integer> variableIndices, Function<CommandContext, Object> handler) {
        this.name = name;
        this.parameters = parameters;
        this.matches = matches;
        this.variableParametersIndices = variableIndices;
        this.handler = handler;
    }

    public int getVariableParameterIndex(String name) {
        return variableParametersIndices.getOrDefault(name, -1);
    }

    public Parameter[] getParameters() {
        return parameters;
    }

    public int getMatches() {
        return matches;
    }

    public Function<CommandContext, Object> getHandler() {
        return handler;
    }

    public String getName() {
        return name;
    }

    public void execute(CommandContext context) {
        handler.apply(context);
    }

    public boolean isPermitted(CommandSender sender) {
        return true;
    }

    public static CommandBuilder name(String name) {
        return CommandBuilder.using(name);
    }

}
