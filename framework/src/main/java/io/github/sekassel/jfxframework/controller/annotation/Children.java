package io.github.sekassel.jfxframework.controller.annotation;

/**
 * Annotation used to define child routes inside an application.
 */
public @interface Children {

    /**
     * The child route at which the controller should be registered.
     * <p>
     * If the main route is "/mainmenu" and the child route is "back" the full route will be "/mainmenu/back".
     *
     * @return The child route at which the controller should be registered.
     */
    String route();

    /**
     * The name of the field containing the controller which should be registered at the child route.
     *
     * @return The name of the field containing the controller which should be registered at the child route.
     */
    String controller();
}
