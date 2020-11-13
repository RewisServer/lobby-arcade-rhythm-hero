package dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.editor;

import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import dev.volix.rewinside.odyssey.lobby.arcade.Direction;
import dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.mapping.NoteMapping;
import dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.mapping.SongMapping;
import dev.volix.rewinside.odyssey.lobby.arcade.nbs.Note;
import dev.volix.rewinside.odyssey.lobby.arcade.nbs.song.Song;
import dev.volix.rewinside.odyssey.lobby.arcade.nbs.song.SongPlayer;

/**
 * @author Benedikt Wüller
 */
public class MappingEditor extends Stage {

    private final TickEntryTableView tableView;

    private final String basePath;
    private final SongPlayer songPlayer;
    private final double tickTime;

    public MappingEditor(final String basePath, final String fileName, final Song song, final SongMapping mapping) {
        this.basePath = basePath;

        this.tickTime = song.getNoteInterval();

        mapping.title = mapping.title == null ? fileName : mapping.title;
        mapping.songDelay = 3000;
        mapping.duration = (long) Math.ceil(this.tickTime * (song.getTicks() + 1));

        final BorderPane pane = new BorderPane();
        final Scene scene = new Scene(pane, 500, 625);

        this.tableView = new TickEntryTableView(song, mapping.notes);
        pane.setCenter(this.tableView);

        final VBox toolbar = new VBox();
        toolbar.getChildren().add(new Controls(new Controls.Listener() {
            @Override public void onTogglePlay() {
                final boolean isPlaying = MappingEditor.this.songPlayer.isPlaying();
                if (isPlaying) {
                    MappingEditor.this.songPlayer.stop();
                } else {
                    MappingEditor.this.songPlayer.play();
                }
            }

            @Override public void onChangeRate(final double rate) {
                MappingEditor.this.songPlayer.setPlaybackRate(rate);
            }

            @Override public void onEditColors() {
                MappingEditor.this.tableView.showColorEditor();
            }
        }));
        toolbar.getChildren().add(new Settings(mapping.title, mapping.noteDelay, (name, delay) -> {
            mapping.title = name;
            mapping.noteDelay = delay;
            this.save(this.tableView, mapping, basePath, fileName);
        }));
        pane.setTop(toolbar);

        this.setTitle("Editor - " + mapping.title);
        this.setScene(scene);
        this.setResizable(false);
        this.setOnCloseRequest(windowEvent -> System.exit(0));

        this.songPlayer = new SongPlayer(song) {
            @Override
            protected void onPlayTick(final int tick) {
                MappingEditor.this.tableView.getSelectionModel().select(tick);
                Platform.runLater(() -> MappingEditor.this.tableView.scrollTo(tick / 20 * 20));
            }

            @Override
            protected void playNote(final Note note, final int tick) {
                try {
                    final File parent = new File(MappingEditor.this.basePath).getParentFile();
                    final Path path = Paths.get(parent.getAbsolutePath(), "sounds", note.getInstrument().getFileName());
                    Sonic.play(path.toFile(), 1.0f, (float) note.getPitch(), (float) note.getVolume());
                } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
                    e.printStackTrace();
                }
            }
        };

        this.tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldEntry, newEntry) -> {
            if(this.songPlayer.isPlaying()) return;
            if (oldEntry == newEntry) return;

            final int index = this.tableView.getItems().indexOf(newEntry);
            MappingEditor.this.songPlayer.setCurrentTick(index);
            this.songPlayer.playTick(index, false);
        });
    }

    private void save(final TableView<TickEntry> tableView, final SongMapping mapping, final String basePath, final String fileName) {
        mapping.notes.clear();

        for (final TickEntry entry : tableView.getItems()) {
            final double timestamp = this.tickTime * entry.tick;
            if (entry.left.getValue()) mapping.notes.add(new NoteMapping(Direction.WEST, timestamp));
            if (entry.up.getValue()) mapping.notes.add(new NoteMapping(Direction.NORTH, timestamp));
            if (entry.down.getValue()) mapping.notes.add(new NoteMapping(Direction.SOUTH, timestamp));
            if (entry.right.getValue()) mapping.notes.add(new NoteMapping(Direction.EAST, timestamp));
        }

        try {
            final File file = new File(Paths.get(basePath, fileName + ".json").toString());
            final FileWriter writer = new FileWriter(file);
            new GsonBuilder().create().toJson(mapping, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
