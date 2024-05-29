package com.github.alantr7.bukkitplugin.annotations.processor;

import com.github.alantr7.bukkitplugin.annotations.processor.meta.ClassMeta;
import com.github.alantr7.bukkitplugin.annotations.processor.meta.Element;
import com.github.alantr7.bukkitplugin.annotations.processor.meta.MethodMeta;
import com.github.alantr7.bukkitplugin.annotations.processor.meta.Type;
import com.github.alantr7.bukkitplugin.annotations.processor.processing.AnnotationProcessor;
import com.github.alantr7.bukkitplugin.annotations.processor.processing.ProcessChain;
import com.github.alantr7.bukkitplugin.annotations.processor.processing.executable.ArgumentsBuilder;
import com.github.alantr7.bukkitplugin.annotations.processor.processing.executable.ProvidedValue;
import com.github.alantr7.bukkitplugin.annotations.processor.reader.MetaLoader;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class UserAnnotationManager {

    private final Map<String, AnnotationProcessor<Annotation>> annotationProcessors = new HashMap<>();

    private final Map<String, Class<Annotation>> annotations = new HashMap<>();

    public <T extends Annotation> void registerProcessor(Class<T> annotationClass, AnnotationProcessor<T> processor) {
        this.annotationProcessors.put(annotationClass.getName(), (AnnotationProcessor<Annotation>) processor);
    }

    public void initialize(MetaLoader stream) {

        for (var annotation : stream.getAnnotations()) {
            if (!annotationProcessors.containsKey(annotation)) {
                System.out.println("No annotation processors for @" + annotation + ". Ignoring the annotation.");
                continue;
            }

            try {
                annotations.put(annotation, (Class<Annotation>) stream.getType(annotation).reflect());
            } catch (Exception ignored) {
                ignored.printStackTrace();
                System.out.println("Annotation is not present in the classpath: " + annotation);
            }
        }

        Map<ClassMeta, Object> instances = new HashMap<>();


        // Find and instantiate classes
        for (var klass : stream.getClasses()) {

            if (!klass.getType().isPresent()) {
                System.out.println("Skipping class " + klass.getSimpleName() + " as it is not present.");
                continue;
            }

            if (!klass.getSuperClass().isPresent()) {
                System.out.println("Skipping class " + klass.getSimpleName() + " as its super class is not present.");
                continue;
            }

            // Attempt to load the class
            Class<?> loadedClass;

            try {
                loadedClass = klass.getType().reflect();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Could not load class " + klass.getQualifiedName());

                continue;
            }

            if (!filter(klass, loadedClass, klass.getAnnotations()))
                continue;

            // Attempt to create instance
            Object instance;

            try {
                instance = loadedClass.getConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            processAnnotations(loadedClass, klass.getAnnotations(), (proc, ann) -> proc.processClass(loadedClass, klass, instance, ann));
            instances.put(klass, instance);

        }

        // Scan fields and methods of instantiated classes
        instances.forEach((klass, instance) -> {
            Class<?> loadedClass = instance.getClass();
            for (var field : klass.getFields()) {
                if (!field.getType().isPresent()) {
                    System.out.println("Field type is not loaded. Skipping it.");
                    continue;
                }

                Field loadedField;
                try {
                    loadedField = loadedClass.getDeclaredField(field.getName());
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }

                if (!filter(field, loadedField, field.getAnnotations()))
                    continue;

                processAnnotations(loadedField, field.getAnnotations(), (processor, ann) -> processor
                        .processField(loadedField, field, instance, ann)
                );

                var fieldValue = requestValue(loadedField, field.getAnnotations(), (proc, ann) -> proc.getFieldValue(loadedField, field, instance, ann));
                if (fieldValue.isPresent()) {
                    try {
                        loadedField.setAccessible(true);
                        loadedField.set(instance, fieldValue.get());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            for (var method : klass.getMethods()) {
                if (!method.getReturnType().isPresent()) {
                    System.out.println("Method return type is not loaded. Skipping it.");
                    continue;
                }

                var parametersMeta = method.getParameters();
                var parameterTypes = new Class[parametersMeta.length];
                for (int i = 0; i < parameterTypes.length; i++) {
                    if (!parametersMeta[i].getType().isPresent()) {
                        System.out.println("Method parameter type is not loaded. Skipping method.");
                        continue;
                    }

                    parameterTypes[i] = parametersMeta[i].getType().reflectUnsafe();
                }

                Method loadedMethod;
                try {
                    loadedMethod = loadedClass.getDeclaredMethod(method.getName(), parameterTypes);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }

                if (!filter(method, loadedMethod, method.getAnnotations()))
                    continue;

                processAnnotations(loadedMethod, method.getAnnotations(), (proc, ann) -> proc.processMethod(loadedMethod, method, instance, ann));
            }
        });

    }

    public void invoke(Object classInstance, Method handle, MethodMeta meta) {
        Object[] arguments;
        if (meta.getParameters().length > 0) {
            var builder = new ArgumentsBuilder(meta, classInstance);

            // Gather arguments from method annotations
            processAnnotations(handle, meta.getAnnotations(), (proc, ann) ->
                    proc.buildArguments(builder, ann));

            // Gather arguments from parameter annotations
            for (int i = 0; i < meta.getParameters().length; i++) {
                var param = meta.getParameters()[i];
                var value = requestValue(handle.getParameters()[i], param.getAnnotations(), (proc, ann) ->
                        proc.getParameterValue(param, classInstance, ann));

                if (value.isPresent())
                    builder.setArgument(i, value.get());
            }

            // Set remaining arguments to null if @NotNull is not present
            for (int i = 0; i < meta.getParameters().length; i++) {
                if (builder.isArgumentProvided(i))
                    continue;

                if (!handle.getParameters()[i].isAnnotationPresent(NotNull.class))
                    builder.setArgument(i, null);
            }

            // Check if all arguments are provided
            if (!builder.isComplete()) {
                System.out.println("Could not build method arguments.");
                return;
            }

            arguments = builder.getArguments();
        } else {
            arguments = new Object[0];
        }

        try {
            handle.setAccessible(true);
            handle.invoke(classInstance, arguments);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean filter(Element element, AnnotatedElement annotated, Type[] annotations) {
        var processChain = new ProcessChain();
        for (var annotation : annotations) {
            var processor = annotationProcessors.get(annotation.getQualifiedName());

            if (processor == null) {
                continue;
            }

            var annotationClass = this.annotations.get(annotation.getQualifiedName());
            if (annotationClass == null) {
                System.out.println("Annotation not found: " + annotation.getQualifiedName());
                continue;
            }

            var classAnnotation = annotated.getAnnotation(annotationClass);
            processor.filter(element, classAnnotation, processChain);

            if (processChain.isCanceled()) {
                System.out.println("Processing canceled by @" + annotation.getQualifiedName());
                return false;
            }
        }

        return true;
    }

    private void processAnnotations(AnnotatedElement element, Type[] annotations, BiConsumer<AnnotationProcessor<Annotation>, Annotation> annotationConsumer) {
        for (var annotation : annotations) {
            var entry = getProcessorEntry(annotation.getQualifiedName());
            if (entry == null)
                continue;

            var elementAnnotation = element.getAnnotation(entry.annotationClass);
            annotationConsumer.accept(entry.processor, elementAnnotation);
        }
    }

    private ProvidedValue requestValue(AnnotatedElement element, Type[] annotations, BiFunction<AnnotationProcessor<Annotation>, Annotation, ProvidedValue> processor) {
        for (var annotation : annotations) {
            var entry = getProcessorEntry(annotation.getQualifiedName());
            if (entry == null)
                continue;

            var elementAnnotation = element.getAnnotation(entry.annotationClass);
            var result = processor.apply(entry.processor, elementAnnotation);

            if (result == null || !result.isPresent())
                continue;

            return result;
        }

        return ProvidedValue.skip();
    }

    private AnnotationProcessorEntry getProcessorEntry(String annotationName) {
        var processor = this.annotationProcessors.get(annotationName);
        if (processor == null)
            return null;

        var annotationClass = this.annotations.get(annotationName);
        if (annotationClass == null) {
            System.out.println("Annotation not found: " + annotationName);
            return null;
        }

        return new AnnotationProcessorEntry(processor, annotationClass);
    }

    record AnnotationProcessorEntry(AnnotationProcessor<Annotation> processor, Class<? extends Annotation> annotationClass) {
    }

}
