package io.github.sekassel.uno;

import io.github.sekassel.uno.controller.Controller;
import io.github.sekassel.uno.controller.SetupController;
import io.github.sekassel.uno.service.GameService;
import io.github.sekassel.uno.util.Utils;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private final GameService gameService;
    private Stage stage;
    private Controller controller;

    public App(GameService gameService) {
        this.gameService = gameService;
    }

    public App() {
        this(new GameService(Utils.getRandomBySeedFile()));
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        primaryStage.setScene(new Scene(new Label("Loading...")));
        primaryStage.setTitle("Uno");


        this.show(new SetupController(this));
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> controller.destroy());
    }


    public void show(Controller controller) {
        controller.init();
        try {
            stage.getScene().setRoot(controller.render());
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        if (this.controller != null) {
            this.controller.destroy();
        }
        this.controller = controller;
        stage.setTitle(controller.getTitle());
    }

    public Stage getStage() {
        return stage;
    }

    public GameService getGameService() {
        return gameService;
    }
}