package dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.mapping;

import com.google.gson.annotations.SerializedName;
import dev.volix.rewinside.odyssey.lobby.arcade.Direction;

/**
 * @author Benedikt Wüller
 */
public class NoteMapping {

    @SerializedName("direction")
    public final Direction direction;

    @SerializedName("timestamp")
    public final double timestamp;

    public NoteMapping(final Direction direction, final double timestamp) {
        this.direction = direction;
        this.timestamp = timestamp;
    }

}
