package org.fulib.fx;

import org.fulib.fx.annotation.controller.*;
import org.fulib.fx.annotation.event.onDestroy;
import org.fulib.fx.annotation.event.onInit;
import org.fulib.fx.annotation.event.onRender;
import org.fulib.fx.annotation.param.Param;
import org.fulib.fx.annotation.param.Params;
import org.fulib.fx.annotation.param.ParamsMap;
import org.fulib.fx.util.ControllerUtil;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Stream;

public class FxClassGenerator {
    private static final String CLASS_SUFFIX = "_Fx";
    private final ProcessingEnvironment processingEnv;

    /**
     * The (erased) `javafx.beans.value.WritableValue` type.
     */
    private final TypeMirror writableValue;

    /**
     * The `setValue` method of `javafx.beans.value.WritableValue`.
     */
    private final ExecutableElement genericSetValue;

    /**
     * The `javafx.scene.Parent` type.
     */
    private final TypeMirror parent;

    private final FxmlGenerator fxmlGenerator;
    private final Map<Element, FileObject> fxmlFiles = new HashMap<>();

    public FxClassGenerator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        this.fxmlGenerator = new FxmlGenerator(processingEnv);

        final TypeElement writableValue = processingEnv.getElementUtils().getTypeElement("javafx.beans.value.WritableValue");
        this.writableValue = processingEnv.getTypeUtils().erasure(writableValue.asType());

        genericSetValue = writableValue.getEnclosedElements()
            .stream()
            .filter(e -> e instanceof ExecutableElement)
            .map(e -> (ExecutableElement) e)
            .filter(e -> "setValue".equals(e.getSimpleName().toString()))
            .findFirst()
            .orElseThrow();

