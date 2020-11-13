package dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.component;

import java.awt.Point;
import lombok.Getter;
import dev.volix.rewinside.odyssey.common.frames.component.SpriteComponent;
import dev.volix.rewinside.odyssey.lobby.arcade.Direction;
import dev.volix.rewinside.odyssey.common.frames.resource.image.SpriteSheet;

/**
 * @author Benedikt Wüller
 */
public class ArrowComponent extends SpriteComponent {

    public static final int SIZE = 32;

    private final Direction direction;

    private long activeSince;

    public ArrowComponent(final int height, final SpriteSheet spriteSheet, final Direction direction) {
        super(new Point(8 + (getIndex(direction) % 4) * (256 - SIZE * 2) / 4 + SIZE, height), spriteSheet, getIndex(direction));
        this.direction = direction;
    }

    public void setActive(final boolean active) {
        this.setSpriteIndex(getIndex(this.direction) + (active ? 4 : 0));
        this.activeSince = active ? System.currentTimeMillis() : -1;
    }

    private static int getIndex(final Direction direction) {
        switch (direction) {
            case WEST: return 4;
            case NORTH: return 5;
            case SOUTH: return 6;
            case EAST: return 7;
        }
        return -1;
    }

    public long getActiveSince() {
        return activeSince;
    }
}
