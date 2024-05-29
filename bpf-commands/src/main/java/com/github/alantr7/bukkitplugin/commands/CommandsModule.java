package com.github.alantr7.bukkitplugin.commands;

import com.github.alantr7.bukkitplugin.annotations.processor.UserAnnotationManager;
import com.github.alantr7.bukkitplugin.annotations.processor.meta.FieldMeta;
import com.github.alantr7.bukkitplugin.annotations.processor.processing.AnnotationProcessor;
import com.github.alantr7.bukkitplugin.commands.annotations.CommandHandler;
import com.github.alantr7.bukkitplugin.commands.executor.CommandExecutor;
import com.github.alantr7.bukkitplugin.commands.registry.Command;
import com.github.alantr7.bukkitplugin.modules.PluginModule;

import java.lang.reflect.Field;

public class CommandsModule extends PluginModule {

    private CommandExecutor executor;

    private static CommandsModule instance;

    @Override
    protected void onInit() {
        executor = new CommandExecutor(getPlugin());
        instance = this;
    }

    @Override
    protected void registerAnnotations(UserAnnotationManager manager) {
        manager.registerProcessor(CommandHandler.class, new AnnotationProcessor<>() {
            @Override
            public void processField(Field field, FieldMeta meta, Object classInstance, CommandHandler annotation) {
                if (!meta.getType().getQualifiedName().equalsIgnoreCase(Command.class.getName())) {
                    getPlugin().getLogger().warning("Command '" + meta.getName() + "' is not of valid type.");
                    return;
                }

                try {
                    field.setAccessible(true);
                    var command = (Command) field.get(classInstance);
                    field.setAccessible(false);

                    if (!executor.isRegistered(command.getName())) {
                        var pluginCommand = getPlugin().getCommand(command.getName());
                        if (pluginCommand != null) {
                            pluginCommand.setExecutor(executor);
                        }
                    }
                    executor.registerCommand(command);
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                    getPlugin().getLogger().warning("Could not register command '" + meta.getName() + "'.");
                }
            }
        });
    }

    @Override
    protected void onPluginEnable() {
    }

    @Override
    protected void onPluginDisable() {

    }

    public CommandExecutor getExecutor() {
        return executor;
    }

    public static CommandsModule instance() {
        return instance;
    }

}