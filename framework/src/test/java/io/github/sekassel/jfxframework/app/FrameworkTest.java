package io.github.sekassel.jfxframework.app;

import io.github.sekassel.jfxframework.FxFramework;
import io.github.sekassel.jfxframework.app.controller.sub.ButtonSubComponent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

public class FrameworkTest extends ApplicationTest {

    public final FxFramework app = new FxFramework() {

        final TestComponent component = DaggerTestComponent.builder().mainApp(this).build();

        @Override
        public void start(Stage stage) {
            super.start(stage);
            router().registerRoutes(component.routes());
        }
    };

    @Override
    public void start(Stage stage) throws Exception {
        super.start(stage);
        app.start(stage);
        stage.requestFocus();
    }

    /**
     * Tests if the framework is able to load the routes and different kinds of controllers.
     */
    @Test
    public void controllers() {
        app.show("/controller/basic");
        verifyThat("Basic Controller", Node::isVisible);
        sleep(1000);

        app.show("/controller/method");
        verifyThat("Method Controller", Node::isVisible);
        sleep(1000);

        app.show("/controller/view");
        verifyThat("View Controller", Node::isVisible);
        sleep(1000);

        app.show("/component/basic");
        verifyThat("Basic Component", Node::isVisible);
        sleep(1000);

        app.show("/component/root");
        verifyThat("Root Component", Node::isVisible);
        sleep(1000);
    }

    /**
     * Tests if the framework can load a controller with a subcomponent and if the subcomponent is correctly injected.
     */
    @Test
    public void subComponent() {
        app.show("/controller/withsubcomponent");
        sleep(1000);
        ButtonSubComponent button = lookup("#buttonSubComponent").query();
        verifyThat(button, Node::isVisible);
        assertEquals(100, button.getMinWidth());
    }

    /**
     * Tests if the framework can load a controller with a for loop and if the loop is correctly executed.
     */
    @Test
    public void simpleForTest() {
        ObservableList<String> list = FXCollections.observableList(new ArrayList<>());
        FxFramework.scheduler().scheduleDirect(() -> {
            list.add("Hello");
            list.add("World");
            list.add("!");
        });

        app.show("/controller/for", Map.of("list", list));
        verifyThat("#container", Node::isVisible);

        VBox container = lookup("#container").queryAs(VBox.class);
        assertEquals(3, container.getChildren().size());

        FxFramework.scheduler().scheduleDirect(() -> list.remove("World"));
        waitForFxEvents();

        assertEquals(2, container.getChildren().size());

        FxFramework.scheduler().scheduleDirect(() -> list.add(1, "World"));
        waitForFxEvents();

        assertEquals(3, container.getChildren().size());
    }

}
