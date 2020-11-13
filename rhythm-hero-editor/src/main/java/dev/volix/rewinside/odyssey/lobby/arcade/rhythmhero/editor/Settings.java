package dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.editor;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

/**
 * @author Benedikt Wüller
 */
public class Settings extends HBox {

    public Settings(final String name, final float delay, final Listener listener) {
        this.setPadding(new Insets(0, 10.0, 10.0, 10.0));
        this.setSpacing(10.0);
        this.setAlignment(Pos.CENTER_LEFT);

        final TextField nameField = new TextField(name);
        this.getChildren().add(new Label("Title:"));
        this.getChildren().add(nameField);

        final TextField delayField = new TextField(String.valueOf(delay));
        delayField.setMaxWidth(100);
        this.getChildren().add(new Label("Delay:"));
        this.getChildren().add(delayField);

        final Button saveButton = new Button("Save");
        saveButton.setOnAction(actionEvent -> listener.onSave(nameField.getText(), Float.parseFloat(delayField.getText())));
        this.getChildren().add(saveButton);
    }

    public interface Listener {
        void onSave(final String name, final float delay);
    }

}
