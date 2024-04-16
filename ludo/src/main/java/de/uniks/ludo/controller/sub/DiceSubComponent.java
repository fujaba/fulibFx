package de.uniks.ludo.controller.sub;

import de.uniks.ludo.service.GameService;
import io.reactivex.rxjava3.core.Observable;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.annotation.event.OnDestroy;
import org.fulib.fx.annotation.event.OnRender;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

@Component(view = "Dice.fxml")
public class DiceSubComponent extends VBox {

    @FXML
    public Label eyesLabel;

    @Inject
    public GameService gameService;

    private final BooleanProperty enabled = new SimpleBooleanProperty(true);

    private CompletableFuture<Integer> rollFuture;

    Timeline rollAnimation;

    @Inject
    public DiceSubComponent() {
        super();
    }

    @OnRender
    public void onRender() {
        this.rollAnimation = new Timeline();
        this.rollAnimation.setCycleCount(10);
        rollAnimation.getKeyFrames().add(new javafx.animation.KeyFrame(javafx.util.Duration.millis(100), event -> {
            int random = gameService.rollRandom();
            eyesLabel.setText(String.valueOf(random));
        }));
        this.rollAnimation.setOnFinished(event -> this.rollFuture.complete(Integer.parseInt(eyesLabel.getText())));
    }

    @OnDestroy
    public void stopRolling() {
        rollAnimation.stop();
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public BooleanProperty enabledProperty() {
        return enabled;
    }

    public Observable<Integer> roll() {
        this.rollFuture = new CompletableFuture<>();
        if (this.isEnabled()) {
            this.enabled.set(false);
            rollAnimation.play();
            return Observable.fromFuture(this.rollFuture);
        }
        return Observable.empty();
    }

    public void reset() {
        this.eyesLabel.setText("🎲");
        this.enabled.set(true);
    }

    public void setLabel(int i) {
        this.eyesLabel.setText(String.valueOf(i));
    }
}
