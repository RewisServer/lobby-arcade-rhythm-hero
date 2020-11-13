package dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero;

import dev.volix.rewinside.odyssey.lobby.arcade.component.PauseComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import dev.volix.rewinside.odyssey.common.frames.alignment.Alignment;
import dev.volix.rewinside.odyssey.common.frames.color.ColorTransformer;
import dev.volix.rewinside.odyssey.common.frames.component.*;
import dev.volix.rewinside.odyssey.lobby.arcade.Direction;
import dev.volix.rewinside.odyssey.lobby.arcade.FrameGame;
import dev.volix.rewinside.odyssey.lobby.arcade.GameState;
import dev.volix.rewinside.odyssey.lobby.arcade.InputKey;
import dev.volix.rewinside.odyssey.common.frames.resource.font.FontAdapter;
import dev.volix.rewinside.odyssey.common.frames.resource.image.ImageAdapter;
import dev.volix.rewinside.odyssey.common.frames.resource.image.SpriteSheet;
import dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.component.ArrowComponent;
import dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.component.ComboComponent;
import dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.component.NoteComponent;
import dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.component.ProgressComponent;
import dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.component.ScoreComponent;
import dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.component.StatisticsComponent;
import dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.mapping.NoteMapping;
import dev.volix.rewinside.odyssey.lobby.arcade.nbs.song.Song;
import dev.volix.rewinside.odyssey.lobby.arcade.nbs.song.SongParser;
import dev.volix.rewinside.odyssey.lobby.arcade.nbs.song.SongPlayer;
import dev.volix.rewinside.odyssey.lobby.arcade.nbs.song.SongPlayerCreator;
import dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.component.SongSelectorComponent;

/**
 * @author Benedikt Wüller
 */
public class RhythmHeroGame extends FrameGame {

    private static final String FONT_NAME = "JetBrainsMono-ExtraBold";
    private static final String INFO_FONT_NAME = "Roboto-Regular";

    private static final int SCORE_PER_NOTE = 10;
    private static final int ERROR_THRESHOLD = 250;
    private static final int IDEAL_HEIGHT = 200;

    private static final int MAX_COMBO = 8;
    private static final int MIN_COMBO_NOTES = 5;
    private static final double COMBO_PERCENTAGE = 0.05;

    private static final int INFO_TEXT_DURATION = 400;
    private static final Color INFO_TEXT_COLOR = Color.GRAY;

    private final ImageAdapter imageAdapter;
    private final FontAdapter fontAdapter;

    private final ProgressComponent progressComponent;
    protected final ComboComponent comboComponent;
    protected final ScoreComponent scoreComponent;

    private final Component idleComponent;
    private final SongSelectorComponent songSelectorComponent;
    private final CompoundComponent baseComponent;
    private final CompoundComponent noteCompoundComponent;
    private final PauseComponent pauseComponent;

    private final Map<Direction, TextComponent> infoTextComponents = new HashMap<>();
    private final Map<Direction, Long> infoTextTimestamps = new HashMap<>();

    private final Map<Direction, ArrowComponent> arrowComponents = new HashMap<>();
    private final Map<NoteMapping, NoteComponent> noteComponents = new LinkedHashMap<>();
    private final Map<Integer, Component> debugComponent = new HashMap<>();

    private final Set<NoteMapping> missedNotes = new HashSet<>();

    private final SongPlayerCreator songPlayerCreator;
    private final boolean debug;

    private SongPlayer songPlayer;
    private double noteSpeed;
    private long songStartedAt;

    protected SongBundle bundle;

    protected int hits;
    protected int perfectHits;
    protected int missedHits;

    private String songKey;
    private StatisticsComponent statisticsComponent;

    private Set<SongSelectedListener> songListeners = new HashSet<>();

