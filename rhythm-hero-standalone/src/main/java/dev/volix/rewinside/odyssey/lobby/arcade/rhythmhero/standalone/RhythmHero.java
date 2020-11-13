package dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.standalone;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.application.Application;
import javafx.stage.Stage;
import dev.volix.rewinside.odyssey.common.frames.color.ColorTransformer;
import dev.volix.rewinside.odyssey.common.frames.color.MinecraftColorPalette;
import dev.volix.rewinside.odyssey.lobby.arcade.FrameGameCreator;
import dev.volix.rewinside.odyssey.lobby.arcade.standalone.FrameGameApplication;
import dev.volix.rewinside.odyssey.common.frames.resource.font.FontAdapter;
import dev.volix.rewinside.odyssey.common.frames.resource.font.FontFileAdapter;
import dev.volix.rewinside.odyssey.common.frames.resource.image.ImageAdapter;
import dev.volix.rewinside.odyssey.common.frames.resource.image.ImageFileAdapter;
import dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.RhythmHeroGame;
import dev.volix.rewinside.odyssey.lobby.arcade.nbs.song.SongPlayerCreator;
import dev.volix.rewinside.odyssey.lobby.arcade.nbs.Note;
import dev.volix.rewinside.odyssey.lobby.arcade.nbs.song.SongPlayer;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * @author Benedikt Wüller
 */
public class RhythmHero extends Application {

    private final File resources;

    public RhythmHero() {
        final File[] directories = new File[] {
                new File("./resources"),
                new File("../resources")
        };

        File directory = null;
        for (final File file : directories) {
            if (!file.exists()) continue;
            directory = file.getAbsoluteFile();
        }

        this.resources = directory;
    }

    @Override
    public void start(final Stage stage) {
        final SongPlayerCreator songListener = (game, song) -> new SongPlayer(song) {
            @Override
            protected void playNote(final Note note, final int tick) {
                try {
                    final Path path = Paths.get(RhythmHero.this.resources.getAbsolutePath(), "sounds", note.getInstrument().getFileName());
                    Sonic.play(path.toFile(), 1.0f, (float) note.getPitch(), (float) note.getVolume());
                } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
                    e.printStackTrace();
                }
            }

            @Override protected void onPlayTick(final int tick) {}
        };

        final ColorTransformer transformer = new MinecraftColorPalette();
        final ImageAdapter imageAdapter = new ImageFileAdapter(this.resources.getAbsolutePath());
        final FontAdapter fontAdapter = new FontFileAdapter(this.resources.getAbsolutePath());
        final FrameGameCreator creator = () -> {
            final File file = Paths.get(this.resources.getAbsolutePath(), "songs").toFile();
            return new RhythmHeroGame(new Dimension(512, 512), transformer, imageAdapter, fontAdapter, songListener, file, true);
        };

        new FrameGameApplication("Rhythm Hero", creator).start();
    }

    public static void main(String[] args) {
        launch(RhythmHero.class);
    }

}
