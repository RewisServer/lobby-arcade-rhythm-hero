package dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.component;

import java.awt.Point;
import dev.volix.rewinside.odyssey.common.frames.component.SpriteComponent;
import dev.volix.rewinside.odyssey.lobby.arcade.Direction;
import dev.volix.rewinside.odyssey.common.frames.resource.image.SpriteSheet;

/**
 * @author Benedikt Wüller
 */
public class NoteComponent extends SpriteComponent {

    public static final int SIZE = 32;

    public NoteComponent(final SpriteSheet spriteSheet, final Direction direction) {
        super(new Point(8 + getIndex(direction) * (256 - SIZE * 2) / 4 + SIZE, -SIZE), spriteSheet, getIndex(direction));
    }

    private static int getIndex(final Direction direction) {
        switch (direction) {
            case WEST: return 0;
            case NORTH: return 1;
            case SOUTH: return 2;
            case EAST: return 3;
        }
        return -1;
    }

}
