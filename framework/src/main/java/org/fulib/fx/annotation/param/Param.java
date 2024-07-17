package org.fulib.fx.annotation.param;

import org.fulib.fx.annotation.event.OnInit;
import org.fulib.fx.annotation.event.OnRender;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;


/**
 * Fields, parameters and methods annotated with this annotation will be injected with a parameter provided when using the {@link org.fulib.fx.FulibFxApp#show(String, Map)} method.
 * <p>
 * If the annotation is used on a field, the field will be injected with the specified parameter's value before initializing the controller/component.
 * If the field is a writable property, the value will be set using the property's setter method (e.g. {@link javafx.beans.property.SimpleStringProperty}).
 * <p>
 * If the annotation is used on a method, the method will be called with the specified parameter's value initializing the controller/component.
 * <p>
 * If the annotation is used on a method argument, the argument will be injected with the specified parameter's value (method has to be annotated with {@link OnRender} or {@link OnInit}).
 */
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {

    /**
     * The name of the parameter.
     *
     * @return The name of the parameter.
     */
    String value();

    /**
     * The method of the field's class that will be called with the parameter's value.
     * Useful for {@code final} fields or {@link javafx.beans.property.Property} fields.
     * @return the method name
     */
    String method() default "";

    /**
     * When using {@link #method()}, this specifies the type of the first (and only) parameter of that method.
     * @return the type of the method parameter
     */
    Class<?> type() default Object.class;
}
