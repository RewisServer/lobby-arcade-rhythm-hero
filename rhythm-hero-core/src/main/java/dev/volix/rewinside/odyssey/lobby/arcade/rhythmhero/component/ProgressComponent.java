package dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import org.jetbrains.annotations.NotNull;
import dev.volix.rewinside.odyssey.common.frames.component.ColorComponent;
import dev.volix.rewinside.odyssey.common.frames.component.CompoundComponent;

/**
 * @author Benedikt Wüller
 */
public class ProgressComponent extends CompoundComponent {

    private final ColorComponent percentageIndicator;

    private double percentage;

    public ProgressComponent(@NotNull final Point position, @NotNull final Dimension dimensions) {
        super(position, dimensions);

        this.percentageIndicator = new ColorComponent(new Point(), new Dimension(0, dimensions.height), new Color(255, 255, 255, 80));
        this.addComponent(this.percentageIndicator);
    }

    public void setPercentage(final double percentage) {
        this.percentage = percentage;
        this.update();
    }

    public void addMissedNote() {
        final int position = (int) Math.min(this.percentage * this.getDimensions().width, this.getDimensions().width);
        this.addComponent(new ColorComponent(new Point(position, 0), new Dimension(2, this.getDimensions().height), Color.RED));
    }

    private void update() {
        final double width = Math.min(this.percentage * this.getDimensions().width, this.getDimensions().width);
        this.percentageIndicator.getDimensions().width = (int) Math.ceil(width);
    }

}
