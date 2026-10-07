package dev.linklecompanion.client.screen;

import dev.linklecompanion.client.lang.ModLanguage;
import dev.linklecompanion.config.LinkleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * Picks the language of everything Linkle says and shows, independent of the game's language.
 * Laid out like Minecraft's own language screen. Lists "Same as game" plus every language that has
 * a Linkle translation file (see {@link ModLanguage}).
 */
public class LinkleLanguageScreen extends OptionsSubScreen {
	private static final String P = "options.linkle_companion.language.";
	private static final int FOOTER_HEIGHT = 53;

	/** The settings screen's own parent, so the rebuilt settings screen returns to the right place. */
	private final Screen settingsParent;
	private @Nullable LanguageList languageList;

	public LinkleLanguageScreen(Screen settingsScreen, Screen settingsParent) {
		super(settingsScreen, Minecraft.getInstance().options, Component.translatable(P + "title"));
		this.settingsParent = settingsParent;
		layout.setFooterHeight(FOOTER_HEIGHT);
	}

	@Override
	protected void addContents() {
		languageList = layout.addToContents(new LanguageList(minecraft));
	}

	@Override
	protected void addOptions() {
	}

	@Override
	protected void addFooter() {
		LinearLayout footer = layout.addToFooter(LinearLayout.vertical()).spacing(8);
		footer.defaultCellSetting().alignHorizontallyCenter();
		footer.addChild(new StringWidget(Component.translatable(P + "note").withColor(0xFFBABABA), font));
		footer.addChild(Button.builder(CommonComponents.GUI_DONE, button -> onDone()).build());
	}

	@Override
	protected void repositionElements() {
		super.repositionElements();
		if (languageList != null) {
			languageList.updateSize(width, layout);
		}
	}

	private void onDone() {
		LanguageList.Entry selected = languageList == null ? null : languageList.getSelected();
		if (selected != null && !selected.code.equals(LinkleConfig.get().language)) {
			LinkleConfig.get().language = selected.code;
			LinkleConfig.save();
			ModLanguage.apply();
			// Rebuild the settings screen so its own labels switch language too.
			minecraft.gui.setScreen(new LinkleSettingsScreen(settingsParent));
			return;
		}
		onClose();
	}

	private class LanguageList extends ObjectSelectionList<LanguageList.Entry> {
		LanguageList(Minecraft minecraft) {
			super(minecraft, LinkleLanguageScreen.this.width, LinkleLanguageScreen.this.height - 33 - FOOTER_HEIGHT, 33, 18);
			String current = LinkleConfig.get().language;
			Component gameLanguage = ModLanguage.displayName(minecraft.getLanguageManager().getSelected());
			add(ModLanguage.AUTO, Component.translatable(P + "auto_current", gameLanguage), current);
			for (String code : ModLanguage.available()) {
				add(code, ModLanguage.displayName(code), current);
			}
			if (getSelected() != null) {
				centerScrollOn(getSelected());
			}
		}

		private void add(String code, Component name, String current) {
			Entry entry = new Entry(code, name);
			addEntry(entry);
			if (code.equals(current)) {
				setSelected(entry);
			}
		}

		@Override
		public int getRowWidth() {
			return super.getRowWidth() + 50;
		}

		class Entry extends ObjectSelectionList.Entry<Entry> {
			private final String code;
			private final Component name;

			Entry(String code, Component name) {
				this.code = code;
				this.name = name;
			}

			@Override
			public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
				graphics.centeredText(font, name, LanguageList.this.width / 2, getContentYMiddle() - 9 / 2, -1);
			}

			@Override
			public boolean keyPressed(KeyEvent event) {
				if (event.isSelection()) {
					setSelected(this);
					onDone();
					return true;
				}
				return super.keyPressed(event);
			}

			@Override
			public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
				setSelected(this);
				if (doubleClick) {
					onDone();
				}
				return super.mouseClicked(event, doubleClick);
			}

			@Override
			public Component getNarration() {
				return Component.translatable("narrator.select", name);
			}
		}
	}
}