    @SneakyThrows
    public RhythmHeroGame(final Dimension viewportDimension, final ColorTransformer transformer, final ImageAdapter imageAdapter,
                          final FontAdapter fontAdapter, final SongPlayerCreator songListener, final File songDirectory, final boolean debug) {
        super(new Dimension(256, 256), viewportDimension, 50, transformer);

        this.setInputDescription(InputKey.LEFT, "Links");
        this.setInputDescription(InputKey.RIGHT, "Rechts");
        this.setInputDescription(InputKey.UP, "Hoch");
        this.setInputDescription(InputKey.DOWN, "Runter");
        this.setInputDescription(InputKey.SPACE, "Bestätigen");

        this.debug = debug;

        this.setKeyRepeatInterval(150);

        this.songPlayerCreator = songListener;
        this.imageAdapter = imageAdapter;
        this.fontAdapter = fontAdapter;

        this.idleComponent = new ImageComponent(new Point(), this.getCanvasDimensions(), imageAdapter.get("idle"));

        this.baseComponent = new CompoundComponent(new Point(), this.getCanvasDimensions());
        this.baseComponent.addComponent(new ImageComponent(new Point(), this.getCanvasDimensions(), imageAdapter.get("background")));

        this.scoreComponent = new ScoreComponent(new Point(this.getCanvasDimensions().width - 32, 24), Color.WHITE, fontAdapter.get(FONT_NAME, 18.0f), Alignment.TOP_RIGHT);
        this.baseComponent.addComponent(this.scoreComponent);

        this.comboComponent = new ComboComponent(new Point(this.getCanvasDimensions().width - 32, 47), Color.WHITE, fontAdapter.get(INFO_FONT_NAME, 13.0f), Alignment.TOP_RIGHT);
        this.baseComponent.addComponent(this.comboComponent);

        this.noteCompoundComponent = new CompoundComponent(new Point(), this.getCanvasDimensions());
        this.baseComponent.addComponent(this.noteCompoundComponent);

        final Point arrowWestPosition = new Point(8 + NoteComponent.SIZE, IDEAL_HEIGHT);
        final Point arrowNorthPosition = new Point(8 + (256 - NoteComponent.SIZE * 2) / 4 + NoteComponent.SIZE, IDEAL_HEIGHT);
        final Point arrowSouthPosition = new Point(8 + 2 * (256 - NoteComponent.SIZE * 2) / 4 + NoteComponent.SIZE, IDEAL_HEIGHT);
        final Point arrowEastPosition = new Point(8 + 3 * (256 - NoteComponent.SIZE * 2) / 4 + NoteComponent.SIZE, IDEAL_HEIGHT);

        final SpriteSheet spriteSheet = imageAdapter.getSheet("arrows", ArrowComponent.SIZE);
        this.arrowComponents.put(Direction.WEST, new ArrowComponent(IDEAL_HEIGHT, spriteSheet, Direction.WEST));
        this.arrowComponents.put(Direction.NORTH, new ArrowComponent(IDEAL_HEIGHT, spriteSheet, Direction.NORTH));
        this.arrowComponents.put(Direction.SOUTH, new ArrowComponent(IDEAL_HEIGHT, spriteSheet, Direction.SOUTH));
        this.arrowComponents.put(Direction.EAST, new ArrowComponent(IDEAL_HEIGHT, spriteSheet, Direction.EAST));

        for (final Component component : this.arrowComponents.values()) {
            this.baseComponent.addComponent(component);
        }

        final Font infoFont = fontAdapter.get(INFO_FONT_NAME, 11.0f);
        this.infoTextComponents.put(Direction.WEST, new TextComponent(new Point(arrowWestPosition.x + NoteComponent.SIZE / 2, arrowWestPosition.y - 5), null, INFO_TEXT_COLOR, infoFont, Alignment.BOTTOM_CENTER));
        this.infoTextComponents.put(Direction.NORTH, new TextComponent(new Point(arrowNorthPosition.x + NoteComponent.SIZE / 2, arrowNorthPosition.y - 5), null, INFO_TEXT_COLOR, infoFont, Alignment.BOTTOM_CENTER));
        this.infoTextComponents.put(Direction.SOUTH, new TextComponent(new Point(arrowSouthPosition.x + NoteComponent.SIZE / 2, arrowSouthPosition.y - 5), null, INFO_TEXT_COLOR, infoFont, Alignment.BOTTOM_CENTER));
        this.infoTextComponents.put(Direction.EAST, new TextComponent(new Point(arrowEastPosition.x + NoteComponent.SIZE / 2, arrowEastPosition.y - 5), null, INFO_TEXT_COLOR, infoFont, Alignment.BOTTOM_CENTER));

        for (final Component component : this.infoTextComponents.values()) {
            this.baseComponent.addComponent(component);
        }

        final List<SongBundle> bundles = new ArrayList<>();
        final File[] mappings = songDirectory.listFiles(file -> file.getName().endsWith(".json"));
        if (mappings == null) throw new RuntimeException("Unable to load mappings from " + songDirectory + ".");

        for (final File mapping : mappings) {
            final String basePath = mapping.getParentFile().getAbsolutePath();
            final String fileName = mapping.getName().replace(".json", "");
            final Song song = new SongParser().parse(Paths.get(basePath, fileName + ".nbs").toFile());
            bundles.add(new SongBundle(basePath, fileName, song, mapping));
        }

        this.songSelectorComponent = new SongSelectorComponent(this.getCanvasDimensions(), fontAdapter, imageAdapter, bundles);

        this.progressComponent = new ProgressComponent(new Point(0, this.getCanvasDimensions().height - 5), new Dimension(this.getCanvasDimensions().width, 5));
        this.baseComponent.addComponent(this.progressComponent);

        this.pauseComponent = new PauseComponent(new Point(), this.getCanvasDimensions(), this.fontAdapter, this.idleComponent);
    }

