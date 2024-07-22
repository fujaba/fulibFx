package org.fulib.fx.app;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.fulib.fx.FulibFxApp;
import org.fulib.fx.app.controller.InvalidParamController;
import org.fulib.fx.app.controller.ModalComponent;
import org.fulib.fx.app.controller.ParamController;
import org.fulib.fx.app.controller.TitleController;
import org.fulib.fx.app.controller.history.AController;
import org.fulib.fx.app.controller.history.BController;
import org.fulib.fx.app.controller.history.CController;
import org.fulib.fx.app.controller.subcomponent.basic.ButtonSubComponent;
import org.fulib.fx.app.controller.types.BasicComponent;
import org.fulib.fx.constructs.Modals;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.fulib.fx.FulibFxApp.FX_SCHEDULER;
import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

public class FrameworkTest extends ApplicationTest {

    public static void runAndWait(Runnable runnable) {
        Platform.runLater(runnable);
        waitForFxEvents();
    }

    public final FulibFxApp app = new FulibFxApp() {

        final TestComponent component = DaggerTestComponent.builder().mainApp(this).build();

        @Override
        public void start(Stage stage) {
            super.start(stage);
            registerRoutes(component.routes());
        }
    };

    @Override
    public void start(Stage stage) throws Exception {
        super.start(stage);
        app.start(stage);
        stage.requestFocus();
    }

    @Test
    public void title() {
        runAndWait(() -> app.show(new TitleController()));
        assertEquals("Title", app.stage().getTitle());
    }

    /**
     * Tests if the framework is able to load the routes and different kinds of controllers.
     */
    @Test
    public void controllerTypes() {
        runAndWait(() -> app.show("/controller/basic"));
        verifyThat("Basic Controller", Node::isVisible);
        sleep(200);

        runAndWait(() -> app.show("/controller/method"));
        verifyThat("Method Controller", Node::isVisible);
        sleep(200);

        runAndWait(() -> app.show("/controller/view"));
        verifyThat("View Controller", Node::isVisible);
        sleep(200);

        runAndWait(() -> app.show("/component/basic"));
        verifyThat("Basic Component", Node::isVisible);
        sleep(200);

        runAndWait(() -> app.show("/component/root"));
        verifyThat("Root Component", Node::isVisible);
        sleep(200);

        runAndWait(() -> app.show("../root"));
        verifyThat("Root Component", Node::isVisible);
        sleep(200);

        assertThrows(RuntimeException.class, () -> app.show("/controller/invalid"));
    }

    /**
     * Tests if the framework can load a controller with a subcomponent and if the subcomponent is correctly injected.
     */
    @Test
    public void subComponent() {
        runAndWait(() -> app.show("/controller/withsubcomponent"));
        sleep(200);
        ButtonSubComponent button = lookup("#buttonSubComponent").query();
        verifyThat(button, Node::isVisible);
        assertEquals(100, button.getMinWidth());
    }

    @Test
    public void routeSubComponent() {
        assertThrows(IllegalArgumentException.class, () -> app.initAndRender("/controller/view"));

        runAndWait(() -> assertEquals(BasicComponent.class, app.initAndRender("/component/basic").getClass()));
    }

    /**
     * Tests if the framework can load a controller with a for loop and if the loop is correctly executed.
     */
    @Test
    public void simpleForTest() {
        ObservableList<String> list = FXCollections.observableList(new ArrayList<>());
        FX_SCHEDULER.scheduleDirect(() -> {
            list.add("Hello");
            list.add("World");
            list.add("!");
        });

        runAndWait(() -> app.show("/controller/for", Map.of("list", list)));
        verifyThat("#container", Node::isVisible);

        VBox container = lookup("#container").queryAs(VBox.class);
        assertEquals(3, container.getChildren().size());

        FX_SCHEDULER.scheduleDirect(() -> list.remove("World"));
        waitForFxEvents();

        assertEquals(2, container.getChildren().size());

        FX_SCHEDULER.scheduleDirect(() -> list.add(1, "World"));
        waitForFxEvents();

        FX_SCHEDULER.scheduleDirect(() -> list.set(1, "Everyone"));
        waitForFxEvents();

        assertEquals(3, container.getChildren().size());

    }

    @Test
    public void testSubOrder() {
        List<String> initList = new ArrayList<>();
        List<String> renderList = new ArrayList<>();
        List<String> destroyList = new ArrayList<>();

        runAndWait(() -> app.show("/ordertest/main", Map.of("initList", initList, "renderList", renderList, "destroyList", destroyList)));

        assertEquals(List.of("main", "sub", "subsub", "othersubsub"), initList);
        assertEquals(List.of("subsub", "othersubsub", "sub", "main"), renderList);
        assertEquals(List.of(), destroyList);

        runAndWait(() -> app.show("/controller/basic"));

        assertEquals(List.of("othersubsub", "subsub", "sub", "main"), destroyList);
    }

