package com.github.alantr7.bukkitplugin.beans;

import com.github.alantr7.bukkitplugin.annotations.core.*;
import com.github.alantr7.bukkitplugin.annotations.processor.meta.*;
import com.github.alantr7.bukkitplugin.annotations.processor.processing.AnnotationProcessor;
import com.github.alantr7.bukkitplugin.annotations.processor.processing.ProcessChain;
import com.github.alantr7.bukkitplugin.annotations.processor.processing.executable.ProvidedValue;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.function.Consumer;

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

}