        parent = processingEnv.getElementUtils().getTypeElement("javafx.scene.Parent").asType();
    }

    public void setFxmlFile(Element element, FileObject fxmlFile) {
        fxmlFiles.put(element, fxmlFile);
    }

    public void generateSidecar(TypeElement componentClass) {
        try {
            final JavaFileObject builderFile = processingEnv.getFiler()
                .createSourceFile(componentClass.getQualifiedName() + CLASS_SUFFIX);
            try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
                this.generateSidecar(out, componentClass);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void generateSidecar(PrintWriter out, TypeElement componentClass) throws IOException {
        final String className = componentClass.getQualifiedName().toString();
        String packageName = null;
        int lastDot = className.lastIndexOf('.');
        if (lastDot > 0) {
            packageName = className.substring(0, lastDot);
        }

        String simpleClassName = className.substring(lastDot + 1);
        String builderClassName = className + CLASS_SUFFIX;
        String builderSimpleClassName = builderClassName.substring(lastDot + 1);

        if (packageName != null) {
            out.println("package " + packageName + ";");
            out.println();
        }

        out.println("import java.util.Map;");
        out.println("import java.util.ResourceBundle;");
        out.println("import javafx.scene.Node;");
        out.println("import org.fulib.fx.controller.ControllerManager;");
        out.println("import org.fulib.fx.controller.internal.FxSidecar;");
        out.println();

        final FileObject fxmlFile = fxmlFiles.get(componentClass);
        if (fxmlFile != null) {
            fxmlGenerator.generateFxmlImports(out, fxmlFile);
        }

        out.printf("public class %s implements FxSidecar<%s> {%n", builderSimpleClassName, simpleClassName);
        out.println("  private final ControllerManager controllerManager;");
        out.printf("  public %s(ControllerManager cm) {%n", builderSimpleClassName);
        out.println("    this.controllerManager = cm;");
        out.println("  }");
        out.println("  @Override");
        out.printf("  public void init(%s instance, Map<String, Object> params) {%n", simpleClassName);
        generateSidecarInit(out, componentClass);
        out.println("  }");
        out.println("  @Override");
        out.printf("  public Node render(%s instance, Map<String, Object> params) {%n", simpleClassName);
        generateSidecarRender(out, componentClass);
        out.println("  }");
        if (fxmlFile != null) {
            out.println("  @Override");
            out.printf("  public Node renderFxml(%s controller, Node root) {%n", simpleClassName);
            fxmlGenerator.generateRenderFxml(out, fxmlFile);
            out.println("  }");
        }
        out.println("  @Override");
        out.printf("  public void destroy(%s instance) {%n", simpleClassName);
        generateSidecarDestroy(out, componentClass);
        out.println("  }");
        out.println("  @Override");
        out.printf("  public ResourceBundle getResources(%s instance) {%n", simpleClassName);
        generateSidecarResources(out, componentClass);
        out.println("  }");
        out.println("  @Override");
        out.printf("  public String getTitle(%s instance) {%n", simpleClassName);
        generateSidecarTitle(out, componentClass);
        out.println("  }");
        out.println("}");
    }

    private void generateSidecarInit(PrintWriter out, TypeElement componentClass) {
        generateParametersIntoFields(out, componentClass);

        generateCallParamMethods(out, componentClass, Param.class);
        generateCallParamMethods(out, componentClass, Params.class);
        generateCallParamMethods(out, componentClass, ParamsMap.class);

        generateCallInitMethods(out, componentClass);

        generateCallSubComponents(out, componentClass, "init");
    }

    private void generateParametersIntoFields(PrintWriter out, TypeElement componentClass) {
        for (Element element : componentClass.getEnclosedElements()) {
            if (!(element instanceof VariableElement varElement)) {
                continue;
            }

            final Param param = element.getAnnotation(Param.class);
            if (param != null) {
                String fieldName = varElement.getSimpleName().toString();
                String fieldType = varElement.asType().toString();
                String paramNameLiteral = stringLiteral(param.value());
                out.printf("    if (params.containsKey(%s)) {%n", paramNameLiteral);

                // TODO field must be public, package-private or protected -- add a diagnostic if it's private
                if (processingEnv.getTypeUtils().isAssignable(varElement.asType(), writableValue)) {
                    // We use the `setValue` method to infer the actual type of the field,
                    // E.g. if the field is a `StringProperty` which extends `WritableValue<String>`,
                    // we can infer that the actual type is `String`.
                    final ExecutableType asMemberOf = (ExecutableType) processingEnv.getTypeUtils().asMemberOf((DeclaredType) varElement.asType(), genericSetValue);
                    final TypeMirror typeArg = asMemberOf.getParameterTypes().get(0);
                    final String writableType = typeArg.toString();
                    if (varElement.getModifiers().contains(Modifier.FINAL)) {
                        out.printf("      instance.%s.setValue((%s) params.get(%s));%n", fieldName, writableType, paramNameLiteral);
                    } else {
                        // final Object param = params.get(<paramNameLiteral>);
                        // if (param instanceof <writableValue>) {
                        //   instance.<fieldName> = (<fieldType>) param);
                        // } else {
                        //   instance.<fieldName>.setValue((<writableType>) param);
                        // }
                        out.printf("      final Object param = params.get(%s);%n", paramNameLiteral);
                        out.printf("      if (param instanceof %s) {%n", writableValue);
                        out.printf("        instance.%s = (%s) param;%n", fieldName, fieldType);
                        out.println("      } else {");
                        out.printf("        instance.%s.setValue((%s) param);%n", fieldName, writableType);
                        out.println("      }");
                    }
                } else {
                    out.printf("      instance.%s = (%s) params.get(%s);%n", fieldName, fieldType, paramNameLiteral);
                }
                out.println("    }");
            }

            final ParamsMap paramsMap = element.getAnnotation(ParamsMap.class);
            if (paramsMap != null) {
                String fieldName = varElement.getSimpleName().toString();
                if (varElement.getModifiers().contains(Modifier.FINAL)) {
                    out.printf("    instance.%s.clear();%n", fieldName);
                    out.printf("    instance.%s.putAll(params);%n", fieldName);
                } else {
                    out.printf("    instance.%s = params;%n", fieldName);
                }
            }
        }
    }

    private void generateCallInitMethods(PrintWriter out, TypeElement componentClass) {
        streamAllMethods(componentClass, onInit.class)
            .sorted(Comparator.comparingInt(a -> a.getAnnotation(onInit.class).value()))
            .forEach(methodElement -> generateCall(out, methodElement));
    }

    private void generateCallParamMethods(PrintWriter out, TypeElement componentClass, Class<? extends Annotation> annotation) {
        streamAllMethods(componentClass, annotation)
            .forEach(methodElement -> generateCall(out, methodElement));
    }

    private void generateCall(PrintWriter out, ExecutableElement methodElement) {
        final List<? extends VariableElement> parameters = methodElement.getParameters();
        final List<String> arguments = Arrays.asList(new String[parameters.size()]);

        fillMethodArguments(methodElement, arguments, parameters);

        for (int i = 0; i < parameters.size(); i++) {
            final VariableElement parameter = parameters.get(i);
            final Param paramAnnotation = parameter.getAnnotation(Param.class);
            if (paramAnnotation != null) {
                arguments.set(i, "(%s) params.get(%s)".formatted(parameter.asType(), stringLiteral(paramAnnotation.value())));
                continue;
            }

            final ParamsMap paramsMapAnnotation = parameter.getAnnotation(ParamsMap.class);
            if (paramsMapAnnotation != null) {
                arguments.set(i, "params");
                continue;
            }
        }

        out.printf("    instance.%s(%s);%n", methodElement.getSimpleName(), String.join(", ", arguments));
    }

    private void fillMethodArguments(ExecutableElement methodElement, List<String> arguments, List<? extends VariableElement> parameters) {
        final Param paramAnnotation = methodElement.getAnnotation(Param.class);
        if (paramAnnotation != null) {
            arguments.set(0, "(%s) params.get(%s)".formatted(parameters.get(0).asType(), stringLiteral(paramAnnotation.value())));
        }

        final Params paramsAnnotation = methodElement.getAnnotation(Params.class);
        if (paramsAnnotation != null) {
            final String[] paramNames = paramsAnnotation.value();
            for (int i = 0; i < paramNames.length; i++) {
                final VariableElement parameter = parameters.get(i);
                arguments.set(i, "(%s) params.get(%s)".formatted(parameter.asType(), stringLiteral(paramNames[i])));
            }
        }

        final ParamsMap paramsMapAnnotation = methodElement.getAnnotation(ParamsMap.class);
        if (paramsMapAnnotation != null) {
            arguments.set(0, "params");
        }
    }

    private void generateCallSubComponents(PrintWriter out, TypeElement componentClass, String method) {
        for (final Element element : componentClass.getEnclosedElements()) {
            if (!(element instanceof VariableElement varElement)) {
                continue;
            }

            final SubComponent subComponent = element.getAnnotation(SubComponent.class);
            if (subComponent == null) {
                continue;
            }

            if (varElement.asType().toString().startsWith("javax.inject.Provider")) {
                // Provider fields are initialized on demand
                continue;
            }

            final String fieldName = varElement.getSimpleName().toString();
            out.printf("    this.controllerManager.%s(instance.%s, params);%n", method, fieldName);
        }
    }

    private void generateSidecarDestroy(PrintWriter out, TypeElement componentClass) {
        generateDestroySubComponents(out, componentClass);
        generateCallDestroyMethods(out, componentClass);
    }

    private void generateCallDestroyMethods(PrintWriter out, TypeElement componentClass) {
        streamAllMethods(componentClass, onDestroy.class)
            .sorted(Comparator.comparingInt(a -> a.getAnnotation(onDestroy.class).value()))
            .forEach(element -> generateCall(out, element));
    }

    private void generateDestroySubComponents(PrintWriter out, TypeElement componentClass) {
        final List<String> fieldNames = new ArrayList<>();
        for (final Element element : componentClass.getEnclosedElements()) {
            if (!(element instanceof VariableElement varElement)) {
                continue;
            }

            final SubComponent subComponent = element.getAnnotation(SubComponent.class);
            if (subComponent == null) {
                continue;
            }

            if (varElement.asType().toString().startsWith("javax.inject.Provider")) {
                // Provider fields are destroyed on demand
                continue;
            }

            final String fieldName = varElement.getSimpleName().toString();
            fieldNames.add(fieldName);
        }

        Collections.reverse(fieldNames);
        for (String fieldName : fieldNames) {
            out.printf("    this.controllerManager.destroy(instance.%s);%n", fieldName);
        }
    }

    private void generateSidecarRender(PrintWriter out, TypeElement componentClass) {
        generateCallSubComponents(out, componentClass, "render");
        generateRenderResult(out, componentClass);
        generateCallRenderMethods(out, componentClass);
        // TODO Register key events
        out.println("    return result;");
    }

    private void generateRenderResult(PrintWriter out, TypeElement componentClass) {
        final Component component = componentClass.getAnnotation(Component.class);
        final Controller controller = componentClass.getAnnotation(Controller.class);

        if (component != null) {
            final String view = component.view();
            if (view.isEmpty()) {
                out.println("    final Node result = instance;");
            } else {
                if (processingEnv.getTypeUtils().isAssignable(componentClass.asType(), parent)) {
                    out.println("    instance.getChildren().clear();");
                }
                out.printf("    final Node result = this.controllerManager.loadFXML(%s, instance, true);%n", stringLiteral(view));
            }
        } else if (controller != null) {
            final String view = controller.view();
            if (view.startsWith("#")) {
                out.printf("    final Node result = instance.%s();%n", view.substring(1));
            } else {
                final String inferredView = view.isEmpty() ? ControllerUtil.transform(componentClass.getSimpleName().toString()) + ".fxml" : view;
                out.printf("    final Node result = this.controllerManager.loadFXML(%s, instance, false);%n", stringLiteral(inferredView));
            }
        }
    }

    private void generateCallRenderMethods(PrintWriter out, TypeElement componentClass) {
        streamAllMethods(componentClass, onRender.class)
            .sorted(Comparator.comparingInt(a -> a.getAnnotation(onRender.class).value()))
            .forEach(element -> generateCall(out, element));
    }

    private void generateSidecarResources(PrintWriter out, TypeElement componentClass) {
        for (Element enclosedElement : componentClass.getEnclosedElements()) {
            if (!(enclosedElement instanceof VariableElement variableElement)) {
                continue;
            }

            final Resource resource = variableElement.getAnnotation(Resource.class);
            if (resource == null) {
                continue;
            }

            final String fieldName = variableElement.getSimpleName().toString();
            out.printf("    return instance.%s;%n", fieldName);
            return;
        }

        out.println("    return controllerManager.getDefaultResourceBundle();");
    }

    private void generateSidecarTitle(PrintWriter out, TypeElement componentClass) {
        final Title title = componentClass.getAnnotation(Title.class);
        if (title == null) {
            out.println("    return null;");
            return;
        }

        if (title.value().startsWith("%")) {
            out.printf("    return getResources(instance).getString(%s);%n", stringLiteral(title.value().substring(1)));
        } else if ("$name".equals(title.value())) {
            out.printf("    return %s;%n", stringLiteral(ControllerUtil.transform(componentClass.getSimpleName().toString())));
        } else {
            out.printf("    return %s;%n", stringLiteral(title.value()));
        }
    }

    private String stringLiteral(String value) {
        return processingEnv.getElementUtils().getConstantExpression(value);
    }

    private Stream<ExecutableElement> streamAllMethods(TypeElement componentClass, Class<? extends Annotation> annotation) {
        return Stream.iterate(componentClass, Objects::nonNull, e -> (TypeElement) processingEnv.getTypeUtils().asElement(e.getSuperclass()))
            .flatMap(e -> streamMethods(e, annotation));
    }

    private Stream<ExecutableElement> streamMethods(TypeElement componentClass, Class<? extends Annotation> annotation) {
        return componentClass
            .getEnclosedElements()
            .stream()
            .filter(element -> element.getAnnotation(annotation) != null && element instanceof ExecutableElement)
            .map(element -> (ExecutableElement) element);
    }
}
