package com.github.alantr7.bukkitplugin.beans;

import com.github.alantr7.bukkitplugin.annotations.config.Config;
import com.github.alantr7.bukkitplugin.annotations.config.ConfigOption;
import com.github.alantr7.bukkitplugin.annotations.core.*;
import com.github.alantr7.bukkitplugin.annotations.processor.meta.*;
import com.github.alantr7.bukkitplugin.annotations.processor.processing.AnnotationProcessor;
import com.github.alantr7.bukkitplugin.annotations.processor.processing.ConfigLoader;
import com.github.alantr7.bukkitplugin.annotations.processor.processing.ProcessChain;
import com.github.alantr7.bukkitplugin.annotations.processor.processing.executable.ProvidedValue;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class DefaultProcessors {

    private final BeanManager manager;

    public DefaultProcessors(BeanManager manager) {
        this.manager = manager;
    }

    final AnnotationProcessor<Singleton> SINGLETON = new AnnotationProcessor<>() {
        @Override
        public void processClass(Class<?> element, ClassMeta meta, Object instance, Singleton annotation) {
            manager.singletons.put(meta.getQualifiedName(), instance);
        }
    };

    final AnnotationProcessor<Config> CONFIG = new AnnotationProcessor<>() {
        @Override
        public void processClass(Class<?> element, ClassMeta meta, Object instance, Config annotation) {
            manager.singletons.put(meta.getQualifiedName(), instance);

            File targetFile = new File(manager.plugin.getDataFolder(), annotation.value());

            // Save default config if it doesn't exist
            if (!targetFile.exists()) {
                manager.plugin.getDataFolder().mkdirs();

                if (manager.plugin.getResource(annotation.value()) != null) {
                    manager.plugin.saveResource(annotation.value(), false);
                } else try {
                    targetFile.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            FileConfiguration config = YamlConfiguration.loadConfiguration(targetFile);
            manager.configs.put(element.getName(), config);
        }
    };

    final AnnotationProcessor<ConfigOption> CONFIG_OPTION = new AnnotationProcessor<>() {
        @Override
        public ProvidedValue getFieldValue(Field field, FieldMeta meta, Object classInstance, ConfigOption annotation) {
            FileConfiguration config = manager.configs.get(classInstance.getClass().getName());
            if (config == null)
                return ProvidedValue.skip();

            try {
                return ConfigLoader.loadOption(config, field.getType(), field.get(classInstance), annotation);
            } catch (Exception e) {
                e.printStackTrace();
                return ProvidedValue.skip();
            }
        }
    };

    final AnnotationProcessor<Inject> INJECT = new AnnotationProcessor<>() {
        @Override
        public ProvidedValue getFieldValue(Field field, FieldMeta meta, Object classInstance, Inject annotation) {
            return ProvidedValue.of(manager.singletons.get(meta.getType().getQualifiedName()));
        }

        @Override
        public ProvidedValue getParameterValue(ParameterMeta meta, Object classInstance, Inject annotation) {
            return ProvidedValue.of(manager.singletons.get(meta.getType().getQualifiedName()));
        }
    };

    final AnnotationProcessor<Invoke> INVOKE = new AnnotationProcessor<>() {
        @Override
        public void processMethod(Method method, MethodMeta meta, Object classInstance, Invoke annotation) {
            if (annotation.value() == Invoke.Schedule.ASAP) {
                manager.annotationManager.invoke(classInstance, method, meta);
            } else {
                manager.tasks.computeIfAbsent(annotation.value(), v -> new LinkedList<>())
                        .add(() -> manager.annotationManager.invoke(classInstance, method, meta));
            }
        }
    };

    final AnnotationProcessor<InvokePeriodically> INVOKE_PERIODICALLY = new AnnotationProcessor<>() {
        @Override
        public void processMethod(Method method, MethodMeta meta, Object classInstance, InvokePeriodically annotation) {
            Bukkit.getScheduler().runTaskTimer(
                    manager.plugin,
                    new Consumer<>() {
                        long ticks = 0;

                        @Override
                        public void accept(BukkitTask task) {
                            manager.annotationManager.invoke(classInstance, method, meta);
                            if (++ticks == annotation.limit())
                                task.cancel();
                        }
                    },
                    annotation.delay(),
                    annotation.interval()
            );
        }
    };

    final AnnotationProcessor<RequiresPlugin> REQUIRES_PLUGIN = new AnnotationProcessor<>() {
        @Override
        public void filter(Element element, RequiresPlugin annotation, ProcessChain chain) {
            if (!Bukkit.getPluginManager().isPluginEnabled(annotation.value()))
                chain.cancel();
        }
    };

    @SuppressWarnings("unchecked")
    final AnnotationProcessor<EventHandler> EVENT_HANDLER = new AnnotationProcessor<>() {
        public void processMethod(Method method, MethodMeta meta, Object classInstance, EventHandler annotation) {
            Parameter[] params = method.getParameters();
            Logger logger = manager.plugin.getLogger();
            String className = classInstance.getClass().getName();
            if (params.length != 1) {
                logger.warning("[BeanManager] Invalid parameter count for event handler: " + className + "#" + method.getName());
            } else if (!Event.class.isAssignableFrom(params[0].getType())) {
                logger.warning("[BeanManager] Invalid event type for event handler: " + className + "#" + method.getName());
            } else {
                try {
                    method.setAccessible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                Bukkit.getPluginManager().registerEvent((Class<Event>) params[0].getType(), DefaultProcessors.this.manager.plugin, annotation.priority(), (listener, event) -> {
                    try {
                        if (params[0].getType().isInstance(event)) {
                            method.invoke(classInstance, event);
                        }
                    } catch (Exception e) {
                        logger.warning("[BeanManager] Error in event execution for " + className + "#" + method.getName());
                        e.printStackTrace();
                    }

                }, DefaultProcessors.this.manager.plugin, annotation.ignoreCancelled());
            }
        }
    };

}
