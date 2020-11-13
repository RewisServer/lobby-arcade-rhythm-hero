package dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.editor;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import dev.volix.rewinside.odyssey.lobby.arcade.nbs.Note;

/**
 * @author Benedikt Wüller
 */
public class ColorEditor extends Stage {

    private final NoteColorMap colorMap;

    public ColorEditor(final NoteColorMap colorMap, final Set<Note> notes) {
        this.colorMap = colorMap;

        final TableView<Note> tableView = this.setupTable(notes.stream()
                .sorted(Comparator.comparingInt(note -> note.getOctave().ordinal()))
                .sorted(Comparator.comparingInt(note -> note.getKey().ordinal()))
                .sorted(Comparator.comparing(note -> note.getInstrument().name()))
                .sorted(Comparator.comparingInt(Note::getLayer))
                .collect(Collectors.toList()));

        final BorderPane pane = new BorderPane();
        pane.setCenter(tableView);

        final Scene scene = new Scene(pane, 350, 500);
        this.setTitle("Editor - Colors");
        this.setScene(scene);
    }

    private TableView<Note> setupTable(final List<Note> notes) {
        final TableView<Note> tableView = new TableView<>();
        tableView.setEditable(true);

        final TableColumn<Note, String> noteColumn = new TableColumn<>("Note");
        noteColumn.setCellValueFactory(entry -> new SimpleStringProperty(entry.getValue().getKey().getDisplayName() + entry.getValue().getOctave().getIndicator()));
        noteColumn.setEditable(false);

        final TableColumn<Note, Color> colorColumn = new TableColumn<>("Color");
        colorColumn.setCellFactory(entry ->  new ColorTableCell<>(colorColumn));
        colorColumn.setOnEditCommit(event -> this.colorMap.setColor(event.getRowValue(), event.getNewValue()));
        colorColumn.setCellValueFactory(entry -> this.colorMap.getColorProperty(entry.getValue()));
        colorColumn.setSortable(false);
        colorColumn.setEditable(true);

        final TableColumn<Note, String> instrumentColumn = new TableColumn<>("Layer / Instrument");
        instrumentColumn.setCellValueFactory(entry -> new SimpleStringProperty(entry.getValue().getLayer() + " / " + entry.getValue().getInstrument().name()));
        instrumentColumn.setEditable(false);

        tableView.getColumns().add(noteColumn);
        tableView.getColumns().add(colorColumn);
        tableView.getColumns().add(instrumentColumn);

        for (final Note note : notes) {
            tableView.getItems().add(note);
        }

        return tableView;
    }

}
