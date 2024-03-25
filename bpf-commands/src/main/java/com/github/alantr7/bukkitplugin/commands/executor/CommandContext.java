package com.github.alantr7.bukkitplugin.commands.executor;

import com.github.alantr7.bukkitplugin.commands.registry.Command;
import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.Optional;

public class CommandContext {

    @Getter
    private final CommandSender executor;

    private final Command command;

    private Object[] arguments;

    public CommandContext(CommandSender executor, Command command, int arguments) {
        this.executor = executor;
        this.command = command;
        this.arguments = new Object[arguments];
    }

    public Object getArgument(String name) {
        return arguments[command.getVariableParameterIndex(name)];
    }

    void setArgument(int index, Object argument) {
        this.arguments[index] = argument;
    }

    public Optional<Object> optArgument(String name) {
        return Optional.ofNullable(getArgument(name));
    }

    public void respond(String message) {
        executor.sendMessage(message);
    }

}
