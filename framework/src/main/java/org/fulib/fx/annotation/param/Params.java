package org.fulib.fx.annotation.param;

import org.fulib.fx.annotation.event.onInit;
import org.fulib.fx.annotation.event.onRender;
import org.fulib.fx.controller.Router;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

/**
 * Fields, parameters and methods annotated with this annotation will be injected with the parameters provided when using the {@link org.fulib.fx.FulibFxApp#show(String, Map)} method.
 * <p>
 * If the annotation is used on a field, the field will be injected with a map containing all specified parameters before initializing the controller/component.
 * <p>
 * If the annotation is used on a method, the provided parameters will be injected into the method as arguments before initializing the controller/component.
 * <p>
 * If the annotation is used on a method argument, the argument will be injected with a map containing all specified parameters (method has to be annotated with {@link onRender} or {@link onInit}).
 * <p>
 * If no parameters are specified, all provided parameters will be used.
 */
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Params {

    /**
     * The names of the parameter which should be injected.
     *
     * @return The name of the parameter.
     */
    String[] value() default {};

}
