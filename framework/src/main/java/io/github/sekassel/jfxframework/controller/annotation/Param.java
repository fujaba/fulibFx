package io.github.sekassel.jfxframework.controller.annotation;

import io.github.sekassel.jfxframework.controller.Router;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark controller parameters.
 * Used by the {@link Router} to inject parameters into controllers.
 */
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {

    /**
     * The name of the parameter.
     *
     * @return The name of the parameter.
     */
    String value();

}
