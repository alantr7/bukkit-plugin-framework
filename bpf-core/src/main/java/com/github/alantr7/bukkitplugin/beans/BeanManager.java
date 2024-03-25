package com.github.alantr7.bukkitplugin.beans;

import com.github.alantr7.bukkitplugin.BukkitPlugin;
import com.github.alantr7.bukkitplugin.annotations.core.*;
import com.github.alantr7.bukkitplugin.annotations.processor.UserAnnotationManager;
import com.github.alantr7.bukkitplugin.annotations.processor.reader.MetaLoader;
import lombok.Getter;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanManager {

    final BukkitPlugin plugin;

    @Getter
    UserAnnotationManager annotationManager = new UserAnnotationManager();

    private final DefaultProcessors processors;

    final Map<String, Object> singletons = new HashMap<>();

    final Map<Integer, BukkitTask> timers = new HashMap<>();

    final Map<Invoke.Schedule, List<Runnable>> tasks = new HashMap<>();

    public BeanManager(BukkitPlugin plugin) {
        this.plugin = plugin;
        this.processors = new DefaultProcessors(this);

        singletons.put(plugin.getClass().getName(), plugin);
        singletons.put(BukkitPlugin.class.getName(), plugin);
    }

    public void init() {
        // Attempt to read annotations
        try (var resource = plugin.getResource("beans.dat")) {

            if (resource == null) {
                return;
            }

            annotationManager.registerProcessor(Singleton.class, processors.SINGLETON);
            annotationManager.registerProcessor(Inject.class, processors.INJECT);
            annotationManager.registerProcessor(Invoke.class, processors.INVOKE);
            annotationManager.registerProcessor(InvokePeriodically.class, processors.INVOKE_PERIODICALLY);
            annotationManager.registerProcessor(RequiresPlugin.class, processors.REQUIRES_PLUGIN);

            var loader = new MetaLoader(resource.readAllBytes());
            annotationManager.initialize(loader);
            System.out.println("Initialized :)");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void executeTasks(Invoke.Schedule schedule) {
        tasks.getOrDefault(schedule, Collections.emptyList()).forEach(Runnable::run);
    }

}
