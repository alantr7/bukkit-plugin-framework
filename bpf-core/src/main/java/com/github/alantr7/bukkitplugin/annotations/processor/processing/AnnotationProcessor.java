package com.github.alantr7.bukkitplugin.annotations.processor.processing;

import com.github.alantr7.bukkitplugin.annotations.processor.meta.*;
import com.github.alantr7.bukkitplugin.annotations.processor.processing.executable.ArgumentsBuilder;
import com.github.alantr7.bukkitplugin.annotations.processor.processing.executable.ProvidedValue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class AnnotationProcessor<T extends Annotation> {

    public void filter(Element element, T annotation, ProcessChain chain) {
    }

    public void processClass(Class<?> element, ClassMeta meta, Object instance, T annotation) {
    }

    public void processField(Field field, FieldMeta meta, Object classInstance, T annotation) {
    }

    public void processMethod(Method method, MethodMeta meta, Object classInstance, T annotation) {
    }

    public void buildArguments(ArgumentsBuilder builder, T annotation) {
    }

    public ProvidedValue getParameterValue(ParameterMeta meta, Object classInstance, T annotation) {
        return ProvidedValue.skip();
    }

    public ProvidedValue getFieldValue(Field field, FieldMeta meta, Object classInstance, T annotation) {
        return ProvidedValue.skip();
    }

}
