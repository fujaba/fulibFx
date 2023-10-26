package io.github.sekassel.jfxframework.controller.exception;

public class ControllerLoadingException extends RuntimeException {

    public ControllerLoadingException(String route, Exception e) {
        super("Controller at route %s couldn't be loaded: %s".formatted(route, e.getMessage()), e);
    }

}