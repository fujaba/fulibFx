package org.fulib.fx.mocking;

import javafx.stage.Stage;
import org.mockito.Spy;
import org.testfx.framework.junit5.ApplicationTest;

public class ControllerTest extends ApplicationTest {

    @Spy
    public final MyApp app = new MyApp();

    protected Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        super.start(stage);
        this.stage = stage;
        stage.requestFocus();
        app.start(stage);
    }
}
