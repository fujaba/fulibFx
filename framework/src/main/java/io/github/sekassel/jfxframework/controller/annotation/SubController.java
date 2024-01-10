package io.github.sekassel.jfxframework.controller.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SubController annotation.
 * <p>
 * Fields annotated with this annotation will be initialized and rendered.
 * The instance still needs to be provided by the user oder using dependency injection.
 * <p>
 * The instance will be used if a subcontroller of the same type is needed.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SubController {

    /**
     * The id of the subcontroller which is used to identify the instance.
     * If no id is provided, any subcontroller of the same type can use this instance.
     *
     * @return The id of the subcontroller
     */
    String value() default "";

}