    private void setMapping(final SongBundle bundle) {
        this.songPlayer = this.songPlayerCreator.createPlayer(this, bundle.song);
        this.songStartedAt = 0;
        this.noteSpeed = 2500;
        this.bundle = bundle;
        this.songKey = bundle.mapping.title.toLowerCase().replace(" ", "-").replace("_", "-");

        final List<NoteMapping> notes = this.bundle.mapping.notes;
        for (int i = 0; i < notes.size(); i++) {
            final NoteMapping note = notes.get(notes.size() - 1 - i);
            final NoteComponent component = new NoteComponent(this.imageAdapter.getSheet("arrows", NoteComponent.SIZE), note.direction);
            this.noteComponents.put(note, component);
        }

        final int comboStepSize = (int) (this.bundle.mapping.notes.size() * COMBO_PERCENTAGE);
        this.comboComponent.setStepSize(Math.max(MIN_COMBO_NOTES, comboStepSize));
        this.comboComponent.setComboCap(MAX_COMBO);

        for (final SongSelectedListener listener : this.songListeners) {
            listener.onSongSelected(this, bundle);
        }
    }

    protected boolean onUpdate(final long currentTime, final long delta) {
        if (this.getState() != GameState.RUNNING) return false;
        if (this.bundle == null) return true;

        if (this.songStartedAt == 0) {
            this.songStartedAt = currentTime;
            this.songPlayer.play(this.bundle.mapping.songDelay);
        }

        // Relative timestamp since the start of the song.
        final long timestamp = currentTime - this.songStartedAt - this.bundle.mapping.songDelay;

        for (final Map.Entry<NoteMapping, NoteComponent> entry : new LinkedHashSet<>(this.noteComponents.entrySet())) {
            final NoteMapping note = entry.getKey();
            final NoteComponent component = entry.getValue();

            final int tick = (int) (note.timestamp / this.bundle.song.getNoteInterval());

            if (!this.setComponentHeight(component, note.timestamp, timestamp, this.noteCompoundComponent)) continue;

            if (this.debug) {
                this.debugComponent.computeIfAbsent(tick, key -> {
                    final Component debugComponent = new TextComponent(
                            new Point(), String.valueOf(tick), Color.BLACK,
                            this.fontAdapter.get(FONT_NAME, 10.0f), Alignment.TOP_LEFT
                    );
                    this.baseComponent.addComponent(debugComponent);
                    return debugComponent;
                });
                this.debugComponent.get(tick).getPosition().y = component.getPosition().y;
            }

            // Check if note has been missed.
            if (timestamp > note.timestamp + ERROR_THRESHOLD && !this.missedNotes.contains(note)) {
                this.missedNotes.add(note);
                this.setInfoText(entry.getKey().direction, "VORBEI");
                this.comboComponent.resetCombo();
                this.progressComponent.addMissedNote();
                this.missedHits += 1;
            }

            // Remove notes which are out of screen.
            if (component.getPosition().y >= this.getCanvasDimensions().height) {
                this.removeNote(note);
                if (this.debug) this.baseComponent.removeComponent(this.debugComponent.remove(tick));
            }
        }

        // Show and fade out info messages.
        for (final Map.Entry<Direction, Long> entry : this.infoTextTimestamps.entrySet()) {
            final TextComponent component = this.infoTextComponents.get(entry.getKey());
            final long infoDelta = currentTime - entry.getValue();

            // If the message is no longer visible, remove the text.
            if (entry.getValue() + INFO_TEXT_DURATION < currentTime) {
                if (component == null || component.getText() == null) continue;
                component.setText(null);
                continue;
            }

            // Set alpha based on visibility duration.
            final double percentage = 1.0 - infoDelta * 1.0 / INFO_TEXT_DURATION;
            final Color color = new Color(INFO_TEXT_COLOR.getRed(), INFO_TEXT_COLOR.getGreen(), INFO_TEXT_COLOR.getBlue(), (int) Math.round(255 * percentage));
            component.setColor(color);
        }

        // Disable highlighting after 100 milliseconds.
        for (final ArrowComponent component : this.arrowComponents.values()) {
            if (component.getActiveSince() < 0) continue;
            if (System.currentTimeMillis() - component.getActiveSince() < 100) continue;
            component.setActive(false);
        }

        // Update percentage display.
        if (timestamp >= 0) {
            this.progressComponent.setPercentage(timestamp * 1.0 / this.bundle.mapping.duration);
        }

        if (timestamp > this.bundle.mapping.duration + this.bundle.song.getNoteInterval()) {
            this.statisticsComponent = new StatisticsComponent(
                    new Point(), this.getCanvasDimensions(), this.imageAdapter, this.fontAdapter,
                    this.bundle.mapping, this.scoreComponent.getScore(), this.comboComponent.getMaxComboNotes(),
                    this.hits, this.perfectHits, this.missedHits
            );

            this.statisticsComponent.addComponent(this.progressComponent);

            this.setState(GameState.DONE);
        }

        return true;
    }

