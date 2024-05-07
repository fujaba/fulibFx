package org.fulib.fx;

import javafx.scene.input.KeyCode;
import org.fulib.fx.annotation.controller.*;
import org.fulib.fx.annotation.event.OnDestroy;
import org.fulib.fx.annotation.event.OnInit;
import org.fulib.fx.annotation.event.OnKey;
import org.fulib.fx.annotation.event.OnRender;
import org.fulib.fx.annotation.param.Param;
import org.fulib.fx.annotation.param.Params;
import org.fulib.fx.annotation.param.ParamsMap;
import org.fulib.fx.util.ControllerUtil;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

import static org.fulib.fx.util.FrameworkUtil.error;

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

    public FxClassGenerator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;

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
        out.println("import org.fulib.fx.annotation.event.OnKey;");
        out.println("import org.fulib.fx.controller.ControllerManager;");
        out.println("import org.fulib.fx.controller.internal.FxSidecar;");
        out.println();

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
        streamAllFields(componentClass, Param.class).forEach(field -> {
            final Param param = field.getAnnotation(Param.class);
            final String fieldName = field.getSimpleName().toString();
            final String fieldType = field.asType().toString();
            final String paramNameLiteral = stringLiteral(param.value());
            out.printf("    if (params.containsKey(%s)) {%n", paramNameLiteral);

            // TODO field must be public, package-private or protected -- add a diagnostic if it's private
            if (processingEnv.getTypeUtils().isAssignable(field.asType(), writableValue)) {
                // We use the `setValue` method to infer the actual type of the field,
                // E.g. if the field is a `StringProperty` which extends `WritableValue<String>`,
                // we can infer that the actual type is `String`.
                final ExecutableType asMemberOf = (ExecutableType) processingEnv.getTypeUtils().asMemberOf((DeclaredType) field.asType(), genericSetValue);
                final TypeMirror typeArg = asMemberOf.getParameterTypes().get(0);
                final String writableType = typeArg.toString();
                if (field.getModifiers().contains(Modifier.FINAL)) {
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
        });

        streamAllFields(componentClass, ParamsMap.class).forEach(field -> {
            final String fieldName = field.getSimpleName().toString();
            if (field.getModifiers().contains(Modifier.FINAL)) {
                out.printf("    instance.%s.clear();%n", fieldName);
                out.printf("    instance.%s.putAll(params);%n", fieldName);
            } else {
                out.printf("    instance.%s = params;%n", fieldName);
            }
        });
    }

    private void generateCallInitMethods(PrintWriter out, TypeElement componentClass) {
        streamAllMethods(componentClass, OnInit.class)
            .sorted(Comparator.comparingInt(a -> a.getAnnotation(OnInit.class).value()))
            .peek(methodElement -> checkOverrides(methodElement, OnInit.class))
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
        streamAllFields(componentClass, SubComponent.class).forEach(field -> {
            if (field.asType().toString().startsWith("javax.inject.Provider")) {
                // Provider fields are initialized on demand
                return;
            }

            final String fieldName = field.getSimpleName().toString();
            out.printf("    this.controllerManager.%s(instance.%s, params);%n", method, fieldName);
        });
    }

    private void generateSidecarDestroy(PrintWriter out, TypeElement componentClass) {
        generateDestroySubComponents(out, componentClass);
        generateCallDestroyMethods(out, componentClass);
    }

    private void generateCallDestroyMethods(PrintWriter out, TypeElement componentClass) {
        streamAllMethods(componentClass, OnDestroy.class)
            .peek(methodElement -> checkOverrides(methodElement, OnDestroy.class))
            .sorted(Comparator.comparingInt(a -> a.getAnnotation(OnDestroy.class).value()))
            .forEach(element -> generateCall(out, element));
    }

    private void generateDestroySubComponents(PrintWriter out, TypeElement componentClass) {
        final List<String> fieldNames = new ArrayList<>();
        streamAllFields(componentClass, SubComponent.class).forEach(field -> {
            if (field.asType().toString().startsWith("javax.inject.Provider")) {
                // Provider fields are destroyed on demand
                return;
            }

            final String fieldName = field.getSimpleName().toString();
            fieldNames.add(fieldName);
        });

        Collections.reverse(fieldNames);
        for (String fieldName : fieldNames) {
            out.printf("    this.controllerManager.destroy(instance.%s);%n", fieldName);
        }
    }

    private void generateSidecarRender(PrintWriter out, TypeElement componentClass) {
        generateCallSubComponents(out, componentClass, "render");
        generateRenderResult(out, componentClass);
        generateCallRenderMethods(out, componentClass);
        generateRegisterKeyEventHandlers(out, componentClass);
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
        streamAllMethods(componentClass, OnRender.class)
            .peek(methodElement -> checkOverrides(methodElement, OnRender.class))
            .sorted(Comparator.comparingInt(a -> a.getAnnotation(OnRender.class).value()))
            .forEach(element -> generateCall(out, element));
    }

    private void generateRegisterKeyEventHandlers(PrintWriter out, TypeElement componentClass) {
        streamAllMethods(componentClass, OnKey.class).forEach(method -> {
            generateRegisterKeyEventHandler(out, componentClass, method);
        });
    }

    private void generateRegisterKeyEventHandler(PrintWriter out, TypeElement componentClass, ExecutableElement method) {
        // @OnKey(
        //     code = KeyCode.R,
        //     control = true,
        //     shift = false,
        //     alt = false,
        //     meta = false,
        //     character = "r",
        //     text = "",
        //     target = onKey.Target.STAGE,
        //     type = onKey.Type.RELEASED
        // )
        // void foo()
        // --- becomes: ---
        // controllerManager.addKeyEventHandler(instance, target, type, event -> {
        //     if (!event.isControlDown()) return;
        //     if (event.getCode() != KeyCode.R) return;
        //     instance.foo();
        // });
        OnKey onKey = method.getAnnotation(OnKey.class);
        out.printf("    controllerManager.addKeyEventHandler(instance, OnKey.Target.%s, OnKey.Type.%s.asEventType(), event -> {%n", onKey.target(), onKey.type());
        if (onKey.control()) {
            out.printf("      if (!event.isControlDown()) return;%n");
        }
        if (onKey.shift()) {
            out.printf("      if (!event.isShiftDown()) return;%n");
        }
        if (onKey.alt()) {
            out.printf("      if (!event.isAltDown()) return;%n");
        }
        if (onKey.meta()) {
            out.printf("      if (!event.isMetaDown()) return;%n");
        }
        if (onKey.code() != KeyCode.UNDEFINED) {
            out.printf("      if (event.getCode() != javafx.scene.input.KeyCode.%s) return;%n", onKey.code());
        }
        if (!onKey.text().isEmpty()) {
            out.printf("      if (!%s.equals(event.getText())) return;%n", stringLiteral(onKey.text()));
        }
        if (!"\0".equals(onKey.character())) {
            out.printf("      if (!%s.equals(event.getCharacter())) return;%n", stringLiteral(onKey.character()));
        }
        out.printf("      instance.%s(%s);%n", method.getSimpleName(), method.getParameters().size() == 1 ? "event" : "");
        out.printf("    });%n");
    }

    private void generateSidecarResources(PrintWriter out, TypeElement componentClass) {
        streamAllFields(componentClass, Resource.class)
            .findFirst()
            .ifPresentOrElse(
                field -> out.printf("    return instance.%s;%n", field.getSimpleName()),
                () -> out.println("    return controllerManager.getDefaultResourceBundle();")
            );
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

    // This will throw an error if the methods are private
    private Stream<ExecutableElement> streamAllMethods(TypeElement componentClass, Class<? extends Annotation> annotation) {
        return streamSuperClasses(componentClass).flatMap(e -> streamMethods(e, annotation)).peek(method -> {
            if (method.getModifiers().contains(Modifier.PRIVATE)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(1012).formatted(Method.class.getSimpleName(), method.getSimpleName(), componentClass.getQualifiedName(), annotation.getSimpleName(), method));
            }
        });
    }

    private Stream<ExecutableElement> streamMethods(TypeElement componentClass, Class<? extends Annotation> annotation) {
        return componentClass
            .getEnclosedElements()
            .stream()
            .filter(element -> element instanceof ExecutableElement && element.getAnnotation(annotation) != null)
            .map(element -> (ExecutableElement) element);
    }

    // This will throw an error if the fields are private
    private Stream<VariableElement> streamAllFields(TypeElement componentClass, Class<? extends Annotation> annotation) {
        return streamSuperClasses(componentClass).flatMap(e -> streamFields(e, annotation)).peek(field -> {
            if (field.getModifiers().contains(Modifier.PRIVATE)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(1012).formatted(Field.class.getSimpleName(), field.getSimpleName(), componentClass.getQualifiedName(), annotation.getSimpleName(), field));
            }
        });
    }

    private Stream<VariableElement> streamFields(TypeElement componentClass, Class<? extends Annotation> annotation) {
        return componentClass
            .getEnclosedElements()
            .stream()
            .filter(element -> element instanceof VariableElement && element.getAnnotation(annotation) != null)
            .map(element -> (VariableElement) element);
    }

    private Stream<TypeElement> streamSuperClasses(TypeElement componentClass) {
        return Stream.iterate(componentClass, Objects::nonNull, e -> (TypeElement) processingEnv.getTypeUtils().asElement(e.getSuperclass()));
    }

    /**
     * Checks if the given method overrides another event method.
     *
     * @param method The method to check
     * @param annotation The annotation the method is annotated with
     */
    private void checkOverrides(ExecutableElement method, Class<? extends Annotation> annotation) {
        TypeMirror clazz = method.getEnclosingElement().asType();
        TypeElement element = (TypeElement) processingEnv.getTypeUtils().asElement(clazz);
        TypeMirror parentClazz = element.getSuperclass();

        // If no parent class is found, the method cannot override anything
        if (parentClazz.getKind() == TypeKind.NONE) {
            return;
        }

        TypeElement parentElement = (TypeElement) processingEnv.getTypeUtils().asElement(parentClazz);

        streamSuperClasses(parentElement).forEach(superClass ->
            streamAllMethods(superClass, annotation)
                .filter(otherMethod -> otherMethod.getSimpleName().equals(method.getSimpleName()))
                .filter(otherMethod -> processingEnv.getTypeUtils().isSubsignature((ExecutableType) method.asType(), (ExecutableType) otherMethod.asType()))
                .filter(otherMethod -> { // Check if the overridden method is an event method
                    for (Class<? extends Annotation> eventAnnotation : ControllerUtil.EVENT_ANNOTATIONS) {
                        if (otherMethod.getAnnotation(eventAnnotation) != null) {
                            return true;
                        }
                    }
                    return false;
                })
                .findFirst()
                .ifPresent(overriddenMethod -> processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(1013).formatted(method, annotation.getSimpleName(), element.getQualifiedName(), superClass.getQualifiedName()), method))
        );
    }
}