    @Test
    public void modalTest() {
        runAndWait(() -> app.show("/controller/basic"));
        verifyThat("Basic Controller", Node::isVisible);
        sleep(200);

        ModalComponent component = new ModalComponent();

        FX_SCHEDULER.scheduleDirect(() -> new Modals(app)
            .modal(component)
            .init((stage, modalComponent) -> stage.setTitle("Modal"))
            .params(Map.of("key", "value"))
            .show()
        );

        waitForFxEvents();

        Stage modal = Modals.getModalStages().get(0);

        assertTrue(Modals.isModal(modal));
        assertFalse(Modals.isModal(app.stage()));

        assertNotNull(modal);
        assertEquals("value", component.getValue());
        verifyThat("Modal Component", Node::isVisible);
        assertEquals("Modal", modal.getTitle());

        runAndWait(modal::close);

        assertTrue(component.destroyed);

        ModalComponent component2 = new ModalComponent();

        FX_SCHEDULER.scheduleDirect(() -> {
            Stage built = new Modals(app)
                    .modal(component2)
                    .init((stage, modalComponent) -> stage.setTitle("Modal"))
                    .build();
            assertEquals("Modal", built.getTitle());
        });

        waitForFxEvents();

        FX_SCHEDULER.scheduleDirect(() -> assertThrows(RuntimeException.class, () -> new Modals(app).modal(component).show()));
    }

    @Test
    @Deprecated
    @SuppressWarnings("all")
    public void modalTestLegacy() {
        runAndWait(() -> app.show("/controller/basic"));
        verifyThat("Basic Controller", Node::isVisible);
        sleep(200);

        ModalComponent component = new ModalComponent();

        FX_SCHEDULER.scheduleDirect(() -> org.fulib.fx.controller.Modals.showModal(app, component, (stage, controller) -> {
            stage.setTitle("Modal");
            stage.setWidth(200);
            stage.setHeight(200);
        }, Map.of("key", "value"), true));

        waitForFxEvents();

        Stage modal = Modals.getModalStages().get(0);

        assertNotNull(modal);
        assertEquals("value", component.getValue());
        verifyThat("Modal Component", Node::isVisible);
        assertEquals("Modal", modal.getTitle());
    }

    @Test
    public void params() {
        ParamController controller = new ParamController();
        StringProperty property = new SimpleStringProperty("string");
        Map<String, Object> params = Map.of(
            "integer", 1,
            "string", "string",
            "character", 'a',
            "bool", true,
            "property", property
        );
        runAndWait(() -> app.show(controller, params));

        assertEquals(1, controller.getOnInitParam());
        assertEquals("string", controller.getSetterParam());
        assertEquals(1, controller.getFieldParam());

        assertEquals("string", controller.fieldPropertyParamProperty().get());

        assertEquals(params, controller.getOnInitParamsMap());
        assertEquals(params, controller.getSetterParamsMap());
        assertEquals(params, controller.getFieldParamsMap());
        assertEquals(params, controller.getFinalFieldParamsMap());

        assertEquals('a', controller.getSetterMultiParams1());
        assertEquals(true, controller.getSetterMultiParams2());

        assertEquals(property, controller.stringPropertyProperty());

        runAndWait(() ->
            assertThrows(
                RuntimeException.class, // Fails because the field is of type Integer, but a String is provided
                () -> app.show(new InvalidParamController(), Map.of("one", "string"))
            )
        );

        runAndWait(() ->
            assertThrows(
                RuntimeException.class, // Fails because the property expects an Integer, but a String is provided
                () -> app.show(new InvalidParamController(), Map.of("two", "123"))
            )
        );

        runAndWait(() ->
            assertThrows(
                RuntimeException.class, // Fails because the property is null
                () -> app.show(new InvalidParamController(), Map.of("three", 123))
            )
        );
    }

    @Test
    public void history() {
        runAndWait(() -> app.show(new AController(), Map.of("string", "a")));
        verifyThat("A:a", Node::isVisible);

        runAndWait(() -> app.show(new BController(), Map.of("string", "b")));
        verifyThat("B:b", Node::isVisible);

        runAndWait(() -> app.show(new CController(), Map.of("string", "c")));
        verifyThat("C:c", Node::isVisible);

        runAndWait(app::back);
        verifyThat("B:b", Node::isVisible);

        runAndWait(app::back);
        verifyThat("A:a", Node::isVisible);

        runAndWait(app::forward);
        verifyThat("B:b", Node::isVisible);

        runAndWait(app::forward);
        verifyThat("C:c", Node::isVisible);

        runAndWait(app::forward);
        verifyThat("C:c", Node::isVisible); // should not change

        runAndWait(app::back);
        verifyThat("B:b", Node::isVisible);

        FX_SCHEDULER.scheduleDirect(app::refresh);
        waitForFxEvents();
        verifyThat("B:b", Node::isVisible);
    }

}
