package io.github.sekassel.jfxframework.controller.exception;

public class ControllerDuplicatedRouteException extends RuntimeException {

    public ControllerDuplicatedRouteException(String route, Class<?> oldController, Class<?> newController) {
        super("Controller %s tried to register with route %s but this route is already registered for %s.".formatted(route, newController.getName(), oldController.getName()));
    }
}