    private boolean setComponentHeight(final Component component, final double componentTimestamp, final double timestamp, final CompoundComponent parentComponent) {
        final double percentage = (timestamp - componentTimestamp) / this.noteSpeed;
        final int targetHeight = (int) (IDEAL_HEIGHT * percentage) + IDEAL_HEIGHT;

        if (targetHeight <= -component.getDimensions().height) return false;

        if (component.getPosition().y > -component.getDimensions().height) {
            parentComponent.addComponent(component);
        }

        component.getPosition().y = targetHeight;

        // Remove notes which are out of screen.
        if (component.getPosition().y >= this.getCanvasDimensions().height) {
            parentComponent.removeComponent(component);
        }

        return true;
    }

    @NotNull
    protected Component getRenderComponent(@NotNull final GameState state) {
        if (state == GameState.IDLE) return this.idleComponent;
        if (this.isPaused()) return this.pauseComponent;
        if (state == GameState.DONE) return this.statisticsComponent;
        if (bundle == null) return this.songSelectorComponent;
        return this.baseComponent;
    }

    @Override
    protected void onKeyRepeat(@NotNull final InputKey key, final long currentTime) {
        // Handle song selector.
        if (this.bundle == null) {
            if (key == InputKey.UP) this.songSelectorComponent.scrollUp();
            if (key == InputKey.DOWN) this.songSelectorComponent.scrollDown();
        }
    }

