package io.github.sekassel.jfxframework.controller.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fields annotated with this annotation will be used as a controller instance for elements of the provided classes in an FXML file.
 * <p>
 * If an element in an FXML file is of a class annotated with @Controller and a field providing an instance of the same class exists, the provided instance will be used as the controller for the element.
 * <p>
 * There should only be one field annotated with @Providing for each class annotated with @Controller.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Providing {
}
