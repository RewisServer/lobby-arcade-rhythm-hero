package dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.editor;

import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;
import javafx.application.Application;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.SongBundle;
import dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.mapping.SongMapping;
import dev.volix.rewinside.odyssey.lobby.arcade.nbs.song.Song;
import dev.volix.rewinside.odyssey.lobby.arcade.nbs.song.SongParser;

/**
 * @author Benedikt Wüller
 */
public class EditorApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        final Optional<SongBundle> result = this.selectSongBundle();
        if (!result.isPresent()) return;

        final SongBundle bundle = result.get();

        if (bundle.song == null) {
            final Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to parse the NBS file.");
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }

        final SongMapping mapping = bundle.mappingFile.exists()
                ? new GsonBuilder().create().fromJson(new FileReader(bundle.mappingFile), SongMapping.class)
                : new SongMapping();

        new MappingEditor(bundle.basePath, bundle.fileName, bundle.song, mapping).show();
    }

    private Optional<SongBundle> selectSongBundle() {
        final File[] directories = new File[] {
                new File("./resources/songs"),
                new File("../resources/songs"),
                new File("./")
        };

        final FileChooser chooser = new FileChooser();
        chooser.setTitle("Select a song");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("NoteBlockSong Files", "*.nbs"));

        for (final File directory : directories) {
            if (!directory.exists()) continue;
            chooser.setInitialDirectory(directory);
            break;
        }

        final File nbsFile = chooser.showOpenDialog(new Stage());
        if (nbsFile == null) return Optional.empty();

        final String basePath = nbsFile.getParentFile().getAbsolutePath();
        final String fileName = nbsFile.getName().replace(".nbs", "");

        final File mappingFile = Paths.get(basePath, fileName + ".json").toFile();
        if (!mappingFile.exists()) {
            final Alert alert = new Alert(Alert.AlertType.INFORMATION, "No mapping could be found for the selected song. A new mapping will be created.");
            alert.setHeaderText(null);
            alert.showAndWait();
        }

        final Song song = new SongParser().parse(nbsFile);
        return Optional.of(new SongBundle(basePath, fileName, song, mappingFile));
    }

    public static void main(String[] args) {
        launch(args);
    }

}
