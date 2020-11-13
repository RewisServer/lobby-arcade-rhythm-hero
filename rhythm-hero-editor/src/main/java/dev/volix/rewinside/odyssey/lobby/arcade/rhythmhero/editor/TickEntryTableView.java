package dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import dev.volix.rewinside.odyssey.lobby.arcade.Direction;
import dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.mapping.NoteMapping;
import dev.volix.rewinside.odyssey.lobby.arcade.nbs.Note;
import dev.volix.rewinside.odyssey.lobby.arcade.nbs.song.Song;

/**
 * @author Benedikt Wüller
 */
public class TickEntryTableView extends TableView<TickEntry> {

    private final NoteColorMap colorMap;

    private ColorEditor colorEditor;

    public TickEntryTableView(final Song song, final List<NoteMapping> noteMappings) {
        this.colorMap = new NoteColorMap();
        this.setupData(song, noteMappings);
        this.setupColumns();

        this.setOnKeyPressed(event -> {
            final TickEntry entry = this.getSelectionModel().getSelectedItem();
            if (entry == null) return;

            if (event.getCode() == KeyCode.W) entry.up.set(true);
            if (event.getCode() == KeyCode.A) entry.left.set(true);
            if (event.getCode() == KeyCode.S) entry.down.set(true);
            if (event.getCode() == KeyCode.D) entry.right.set(true);

            if (event.getCode() == KeyCode.BACK_SPACE) {
                entry.up.set(false);
                entry.down.set(false);
                entry.left.set(false);
                entry.right.set(false);
            }
        });
    }

    public void showColorEditor() {
        this.colorEditor.show();
    }

    private void setupData(final Song song, final List<NoteMapping> noteMappings) {
        this.getItems().clear();

        final double tickTime = song.getNoteInterval();

        final Set<Note> editorNotes = new HashSet<>();

        for (int tick = 0; tick <= song.getTicks(); tick++) {
            final List<Note> notes = new ArrayList<>(song.getNotes(tick));
            editorNotes.addAll(notes);

            final int finalTick = tick;
            final boolean left = noteMappings.stream().anyMatch(it -> it.direction == Direction.WEST && Math.round((it.timestamp) / tickTime) == finalTick);
            final boolean up = noteMappings.stream().anyMatch(it -> it.direction == Direction.NORTH && Math.round((it.timestamp) / tickTime) == finalTick);
            final boolean down = noteMappings.stream().anyMatch(it -> it.direction == Direction.SOUTH && Math.round((it.timestamp) / tickTime) == finalTick);
            final boolean right = noteMappings.stream().anyMatch(it -> it.direction == Direction.EAST && Math.round((it.timestamp) / tickTime) == finalTick);

            this.getItems().add(new TickEntry(tick, notes, left, up, down, right));
        }

        this.colorEditor = new ColorEditor(this.colorMap, editorNotes);
    }

    private void setupColumns() {
        final TableColumn<TickEntry, Number> tickColumn = new TableColumn<>("Tick");
        tickColumn.setCellValueFactory(entry -> new SimpleIntegerProperty(entry.getValue().tick + 1));
        tickColumn.setEditable(false);
        tickColumn.setSortable(false);

        final TableColumn<TickEntry, String> noteColumn = new TableColumn<>("Notes");
        noteColumn.setCellValueFactory(entry -> new SimpleStringProperty("dummy"));
        noteColumn.setCellFactory(entry -> new TableCell<TickEntry, String>() {
            @Override
            protected void updateItem(final String item, final boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                final HBox box = new HBox();
                box.setSpacing(3);

                final TickEntry self = TickEntryTableView.this.getItems().get(this.getIndex());
                for (final Note note : self.notes) {
                    final Text text = new Text(note.getKey().getDisplayName() + note.getOctave().getIndicator());

                    final SimpleObjectProperty<Color> property = colorMap.getColorProperty(note);
                    property.addListener((observable, oldValue, newValue) -> text.setFill(newValue));
                    text.setFill(property.get());
                    text.setStyle("-fx-font-weight: bold");

                    box.getChildren().add(text);
                }

                setGraphic(box);
            }
        });
        noteColumn.setPrefWidth(125);
        noteColumn.setSortable(false);
        noteColumn.setEditable(false);

        final TableColumn<TickEntry, Boolean> leftColumn = new TableColumn<>("Left (A)");
        leftColumn.setCellFactory(entry -> new CheckBoxTableCell<>());
        leftColumn.setCellValueFactory(entry -> entry.getValue().left);
        leftColumn.setSortable(false);

        final TableColumn<TickEntry, Boolean> upColumn = new TableColumn<>("Up (W)");
        upColumn.setCellFactory(entry -> new CheckBoxTableCell<>());
        upColumn.setCellValueFactory(entry -> entry.getValue().up);
        upColumn.setSortable(false);

        final TableColumn<TickEntry, Boolean> downColumn = new TableColumn<>("Down (S)");
        downColumn.setCellFactory(entry -> new CheckBoxTableCell<>());
        downColumn.setCellValueFactory(entry -> entry.getValue().down);
        downColumn.setSortable(false);

        final TableColumn<TickEntry, Boolean> rightColumn = new TableColumn<>("Right (D)");
        rightColumn.setCellFactory(entry -> new CheckBoxTableCell<>());
        rightColumn.setCellValueFactory(entry -> entry.getValue().right);
        rightColumn.setSortable(false);

        final TableColumn<TickEntry, String> actionColumn = new TableColumn<>("Actions");
        actionColumn.setCellValueFactory(entry -> new SimpleStringProperty("dummy"));
        actionColumn.setCellFactory(entry -> {
            final Button cloneButton = new Button("⎘");
            cloneButton.setPadding(new Insets(0, 5, 0, 5));
            cloneButton.setStyle("-fx-font-size: 14px");
            cloneButton.setTooltip(new Tooltip("Copy to all ticks with the exact same notes."));

            return new TableCell<TickEntry, String>() {
                @Override
                protected void updateItem(final String item, final boolean empty) {
                    super.updateItem(item, empty);
                    setText(null);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        cloneButton.setOnAction(event -> {
                            final TickEntry self = TickEntryTableView.this.getItems().get(this.getIndex());

                            for (final TickEntry entry : TickEntryTableView.this.getItems()) {
                                if (entry == self) continue;
                                if (entry.notes.size() != self.notes.size()) continue;

                                boolean equal = true;
                                for (final Note key : self.notes) {
                                    if (!entry.notes.contains(key)) {
                                        equal = false;
                                        break;
                                    }
                                }
                                if (!equal) continue;

                                entry.left.set(self.left.get());
                                entry.up.set(self.up.get());
                                entry.down.set(self.down.get());
                                entry.right.set(self.right.get());
                            }
                        });
                        setGraphic(cloneButton);
                    }
                }
            };
        });
        actionColumn.setSortable(false);

        this.setEditable(true);
        this.getColumns().add(tickColumn);
        this.getColumns().add(noteColumn);
        this.getColumns().add(leftColumn);
        this.getColumns().add(upColumn);
        this.getColumns().add(downColumn);
        this.getColumns().add(rightColumn);
        this.getColumns().add(actionColumn);
    }

}
