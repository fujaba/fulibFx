package de.uniks.ludo;

import javafx.stage.Stage;
import org.mockito.Spy;
import org.testfx.framework.junit5.ApplicationTest;

public class ControllerTest extends ApplicationTest {

    @Spy
    protected App app = new App();

    protected Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        super.start(stage);
        this.stage = stage;
        stage.requestFocus();
        app.start(stage);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        app.stop();
        app = null;
        stage = null;
    }
}
