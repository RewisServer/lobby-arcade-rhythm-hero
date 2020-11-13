package dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.component;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import lombok.Getter;
import dev.volix.rewinside.odyssey.common.frames.alignment.Alignment;
import dev.volix.rewinside.odyssey.common.frames.component.TextComponent;

/**
 * @author Benedikt Wüller
 */
public class ComboComponent extends TextComponent {

    private int comboCap = Integer.MAX_VALUE;
    private int stepSize = -1;

    private int combo = 1;
    private int notes = 0;

    private int maxComboNotes;

    public ComboComponent(final Point position, final Color color, final Font font, final Alignment alignment) {
        super(position, null, color, font, alignment);
    }

    public void setStepSize(final int stepSize) {
        this.stepSize = stepSize;
        this.update();
    }

    public void setComboCap(final int maxCombo) {
        this.comboCap = maxCombo;
        this.update();
    }

    public void addNote() {
        this.notes++;
        this.update();
    }

    public void resetCombo() {
        this.notes = 0;
        this.update();
    }

    private void update() {
        this.maxComboNotes = Math.max(this.maxComboNotes, this.notes);

        if (this.stepSize < 0) return;
        this.combo = Math.min(this.notes / this.stepSize + 1, this.comboCap);

        if (this.combo <= 1) {
            this.setText(null);
        } else {
            this.setText(this.notes + " (x" + this.combo + ")");
        }
    }

    public int getCombo() {
        return this.combo;
    }

    public int getMaxComboNotes() {
        return maxComboNotes;
    }

}
