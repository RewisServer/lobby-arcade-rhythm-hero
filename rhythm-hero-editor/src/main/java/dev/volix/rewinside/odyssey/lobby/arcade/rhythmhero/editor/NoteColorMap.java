package dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.editor;

import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import dev.volix.rewinside.odyssey.lobby.arcade.nbs.Note;

/**
 * @author Benedikt Wüller
 */
public class NoteColorMap {

    private final Map<Note, SimpleObjectProperty<Color>> colors = new HashMap<>();

    public void setColor(final Note note, final Color color) {
        this.getColorProperty(note).setValue(color);
    }

    public SimpleObjectProperty<Color> getColorProperty(final Note note) {
        this.colors.computeIfAbsent(note, key -> new SimpleObjectProperty<>(Color.BLACK));
        return this.colors.get(note);
    }

}
