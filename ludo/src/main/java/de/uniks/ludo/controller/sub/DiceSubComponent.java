package de.uniks.ludo.controller.sub;

import io.reactivex.rxjava3.core.Observable;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.annotation.event.onDestroy;
import org.fulib.fx.annotation.event.onRender;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

@Component(view = "Dice.fxml")
public class DiceSubComponent extends VBox {

    @FXML
    public Label eyesLabel;

    private final BooleanProperty enabled = new SimpleBooleanProperty(true);

    private CompletableFuture<Integer> rollFuture;

    Timeline rollAnimation;

    @Inject
    public DiceSubComponent() {
        super();
    }

    @onRender
    public void onRender() {
        this.rollAnimation = new Timeline();
        this.rollAnimation.setCycleCount(10);
        rollAnimation.getKeyFrames().add(new javafx.animation.KeyFrame(javafx.util.Duration.millis(100), event -> {
            int random = (int) (Math.random() * 6 + 1);
            eyesLabel.setText(String.valueOf(random));
        }));
        this.rollAnimation.setOnFinished(event -> this.rollFuture.complete(Integer.parseInt(eyesLabel.getText())));
    }

    @onDestroy
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