    @Override
    protected void onKeyDown(@NotNull final InputKey key, final long currentTime) {
        // Handle song selector.
        if (this.bundle == null) {
            if (key == InputKey.UP) this.songSelectorComponent.scrollUp();
            if (key == InputKey.DOWN) this.songSelectorComponent.scrollDown();
            if (key == InputKey.SPACE) {
                final SongBundle bundle = this.songSelectorComponent.setSelectedBundle();
                this.setMapping(bundle);
            }
            return;
        }

        // Check if song has started yet.
        if (this.songStartedAt == 0) return;

        final Direction direction = key.getDirection();
        if (direction == null) return;
        this.pressArrow(currentTime, direction);
    }

    private void pressArrow(final long currentTime, final Direction direction) {
        final long timestamp = currentTime - this.songStartedAt - this.bundle.mapping.songDelay;
        this.arrowComponents.get(direction).setActive(true);

        // Find the first matching note within the error threshold.
        final Optional<Map.Entry<NoteMapping, NoteComponent>> result = this.noteComponents.entrySet().stream()
                .sorted(Comparator.comparingDouble(entry -> entry.getKey().timestamp))
                .filter(entry -> {
                    final NoteMapping note = entry.getKey();
                    if (note.direction != direction) return false;
                    final double difference = Math.abs(note.timestamp - timestamp);
                    return difference <= ERROR_THRESHOLD;
                }).findFirst();

        if (!result.isPresent()) {
            // No matching note could be found.
            this.setInfoText(direction, "DANEBEN");
            this.comboComponent.resetCombo();
            return;
        }

        final NoteMapping note = result.get().getKey();

        // Calculate score based on timing.
        final double difference = Math.abs(note.timestamp - timestamp);
        final double percentage = 1.0 - (difference * 1.0 / ERROR_THRESHOLD);
        final int score = (int) Math.ceil(SCORE_PER_NOTE * percentage);
        this.scoreComponent.addScore(score * this.comboComponent.getCombo());

        if (score == SCORE_PER_NOTE) {
            // Maximum possible score = perfect hit.
            this.setInfoText(direction, "PERFEKT");
            this.perfectHits += 1;
        }

        this.removeNote(note);
        this.comboComponent.addNote();
        this.hits += 1;

        if (this.debug) {
            this.baseComponent.removeComponent(this.debugComponent.remove((int) (note.timestamp / this.bundle.song.getNoteInterval())));
        }
    }

    private void setInfoText(final Direction direction, final String text) {
        this.infoTextTimestamps.put(direction, this.getTotalTime());
        this.infoTextComponents.get(direction).setText(text);
    }

    private void removeNote(final NoteMapping note) {
        this.noteCompoundComponent.removeComponent(this.noteComponents.remove(note));
        this.missedNotes.remove(note);
    }

    @Override
    protected void onPause() {
        if (this.songPlayer == null || !this.songPlayer.isPlaying()) return;
        this.songPlayer.stop();
    }

    @Override
    protected void onResume() {
        if (this.songPlayer == null || this.songPlayer.isPlaying()) return;

        final int tick = (int) ((this.getTotalTime() - this.songStartedAt - this.bundle.mapping.songDelay) / this.bundle.song.getNoteInterval());
        if (tick < 0) return;

        this.songPlayer.setCurrentTick(tick);
        this.songPlayer.play();
    }

    @Override
    protected void onStateChange(@NotNull final GameState oldState, @NotNull final GameState newState) {
        if (newState == GameState.RUNNING) return;
        if (this.songPlayer == null) return;
        this.songPlayer.stop();
    }

    public String getSongKey() {
        return this.songKey;
    }

    public void addSongListener(final SongSelectedListener listener) {
        this.songListeners.add(listener);
    }

    public void removeSongListener(final SongSelectedListener listener) {
        this.songListeners.remove(listener);
    }

}
