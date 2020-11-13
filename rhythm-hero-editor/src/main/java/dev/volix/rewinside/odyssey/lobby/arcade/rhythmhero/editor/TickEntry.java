package dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.editor;

import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import dev.volix.rewinside.odyssey.lobby.arcade.nbs.Note;

/**
 * @author Benedikt Wüller
 */
public class TickEntry {

    public int tick;
    public List<Note> notes;
    public SimpleBooleanProperty left;
    public SimpleBooleanProperty up;
    public SimpleBooleanProperty down;
    public SimpleBooleanProperty right;

    public TickEntry(final int tick, final List<Note> notes, final boolean left, final boolean up, final boolean down, final boolean right) {
        this.tick = tick;
        this.notes = notes;
        this.left = new SimpleBooleanProperty(left);
        this.up = new SimpleBooleanProperty(up);
        this.down = new SimpleBooleanProperty(down);
        this.right = new SimpleBooleanProperty(right);
    }

}
