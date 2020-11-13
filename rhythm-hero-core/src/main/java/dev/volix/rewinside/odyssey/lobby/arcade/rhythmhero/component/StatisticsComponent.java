package dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import dev.volix.rewinside.odyssey.common.frames.alignment.Alignment;
import dev.volix.rewinside.odyssey.common.frames.component.CompoundComponent;
import dev.volix.rewinside.odyssey.common.frames.component.ImageComponent;
import dev.volix.rewinside.odyssey.common.frames.component.TextComponent;
import dev.volix.rewinside.odyssey.common.frames.resource.font.FontAdapter;
import dev.volix.rewinside.odyssey.common.frames.resource.image.ImageAdapter;
import dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.mapping.SongMapping;

/**
 * @author Benedikt Wüller
 */
public class StatisticsComponent extends CompoundComponent {

    private static final Color TEXT_COLOR = Color.ORANGE;

    private static final String FONT_SCORE = "JetBrainsMono-ExtraBold";
    private static final String FONT_REGULAR = "Roboto-Medium";

    public StatisticsComponent(final Point position, final Dimension dimensions,
                               final ImageAdapter imageAdapter, final FontAdapter fontAdapter,
                               final SongMapping mapping, final int score, final int maxCombo,
                               final int hits, final int perfectHits, final int missedNotes) {
        super(position, dimensions);

        this.addComponent(new ImageComponent(new Point(), dimensions, imageAdapter.get("game-over")));
        this.addComponent(new TextComponent(new Point(dimensions.width / 2, 15), mapping.title, new Color(229, 229, 51), fontAdapter.get(FONT_REGULAR, 20.0f), Alignment.TOP_CENTER));
        this.addComponent(new TextComponent(new Point(dimensions.width / 2 + 5, 60), String.valueOf(score), Color.BLACK, fontAdapter.get(FONT_SCORE, 25.0f), Alignment.TOP_CENTER));

        final Font nameFont = fontAdapter.get(FONT_REGULAR, 18.0f);
        final Font valueFont = fontAdapter.get(FONT_SCORE, 18.0f);
        this.addStat(128, "Beste Combo", String.valueOf(maxCombo), nameFont, valueFont, Color.GREEN.darker());
        this.addStat(154, "Getroffene Noten", String.valueOf(hits), nameFont, valueFont, Color.GREEN.darker());
        this.addStat(183, "Perfekte Noten", String.valueOf(perfectHits), nameFont, valueFont, Color.GREEN.darker());
        this.addStat(210, "Verpasste Noten", String.valueOf(missedNotes), nameFont, valueFont, missedNotes == 0 ? Color.GREEN.darker() : Color.RED.darker());
    }

    private void addStat(final int height, final String name, final String value, final Font nameFont, final Font valueFont, final Color valueColor) {
        this.addComponent(new TextComponent(new Point(18, height), name + ":", TEXT_COLOR, nameFont, Alignment.TOP_LEFT));
        this.addComponent(new TextComponent(new Point(this.getDimensions().width - 18, height), value, valueColor, valueFont, Alignment.TOP_RIGHT));
    }

}
