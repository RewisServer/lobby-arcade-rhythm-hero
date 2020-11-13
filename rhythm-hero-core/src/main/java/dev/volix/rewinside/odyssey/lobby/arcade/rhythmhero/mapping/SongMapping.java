package dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.mapping;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Benedikt Wüller
 */
public class SongMapping {

    @SerializedName("title")
    public String title;

    @SerializedName("duration")
    public long duration;

    @SerializedName("note_speed")
    public long noteSpeed;

    @SerializedName("delay")
    public long songDelay;

    @SerializedName("note_delay")
    public float noteDelay;

    @SerializedName("notes")
    public final List<NoteMapping> notes = new ArrayList<>();

}
