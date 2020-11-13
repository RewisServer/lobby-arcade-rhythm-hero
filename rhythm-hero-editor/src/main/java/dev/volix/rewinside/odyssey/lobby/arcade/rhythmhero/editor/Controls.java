package dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.editor;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;

/**
 * @author Benedikt Wüller
 */
public class Controls extends HBox {

    public Controls(final Listener listener) {
        this.setPadding(new Insets(10.0, 10.0, 5.0, 10.0));
        this.setSpacing(10.0);

        final Button playButton = new Button("Play/Pause");
        playButton.setOnAction(actionEvent -> listener.onTogglePlay());

        final Slider slider = new Slider();
        slider.setMin(0.25);
        slider.setMax(1.0);
        slider.setValue(1.0);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.valueProperty().addListener((observable, oldValue, newValue) -> listener.onChangeRate((Double) newValue));

        final Button colorButton = new Button("Edit Note Colors");
        colorButton.setOnAction(actionEvent -> listener.onEditColors());

        this.getChildren().add(playButton);
        this.getChildren().add(slider);
        this.getChildren().add(colorButton);
    }

    public interface Listener {
        void onTogglePlay();
        void onChangeRate(double rate);
        void onEditColors();
    }

}
