package com.github.alantr7.bukkitplugin.annotations.processor.reader;

import com.github.alantr7.bukkitplugin.annotations.processor.meta.*;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class MetaLoader {

    private final ByteBufferReader reader;

    private final Map<String, ClassMeta> classes = new HashMap<>();

    private final Map<String, Type> types = new HashMap<>();

    {
        types.put("int", createType(Integer.class.getName()));
        types.put("long", createType(Long.class.getName()));
        types.put("boolean", createType(Boolean.class.getName()));
        types.put("short", createType(Short.class.getName()));
        types.put("byte", createType(Byte.class.getName()));
        types.put("void", createType(Void.class.getName()));
    }

    private final Set<String> annotations = new LinkedHashSet<>();

    public MetaLoader(byte[] bytes) {
        this.reader = new ByteBufferReader(bytes);
        this.read();

//        System.out.println("-------------------------------------------");
//        System.out.println("Loaded " + getClasses().length + " classes:");
//        for (var klass : getClasses()) {
//            System.out.println("  - " + klass.getSimpleName());
//            System.out.println("      Package: " + klass.getPackageName());
//            System.out.println("      Present: " + klass.getType().isPresent());
//            System.out.println("      Super: " + klass.getSuperClass().getQualifiedName());
//            System.out.println("        Present: " + klass.getSuperClass().isPresent());
//            System.out.println("      Interfaces: [" + klass.getInterfaces().length + "]");
//            for (var interf : klass.getInterfaces()) {
//                System.out.println("      - " + interf.getQualifiedName());
//            }
//            System.out.println();
//        }
    }

    private void read() {

        // File version
        int version = reader.readU1();
//        System.out.println("FILE VERSION: " + version + "\n");

        // Constant pool
        var constantPool = new String[reader.readU1()];
//        System.out.println("CONSTANT POOL:");
        for (int i = 0; i < constantPool.length; i++) {
            constantPool[i] = reader.readString();
//            System.out.println(" - " + constantPool[i]);
        }
//        System.out.println();

        // Annotations
        int[] annotations = new int[reader.readU1()];
//        System.out.println("ANNOTATIONS:");
        for (int i = 0; i < annotations.length; i++) {
            annotations[i] = reader.readU1();
            createType(constantPool[annotations[i]]); // TODO: Check this.. it registers annotation to the type map
//            System.out.println(" - " + constantPool[annotations[i]]);
        }
//        System.out.println();

        // Classes
        var classes = new ClassMeta[reader.readU1()];
//        System.out.println("CLASSES:");
        for (int i = 0; i < classes.length; i++) {
            var className = constantPool[reader.readU1()];
            var classSuper = constantPool[reader.readU1()];
            var classInterfaces = new Type[reader.readU1()];
            for (int j = 0; j < classInterfaces.length; j++) {
                classInterfaces[j] = createType(constantPool[reader.readU1()]);
            }

            var classAnnotations = _readAnnotations(constantPool, annotations);
            createType(className);

//            for (var annotation : classAnnotations) {
//                System.out.println("   @" + annotation.getQualifiedName());
//            }
//            System.out.println(" - " + className + " extends " + classSuper);

//            System.out.println("\n   FIELDS:");
            var classFields = new FieldMeta[reader.readU1()];
            for (int j = 0; j < classFields.length; j++) {
                var fieldName = constantPool[reader.readU1()];
                var fieldType = constantPool[reader.readU1()];
                var fieldAnnotations = _readAnnotations(constantPool, annotations);

                var field = new FieldMeta(this, className, fieldName, createType(fieldType), fieldAnnotations);
                classFields[j] = field;

//                for (var annotation : fieldAnnotations) {
//                    System.out.println("      @" + annotation.getQualifiedName());
//                }
//                System.out.println("    - " + fieldType + " " + fieldName + ";");
            }

//            System.out.println("\n   METHODS:");
            var classMethods = new MethodMeta[reader.readU1()];
            for (int j = 0; j < classMethods.length; j++) {
                var methodName = constantPool[reader.readU1()];
                var methodReturnType = constantPool[reader.readU1()];
                var parameters = new ParameterMeta[reader.readU1()];
                for (int k = 0; k < parameters.length; k++) {
                    var parameterType = constantPool[reader.readU1()];
                    var parameterAnnotations = _readAnnotations(constantPool, annotations);

                    parameters[k] = new ParameterMeta(createType(parameterType), parameterAnnotations);
                }

                var methodAnnotations = _readAnnotations(constantPool, annotations);

//                for (var annotation : methodAnnotations) {
//                    System.out.println("      @" + annotation.getQualifiedName());
//                }
//                if (parameters.length == 0) {
//                    System.out.println("    - " + methodReturnType + " " + methodName + "()");
//                } else {
//                    System.out.println("    - " + methodReturnType + " " + methodName + "(");
//                    for (var parameter : parameters) {
//                        for (var annotation : parameter.getAnnotations()) {
//                            System.out.println("        @" + annotation.getQualifiedName());
//                        }
//                        System.out.println("        " + parameter.getType().getQualifiedName() + ",");
//                    }
//                    System.out.println("      );");
//                }

                var method = new MethodMeta(this, className, methodName, createType(methodReturnType), methodAnnotations, parameters);
                classMethods[j] = method;
            }

            classes[i] = new ClassMeta(this, className, createType(classSuper), classInterfaces, classAnnotations, classFields, classMethods);
//            System.out.println();
//            System.out.println();
//            System.out.println();
        }

        // Finalize everything
        for (var klass : classes) {
            this.classes.put(klass.getQualifiedName(), klass);
        }
        for (var annotation : annotations) {
            this.annotations.add(constantPool[annotation]);
        }

    }

    private Type[] _readAnnotations(String[] constantPool, int[] annotationPointers) {
        var annotations = new Type[reader.readU1()];
        for (int i = 0; i < annotations.length; i++) {
            annotations[i] = createType(constantPool[annotationPointers[reader.readU1()]]);
        }

        return annotations;
    }

    private Type createType(String from) {
        return types.computeIfAbsent(from, Type::new);
    }

    public ClassMeta getClass(String name) {
        return classes.get(name);
    }

    public ClassMeta[] getClasses() {
        return classes.values().toArray(new ClassMeta[0]);
    }

    public Set<String> getAnnotations() {
        return annotations;
    }

    public Type getType(String type) {
        return types.get(type);
    }

}
