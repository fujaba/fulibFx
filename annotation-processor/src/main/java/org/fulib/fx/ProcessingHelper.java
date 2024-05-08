package org.fulib.fx;

import org.fulib.fx.util.ControllerUtil;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.stream.Stream;

import static org.fulib.fx.util.FrameworkUtil.error;

/**
 * Helper class for processing annotations and elements.
 */
public class ProcessingHelper {

    private final ProcessingEnvironment processingEnv;

    public ProcessingHelper(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    /**
     * Checks if a method is an event method (annotated with {@link ControllerUtil#EVENT_ANNOTATIONS}).
     *
     * @param method The method to check
     * @return True if the method is an event method
     */
    boolean isEventMethod(ExecutableElement method) {
        for (Class<? extends Annotation> eventAnnotation : ControllerUtil.EVENT_ANNOTATIONS) {
            if (method.getAnnotation(eventAnnotation) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a stream of all methods of the given class.
     *
     * @param clazz The class to get the methods from
     * @return A stream of all methods of the given class
     */
    Stream<ExecutableElement> streamMethods(TypeElement clazz) {
        return clazz
            .getEnclosedElements()
            .stream()
            .filter(element -> element instanceof ExecutableElement)
            .map(element -> (ExecutableElement) element);
    }

    /**
     * Returns a stream of all methods of the given class and its superclasses.
     *
     * @param clazz The class to get the methods from
     * @return A stream of all methods of the given class and its superclasses
     */
    Stream<ExecutableElement> streamAllMethods(TypeElement clazz) {
        return streamSuperClasses(clazz).flatMap(this::streamMethods);
    }

    /**
     * Returns a stream of all superclasses of the given class, including the class itself.
     *
     * @param clazz The class to get the superclasses from
     * @return A stream of all superclasses of the given class, including the class itself
     */
    Stream<TypeElement> streamSuperClasses(TypeElement clazz) {
        return Stream.iterate(clazz, Objects::nonNull, e -> (TypeElement) processingEnv.getTypeUtils().asElement(e.getSuperclass()));
    }

    /**
     * Returns a stream of all methods of the given class and its superclasses that are annotated with the given annotation.
     * This will throw an error if the methods are private.
     *
     * @param clazz      The class to get the methods from
     * @param annotation The annotation to filter the methods by
     * @return A stream of methods that are annotated with the given annotation and are not private
     */
    Stream<ExecutableElement> streamAllMethods(TypeElement clazz, Class<? extends Annotation> annotation) {
        return streamSuperClasses(clazz).flatMap(e -> streamMethods(e, annotation)).peek(method -> {
            if (method.getModifiers().contains(Modifier.PRIVATE)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(1012).formatted(Method.class.getSimpleName(), method.getSimpleName(), clazz.getQualifiedName(), annotation.getSimpleName(), method));
            }
        });
    }

    /**
     * Returns a stream of all methods of the given class that are annotated with the given annotation.
     *
     * @param clazz      The class to get the methods from
     * @param annotation The annotation to filter the methods by
     * @return A stream of methods that are annotated with the given annotation
     */
    Stream<ExecutableElement> streamMethods(TypeElement clazz, Class<? extends Annotation> annotation) {
        return clazz
            .getEnclosedElements()
            .stream()
            .filter(element -> element instanceof ExecutableElement && element.getAnnotation(annotation) != null)
            .map(element -> (ExecutableElement) element);
    }

    /**
     * Returns a stream of all fields of the given class and its superclasses that are annotated with the given annotation.
     * This will throw an error if the fields are private.
     *
     * @param clazz      The class to get the fields from
     * @param annotation The annotation to filter the fields by
     * @return A stream of fields that are annotated with the given annotation and are not private
     */
    Stream<VariableElement> streamAllFields(TypeElement clazz, Class<? extends Annotation> annotation) {
        return streamSuperClasses(clazz).flatMap(e -> streamFields(e, annotation)).peek(field -> {
            if (field.getModifiers().contains(Modifier.PRIVATE)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(1012).formatted(Field.class.getSimpleName(), field.getSimpleName(), clazz.getQualifiedName(), annotation.getSimpleName(), field));
            }
        });
    }

    /**
     * Returns a stream of all fields of the given class that are annotated with the given annotation.
     *
     * @param componentClass The class to get the fields from
     * @param annotation     The annotation to filter the fields by
     * @return A stream of fields that are annotated with the given annotation
     */
    Stream<VariableElement> streamFields(TypeElement componentClass, Class<? extends Annotation> annotation) {
        return componentClass
            .getEnclosedElements()
            .stream()
            .filter(element -> element instanceof VariableElement && element.getAnnotation(annotation) != null)
            .map(element -> (VariableElement) element);
    }

    /**
     * Returns the string literal of the given value.
     *
     * @param value The value to get the string literal of
     * @return The string literal of the given value
     */
    String stringLiteral(String value) {
        return processingEnv.getElementUtils().getConstantExpression(value);
    }

    /**
     * Checks if the given type mirror is a provider.
     *
     * @param typeMirror The type mirror to check
     * @return True if the type mirror is a provider
     */
    boolean isProvider(TypeMirror typeMirror) {
        return typeMirror.toString().startsWith("javax.inject.Provider");
    }
}
