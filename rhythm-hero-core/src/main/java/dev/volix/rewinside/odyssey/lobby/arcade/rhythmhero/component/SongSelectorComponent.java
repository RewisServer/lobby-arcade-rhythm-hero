package dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import dev.volix.rewinside.odyssey.common.frames.alignment.Alignment;
import dev.volix.rewinside.odyssey.common.frames.component.CompoundComponent;
import dev.volix.rewinside.odyssey.common.frames.component.ImageComponent;
import dev.volix.rewinside.odyssey.common.frames.component.SpriteComponent;
import dev.volix.rewinside.odyssey.common.frames.component.TextComponent;
import dev.volix.rewinside.odyssey.common.frames.resource.font.FontAdapter;
import dev.volix.rewinside.odyssey.common.frames.resource.image.ImageAdapter;
import dev.volix.rewinside.odyssey.common.frames.resource.image.SpriteSheet;
import dev.volix.rewinside.odyssey.lobby.arcade.rhythmhero.SongBundle;

/**
 * @author Benedikt Wüller
 */
public class SongSelectorComponent extends CompoundComponent {

    private static final String FONT_NAME = "Roboto-Medium";

    private final List<SongBundle> bundles = new ArrayList<>();

    private final SongComponent[] songComponents = new SongComponent[5];
    private final SpriteComponent scrollUpComponent;
    private final SpriteComponent scrollDownComponent;

    private final Font font;

    private int scrollIndex;
    private int selectedBundle;

    public SongSelectorComponent(@NotNull final Dimension dimensions, final FontAdapter fontAdapter, final ImageAdapter imageAdapter, final List<SongBundle> bundles) {
        super(new Point(), dimensions);

        bundles.sort(Comparator.comparing(bundle -> bundle.mapping.title.toLowerCase()));
        this.bundles.addAll(bundles);

        this.font = fontAdapter.get(FONT_NAME, 18.0f);

        this.addComponent(new ImageComponent(new Point(), dimensions, imageAdapter.get("menu")));

        for (int i = 0; i < this.songComponents.length; i++) {
            final SongBundle bundle = this.bundles.size() > i ? this.bundles.get(i) : null;
            this.songComponents[i] = new SongComponent(new Point(0, 64 + i * 32), new Dimension(dimensions.width, 32), bundle);
            this.addComponent(this.songComponents[i]);
        }

        this.addComponent(new TextComponent(new Point(dimensions.width / 2, 10), "WÄHLE EINEN SONG", Color.GRAY.brighter(), font, Alignment.TOP_CENTER));

        final SpriteSheet spriteSheet = imageAdapter.getSheet("scroll-arrows", 32);
        this.scrollUpComponent = new SpriteComponent(new Point(dimensions.width / 2 - 16, 36), spriteSheet, 1);
        this.scrollDownComponent = new SpriteComponent(new Point(dimensions.width / 2 - 16, dimensions.height - 32), spriteSheet,
                this.bundles.size() > this.songComponents.length ? 2 : 3
        );

        this.addComponent(this.scrollUpComponent);
        this.addComponent(this.scrollDownComponent);

        this.setSelectedBundle(0);
    }

    private void setSelectedBundle(final int index) {
        final int nextScrollIndex = index / this.songComponents.length * this.songComponents.length;
        this.songComponents[this.selectedBundle - this.scrollIndex].setSelected(false);

        if (nextScrollIndex != this.scrollIndex) {
            this.scrollIndex = nextScrollIndex;
            this.updatePage();
        }

        this.songComponents[index - this.scrollIndex].setSelected(true);
        this.selectedBundle = index;
        this.updatePage();
    }

    private void updatePage() {
        for (int i = 0; i < this.songComponents.length; i++) {
            final int index = this.scrollIndex + i;
            final SongBundle bundle = this.bundles.size() > index ? this.bundles.get(index) : null;
            this.songComponents[i].setBundle(bundle);
        }

        this.scrollUpComponent.setSpriteIndex(this.scrollIndex > 0 ? 0 : 1);
        this.scrollDownComponent.setSpriteIndex(this.bundles.size() > this.scrollIndex + this.songComponents.length ? 2 : 3);
    }

    public void scrollUp() {
        this.setSelectedBundle(Math.max(0, this.selectedBundle - 1));
    }

    public void scrollDown() {
        this.setSelectedBundle(Math.min(this.bundles.size() - 1, this.selectedBundle + 1));
    }

    public SongBundle setSelectedBundle() {
        return this.bundles.get(this.selectedBundle);
    }

    private class SongComponent extends CompoundComponent {

        private final TextComponent titleComponent;
        private final TextComponent versionComponent;

        private SongBundle bundle;

        public SongComponent(@NotNull final Point position, @NotNull final Dimension dimensions, final SongBundle bundle) {
            super(position, dimensions);

            this.titleComponent = new TextComponent(new Point(15, 0), null, new Color(229, 229, 51), SongSelectorComponent.this.font, Alignment.TOP_LEFT);
            this.versionComponent = new TextComponent(new Point(dimensions.width - 15, 0), null, Color.GRAY.darker(), SongSelectorComponent.this.font, Alignment.TOP_RIGHT);

            this.addComponent(this.titleComponent);
            this.addComponent(this.versionComponent);

            this.setBundle(bundle);
        }

        public void setBundle(final SongBundle bundle) {
            if (this.bundle == bundle) return;

            this.titleComponent.setText(bundle == null ? null : bundle.mapping.title);
            this.versionComponent.setText(bundle == null ? null : bundle.song.getSuggestedMinecraftVersion());

            this.bundle = bundle;
            this.setDirty(true);
        }

        public void setSelected(final boolean selected) {
            this.titleComponent.setColor(selected ? Color.WHITE : new Color(229, 229, 51));
            this.versionComponent.setColor(selected ? Color.GRAY : Color.GRAY.darker());
        }

    }

}
