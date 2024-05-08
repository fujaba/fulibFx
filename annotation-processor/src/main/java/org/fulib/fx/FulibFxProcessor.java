package org.fulib.fx;

import com.google.auto.service.AutoService;
import org.fulib.fx.annotation.Route;
import org.fulib.fx.annotation.controller.*;
import org.fulib.fx.annotation.event.OnKey;
import org.fulib.fx.annotation.param.Params;
import org.fulib.fx.annotation.param.ParamsMap;
import org.fulib.fx.util.ControllerUtil;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static org.fulib.fx.util.FrameworkUtil.error;
import static org.fulib.fx.util.FrameworkUtil.note;

@SupportedAnnotationTypes({
    "org.fulib.fx.annotation.controller.*",
    "org.fulib.fx.annotation.Route",
    "org.fulib.fx.annotation.param.*",
})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class FulibFxProcessor extends AbstractProcessor {

    private FxClassGenerator generator;
    private ProcessingHelper helper;

    public FulibFxProcessor() {
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.helper = new ProcessingHelper(processingEnv);
        this.generator = new FxClassGenerator(helper, processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        // Check if the element is a valid component
        for (Element element : roundEnv.getElementsAnnotatedWith(Component.class)) {
            checkComponent(element);
            checkDoubleAnnotation(element); // Check if a class is annotated with both @Controller and @Component
            if (element instanceof TypeElement typeElement) {
                checkEventModifiers(typeElement);
                generator.generateSidecar(typeElement);
                checkOverrides(typeElement);
            }
        }

        // Check if the element is a valid controller
        for (Element element : roundEnv.getElementsAnnotatedWith(Controller.class)) {
            checkController(element);
            if (element instanceof TypeElement typeElement) {
                checkEventModifiers(typeElement);
                generator.generateSidecar(typeElement);
                checkOverrides(typeElement);
            }
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(SubComponent.class)) {
            checkSubComponentElement(element);
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(Route.class)) {
            checkRoute(element);
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(ParamsMap.class)) {
            checkParamsMap(element);
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(Params.class)) {
            checkParams(element);
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(Resource.class)) {
            checkResources(element);
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(Title.class)) {
            checkTitle(element);
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(OnKey.class)) {
            checkOnKey(element);
        }

        return true;
    }

    private void checkOverrides(TypeElement typeElement) {
        Map<String, List<ExecutableElement>> eventMethods = helper.streamAllMethods((TypeElement) processingEnv.getTypeUtils().asElement(typeElement.getSuperclass()))
            .filter(this::isEventElement)
            .collect(groupingBy(method -> method.getSimpleName().toString()));
        typeElement.getEnclosedElements().stream()
            .filter(e -> e.getKind() == ElementKind.METHOD)
            .map(e -> (ExecutableElement) e)
            .filter(this::isEventElement)
            .forEach(e -> checkOverrides(e, eventMethods));
    }

    private void checkOnKey(Element element) {
        if (element instanceof ExecutableElement method) {
            if (!method.getParameters().isEmpty() && !(method.getParameters().size() == 1 && processingEnv.getTypeUtils().isAssignable(method.getParameters().get(0).asType(), processingEnv.getElementUtils().getTypeElement("javafx.scene.input.KeyEvent").asType()))) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(1010).formatted(method.getSimpleName(), method.getEnclosingElement().asType().toString()), method);
            }
        }
    }

    private void checkResources(Element element) {
        final String elementType = element.asType().toString();
        if (!processingEnv.getTypeUtils().isSubtype(element.asType(), processingEnv.getElementUtils().getTypeElement("java.util.ResourceBundle").asType())) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(2004).formatted(element.getSimpleName(), element.getEnclosingElement().getSimpleName()), element);
        }
    }

    private void checkTitle(Element element) {
        if (!isComponent(element.asType()) && !isController(element.asType())) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(1009), element);
        }
    }

    private void checkParams(Element element) {
        if (element instanceof ExecutableElement method) {
            Params annotation = method.getAnnotation(Params.class);
            if (method.getParameters().size() != annotation.value().length) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(4006).formatted(method.getSimpleName(), method.getEnclosingElement().asType().toString()), method);
            }
        }
    }

    private void checkParamsMap(Element element) {
        if (element instanceof ExecutableElement method) {
            if (method.getParameters().size() != 1) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(4003).formatted(method.getSimpleName(), method.getEnclosingElement().asType().toString()), method);
            }
        }
    }

    private void checkRoute(Element element) {

        // Check if the field is of a provider type
        if (!element.asType().toString().startsWith("javax.inject.Provider")) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(3004).formatted(element.getSimpleName(), element.asType().toString()), element);
            return;
        }

        // Check if the provided class is of a controller or component type
        for (TypeMirror generic : ((DeclaredType) element.asType()).getTypeArguments()) {
            if (!isController(generic) && !isComponent(generic)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(3003).formatted(element.getSimpleName(), element.asType().toString()), element);
            }
        }

    }

    private void checkController(Element element) {
        final String view = element.getAnnotation(Controller.class).view();
        if (!view.isEmpty()) {
            if (view.startsWith("#")) {
                // Check if the view is referring to a valid method
                checkViewMethod(element, view);
            } else {
                // Check if the specified view file exists
                checkViewResource(element, view);
            }
        } else {
            // No view specified, so we transform the class name to a view file name
            checkViewResource(element, ControllerUtil.transform(element.getSimpleName().toString()) + ".fxml");
        }
    }

    private void checkComponent(Element element) {
        final String view = element.getAnnotation(Component.class).view();

        // Check if the specified view file exists
        if (!view.isEmpty()) {
            checkViewResource(element, view);
        }

        // Check if the element is a subclass of javafx.scene.Node
        if (!processingEnv.getTypeUtils().isAssignable(element.asType(), processingEnv.getElementUtils().getTypeElement("javafx.scene.Node").asType())) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(1006), element);
        }
    }

    private void checkDoubleAnnotation(Element element) {
        if (element.getAnnotation(Controller.class) != null && element.getAnnotation(Component.class) != null) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(1007), element);
        }
    }

    private void checkViewResource(Element element, String view) {
        String packageName = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();

        // relativize the view path -- while it starts with ../, remove the last package segment
        // this is necessary to avoid an IllegalArgumentException when calling getResource,
        // because it does not allow relative paths
        while (view.startsWith("../")) {
            int index = packageName.lastIndexOf('.');
            if (index == -1) {
                final String viewPath = packageName + "/" + view; // no replace needed, packageName has no more '.'s
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(2000).formatted(viewPath), element);
                return;
            }
            packageName = packageName.substring(0, index);
            view = view.substring(3);
        }

        try {
            // Check if the specified view file exists in the source path
            final FileObject resource = processingEnv.getFiler().getResource(StandardLocation.SOURCE_PATH, packageName, view);
        } catch (IOException e) {
            final String viewPath = packageName.replace('.', '/') + "/" + view;
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(2000).formatted(viewPath), element);
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, note(2000));
        }
    }

    private void checkViewMethod(Element element, String view) {
        String methodName = view.substring(1); // remove the leading '#'
        processingEnv.getElementUtils().getAllMembers((TypeElement) element).stream()
            .filter(elem -> elem.getKind() == ElementKind.METHOD)
            .filter(elem -> elem.getSimpleName().toString().equals(methodName))
            .map(elem -> (ExecutableElement) elem)
            .findFirst()
            .ifPresentOrElse(
                method -> {
                    if (!method.getParameters().isEmpty()) {
                        Name methodSimpleName = method.getSimpleName();
                        String className = method.getEnclosingElement().asType().toString();
                        String error = error(1008).formatted(methodSimpleName, className);
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error, method);
                    }

                    TypeMirror parent = processingEnv.getElementUtils().getTypeElement("javafx.scene.Parent").asType();

                    if (!processingEnv.getTypeUtils().isAssignable(method.getReturnType(), parent)) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(1002), method);
                    }
                },
                () -> processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(1003).formatted(methodName), element)
            );
    }

    private void checkSubComponentElement(Element element) {
        // Check if the field is of a component type
        if (isComponent(element.asType())) {
            return;
        }

        // Check if the field is of a provider type
        if (helper.isProvider(element.asType())) {
            // Check if the provided class is of a component type
            if (element.asType() instanceof DeclaredType type) {
                final TypeMirror componentType = type.getTypeArguments().get(0);
                if (isComponent(componentType)) {
                    return;
                }
            }
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(6005).formatted(element.getSimpleName(), element.getEnclosingElement().asType().toString()), element);
    }

    // The method expects the typeMirror to be a declared type
    private boolean isComponent(TypeMirror typeMirror) {
        DeclaredType declaredType = (DeclaredType) typeMirror;
        return declaredType.asElement().getAnnotation(Component.class) != null;
    }

    // The method expects the typeMirror to be a declared type
    private boolean isController(TypeMirror typeMirror) {
        DeclaredType declaredType = (DeclaredType) typeMirror;
        return declaredType.asElement().getAnnotation(Controller.class) != null;
    }

    /**
     * Checks if an element is an event element (annotated with {@link ControllerUtil#EVENT_ANNOTATIONS}).
     *
     * @param element The element to check
     * @return True if the element is an event element
     */
    private boolean isEventElement(Element element) {
        for (Class<? extends Annotation> eventAnnotation : ControllerUtil.EVENT_ANNOTATIONS) {
            if (element.getAnnotation(eventAnnotation) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given method overrides another event method.
     *
     * @param method       The method to check
     * @param eventMethods A map of all event methods of the superclasses
     */
    private void checkOverrides(ExecutableElement method, Map<String, List<ExecutableElement>> eventMethods) {
        TypeElement element = (TypeElement) processingEnv.getTypeUtils().asElement(method.getEnclosingElement().asType());

        // If no parent class is found, the method cannot override anything
        if (element.getSuperclass().getKind() == TypeKind.NONE) {
            return;
        }

        String methodName = method.getSimpleName().toString();

        // Check if one of the parent classes has the method
        if (!eventMethods.containsKey(methodName)) {
            return;
        }

        for (ExecutableElement parentMethod : eventMethods.get(method.getSimpleName().toString())) {
            if (sameMethodSignature(method, parentMethod)) {
                Name className = element.getQualifiedName();
                Name parentClassName = ((TypeElement) parentMethod.getEnclosingElement()).getQualifiedName();
                String error = error(1013).formatted(method, className, parentClassName);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error, method);
                break;
            }
        }

    }

    private boolean sameMethodSignature(ExecutableElement method, ExecutableElement otherMethod) {
        ExecutableType methodType = (ExecutableType) method.asType();
        ExecutableType otherMethodType = (ExecutableType) otherMethod.asType();
        return method.getSimpleName().equals(otherMethod.getSimpleName()) &&
            method.getParameters().size() == otherMethod.getParameters().size() &&
            processingEnv.getTypeUtils().isSubsignature(methodType, otherMethodType);
    }

    private void checkEventModifiers(TypeElement clazz) {
        Stream.concat(helper.streamAllMethods(clazz), helper.streamAllFields(clazz))
            .filter(this::isEventElement)
            .filter(element -> element.getModifiers().contains(Modifier.PRIVATE))
            .forEach(element -> {
                String kind = element.getKind() == ElementKind.METHOD ? "method" : "field";
                Name name = element.getSimpleName();
                Name clazzName = clazz.getQualifiedName();
                String error = error(1012).formatted(kind, name, clazzName);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error, element);
            });
    }

}
