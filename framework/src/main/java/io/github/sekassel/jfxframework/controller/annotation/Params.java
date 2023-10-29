package io.github.sekassel.jfxframework.controller.annotation;

import io.github.sekassel.jfxframework.controller.Router;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark controller parameters in which the {@link Router} should inject the parameter map.
 * Used by the {@link Router} to inject parameters into controllers.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Params {
}
