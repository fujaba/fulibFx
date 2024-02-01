package org.fulib.fx.annotation.controller;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for marking a controller which can be re-used in other controllers.
 * <p>
 * Classes annotated with @Component should extend from a Parent (e.g. Pane, HBox, VBox, ...).
 * When rendering the component, the class itself will be used as the view (e.g. the HBox itself).
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {

    /**
     * The view that should be rendered when displaying the component.
     * <p>
     * If a view is specified, the fxml file will be loaded and the component will be used as the root of the view.
     * Example: 'path/to/myView.fxml' will load the FXML file myView.fxml.
     *
     * @return The String specifying the view.
     */
    String view() default "";

}
