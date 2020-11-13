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
public class ScoreComponent extends TextComponent {

    private int score;

    public ScoreComponent(final Point position, final Color color, final Font font, final Alignment alignment) {
        super(position, "0", color, font, alignment);
    }

    public void addScore(final int score) {
        this.score += score;
        this.setText(String.valueOf(this.score));
    }

    public int getScore() {
        return score;
    }
}
