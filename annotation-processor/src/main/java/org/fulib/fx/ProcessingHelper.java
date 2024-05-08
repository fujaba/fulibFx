package org.fulib.fx;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Helper class for processing annotations and elements.
 */
public class ProcessingHelper {

    private final ProcessingEnvironment processingEnv;

    public ProcessingHelper(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
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
     *
     * @param clazz      The class to get the methods from
     * @param annotation The annotation to filter the methods by
     * @return A stream of methods that are annotated with the given annotation
     */
    Stream<ExecutableElement> streamAllMethods(TypeElement clazz, Class<? extends Annotation> annotation) {
        return streamAllMethods(clazz).filter(element -> element.getAnnotation(annotation) != null);
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
     * Returns a stream of all methods of the given class that are annotated with the given annotation.
     *
     * @param clazz      The class to get the methods from
     * @param annotation The annotation to filter the methods by
     * @return A stream of methods that are annotated with the given annotation
     */
    Stream<ExecutableElement> streamMethods(TypeElement clazz, Class<? extends Annotation> annotation) {
        return streamMethods(clazz).filter(element -> element.getAnnotation(annotation) != null);
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
     * Returns a stream of all fields of the given class and its superclasses that are annotated with the given annotation.
     *
     * @param clazz      The class to get the fields from
     * @param annotation The annotation to filter the fields by
     * @return A stream of fields that are annotated with the given annotation
     */
    Stream<VariableElement> streamAllFields(TypeElement clazz, Class<? extends Annotation> annotation) {
        return streamAllFields(clazz).filter(element -> element.getAnnotation(annotation) != null);
    }

    /**
     * Returns a stream of all fields of the given class and its superclasses that are annotated with the given annotation.
     * This will throw an error if the fields are private.
     *
     * @param clazz The class to get the fields from
     * @return A stream of fields that are annotated with the given annotation
     */
    Stream<VariableElement> streamAllFields(TypeElement clazz) {
        return streamSuperClasses(clazz).flatMap(this::streamFields);
    }

    /**
     * Returns a stream of all fields of the given class that are annotated with the given annotation.
     *
     * @param clazz      The class to get the fields from
     * @param annotation The annotation to filter the fields by
     * @return A stream of fields that are annotated with the given annotation
     */
    Stream<VariableElement> streamFields(TypeElement clazz, Class<? extends Annotation> annotation) {
        return streamFields(clazz).filter(element -> element.getAnnotation(annotation) != null);
    }

    /**
     * Returns a stream of all fields of the given class that are annotated with the given annotation.
     *
     * @param clazz The class to get the fields from
     * @return A stream of fields that are annotated with the given annotation
     */
    Stream<VariableElement> streamFields(TypeElement clazz) {
        return clazz
            .getEnclosedElements()
            .stream()
            .filter(element -> element instanceof VariableElement)
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
