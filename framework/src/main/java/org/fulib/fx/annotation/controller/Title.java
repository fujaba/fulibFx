package org.fulib.fx.annotation.controller;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Annotation used to give a title to a controller/component.
 * <p>
 * The title is used to set the title of the window when the controller is displayed using {@link org.fulib.fx.FulibFxApp#show(String, Map)}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Title {

    /**
     * The title of the controller.
     * <p>
     * Can be a simple string or a key if a resource bundle is provided
     * (see {@link Resource} and {@link org.fulib.fx.FulibFxApp#setDefaultResourceBundle(ResourceBundle)}).
     *
     * @return The title of the controller.
     */
    String value() default "$name";

}
