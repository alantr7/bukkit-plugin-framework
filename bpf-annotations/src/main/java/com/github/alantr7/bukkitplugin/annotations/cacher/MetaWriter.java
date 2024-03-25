package com.github.alantr7.bukkitplugin.annotations.cacher;

import com.github.alantr7.bukkitplugin.annotations.AnnotationProcessor;
import com.github.alantr7.bukkitplugin.annotations.AnnotationRelocator;
import com.github.alantr7.bukkitplugin.annotations.cacher.builder.ByteArrayBuilder;
import com.github.alantr7.bukkitplugin.annotations.cacher.builder.ConstantPoolBuilder;
import com.github.alantr7.bukkitplugin.annotations.cacher.map.ClassMap;

import javax.tools.Diagnostic;
import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Predicate;

public class MetaWriter {

    final ConstantPoolBuilder constantPool = new ConstantPoolBuilder();

    final ByteArrayBuilder writer = new ByteArrayBuilder();

    private final Set<ClassMap> classes = new HashSet<>();

    private final List<String> annotations = new LinkedList<>();

    public static final int VERSION = 1;

    private MetaWriter() {
    }

    public byte[] writeToByteArray() {
        _createConstantPool(); // Gathers constants for the constant pool

        // Write version
        writer.writeU1(VERSION);

        // Write constant pool
        writer.writeU1(constantPool.getEntries().size()); // Constant pool size
        for (var entry : constantPool.getEntries())
            writer.writeString(AnnotationRelocator.relocate(entry));

        // Write annotation pointers
        writer.writeU1(annotations.size());
        for (var annotation : annotations)
            writer.writeU1(constantPool.indexOf(annotation));

        // Classes
        writer.writeU1(classes.size()); // Classes count
        for (var map : classes) {

            writer.writeU1(constantPool.indexOf(map.name)); // Class name in CP
            writer.writeU1(constantPool.indexOf(map.superclass)); // Super class
            writer.writeU1(map.interfaces.length);
            for (var interf : map.interfaces)
                writer.writeU1(constantPool.indexOf(interf)); // Interfaces
            _writeAnnotations(map.annotations); // Class annotations

            // Write fields
            writer.writeU1(map.fields.size()); // Fields count
            for (var field : map.fields.values()) {
                writer.writeU1(constantPool.indexOf(field.name)); // Field name in CP
                writer.writeU1(constantPool.indexOf(field.type)); // Field type in CP

                // Write annotations
                _writeAnnotations(field.annotations);
            }


            // Write methods
            writer.writeU1(map.methods.size()); // Methods count
            for (var method : map.methods.values()) {
                writer.writeU1(constantPool.indexOf(method.name)); // Method name in CP
                writer.writeU1(constantPool.indexOf(method.returnType)); // Method type in CP

                // Write parameters
                writer.writeU1(method.parameters.length);
                for (int i = 0; i < method.parameters.length; i++) {
                    writer.writeU1(constantPool.indexOf(method.parameters[i].type));
                    _writeAnnotations(method.parameters[i].annotations);
                }

                // Write annotations
                _writeAnnotations(method.annotations);

                AnnotationProcessor.inst.getMessager().printMessage(Diagnostic.Kind.NOTE, "Method annotations:");
                AnnotationProcessor.inst.getMessager().printMessage(Diagnostic.Kind.NOTE, method.annotations.toString());
            }
        }

        return writer.getBuffer();
    }

    private void _createConstantPool() {
        for (var map : classes) {
            constantPool.add(map.name);
            constantPool.add(map.superclass);
            for (var interf : map.interfaces)
                constantPool.add(interf);
            for (var annotation : map.annotations) {
                constantPool.add(annotation);
                annotations.add(annotation);
            }

            for (var field : map.fields.values()) {
                constantPool.add(field.name);
                constantPool.add(field.type);
                for (var annotation : field.annotations) {
                    constantPool.add(annotation);
                    annotations.add(annotation);
                }
            }

            for (var field : map.methods.values()) {
                constantPool.add(field.name);
                constantPool.add(field.returnType);
                for (var parameter : field.parameters) {
                    constantPool.add(parameter.type);
                    for (var annotation : parameter.annotations) {
                        constantPool.add(annotation);
                        annotations.add(annotation);
                    }
                }
                for (var annotation : field.annotations) {
                    constantPool.add(annotation);
                    annotations.add(annotation);
                }
            }
        }
    }

    private void _writeAnnotations(List<String> annotations) {
        writer.writeU1(annotations.size()); // Annotations count
        for (var annotation : annotations) {
            writer.writeU1(this.annotations.indexOf(annotation));
        }
    }

    public static byte[] writeToByteArray(ClassMap[] classes) {
        var writer = new MetaWriter();
        writer.classes.addAll(Arrays.asList(classes));

        return writer.writeToByteArray();
    }

    public static void writeToFile(ClassMap[] classes, File file) {
        var array = writeToByteArray(classes);
        try {
            Files.write(file.toPath(), array);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
