package org.fulib.fx.annotation.param;

import org.fulib.fx.annotation.event.onInit;
import org.fulib.fx.annotation.event.onRender;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

/**
 * Fields, parameters and methods annotated with this annotation will be injected with a map of all parameters when using the {@link org.fulib.fx.FulibFxApp#show(String, Map)} method.
 * <p>
 * If the annotation is used on a field, the field will be injected with the parameter map.
 * <p>
 * If the annotation is used on a method, the method will be called with the parameter map as an argument.
 * <p>
 * If the annotation is used on a method argument, the argument will be injected with the parameter map (method has to be annotated with {@link onRender} or {@link onInit}).
 */
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ParamsMap {

}
