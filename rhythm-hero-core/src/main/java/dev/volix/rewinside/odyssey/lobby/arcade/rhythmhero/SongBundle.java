package dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero;

import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import dev.volix.rewinside.odyssey.lobby.arcade.nbs.song.Song;
import dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.mapping.SongMapping;

/**
 * @author Benedikt Wüller
 */
public class SongBundle {

    public final String basePath;
    public final String fileName;
    public final Song song;
    public final File mappingFile;
    public final SongMapping mapping;

    public SongBundle(final String basePath, final String fileName, final Song song, final File mappingFile) {
        this.basePath = basePath;
        this.fileName = fileName;
        this.song = song;
        this.mappingFile = mappingFile;

        try {
            this.mapping = this.mappingFile.exists() ? new GsonBuilder().create().fromJson(new FileReader(this.mappingFile), SongMapping.class) : new SongMapping();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Unable to load mapping.", e);
        }
    }

}
