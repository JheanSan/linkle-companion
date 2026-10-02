package dev.linklecompanion.client.screen;

import com.mojang.serialization.Codec;
import dev.linklecompanion.config.LinkleConfig;
import dev.linklecompanion.entity.LinkleVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.CommonComponents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * The in-game settings for Linkle, built from vanilla option widgets so it looks and scrolls like
 * Minecraft's own settings. Every option has a tooltip. Opened from Mod Menu or the Settings button
 * in Linkle's inventory.
 *
 * <p>"Your game" options are this player's own. "Gameplay" options belong to whoever runs the
 * world: they can be changed in single player (and by a LAN host), but are greyed out when you play
 * on someone else's server, because the server's own config file decides there.
 */
public class LinkleSettingsScreen extends OptionsSubScreen {
	private static final String P = "options.linkle_companion.";

	private final List<OptionInstance<?>> gameplayOptions = new ArrayList<>();

	public LinkleSettingsScreen(Screen parent) {
		super(parent, Minecraft.getInstance().options, Component.translatable(P + "title"));
	}

	@Override
	protected void addOptions() {
		LinkleConfig c = LinkleConfig.get();
		gameplayOptions.clear();

		list.addHeader(Component.translatable(P + "section.client"));
		list.addSmall(
			bool("hairEnabled", c.hairEnabled, v -> c.hairEnabled = v),
			bool("hairSway", c.hairSway, v -> c.hairSway = v),
			choice("dialogueDisplay", c.dialogueDisplay, v -> c.dialogueDisplay = v, "hud", "actionbar", "off"),
			choice("dialoguePosition", c.dialoguePosition, v -> c.dialoguePosition = v, "top_left", "top_center", "top_right"),
			slider("dialogueSeconds", 2, 20, c.dialogueSeconds, v -> c.dialogueSeconds = v, "seconds")
		);

		list.addHeader(Component.translatable(P + "section.general"));
		list.addSmall(
			game(bool("enabled", c.enabled, v -> c.enabled = v)),
			game(choice("chattiness", c.chattiness, v -> c.chattiness = v, "normal", "quiet", "chatty")),
			game(choice("defaultVariant", c.defaultVariant, v -> c.defaultVariant = v,
				Arrays.stream(LinkleVariant.values()).map(LinkleVariant::id).toArray(String[]::new))),
			game(bool("onePerPlayer", c.onePerPlayer, v -> c.onePerPlayer = v))
		);

		list.addHeader(Component.translatable(P + "section.movement"));
		list.addSmall(
			game(slider("followStartDistance", 2, 32, (int) c.followStartDistance, v -> c.followStartDistance = v, "blocks")),
			game(slider("followStopDistance", 1, 16, (int) c.followStopDistance, v -> c.followStopDistance = v, "blocks")),
			game(bool("teleportToOwner", c.teleportToOwner, v -> c.teleportToOwner = v)),
			game(slider("teleportDistance", 8, 128, (int) c.teleportDistance, v -> c.teleportDistance = v, "blocks")),
			game(bool("followThroughPortals", c.followThroughPortals, v -> c.followThroughPortals = v)),
			game(slider("guardRadius", 4, 48, (int) c.guardRadius, v -> c.guardRadius = v, "blocks"))
		);

		list.addHeader(Component.translatable(P + "section.combat"));
		list.addSmall(
			game(slider("damagePercent", 0, 300, (int) Math.round(c.damageMultiplier * 100), v -> c.damageMultiplier = v / 100.0, "percent")),
			game(bool("volleyEnabled", c.volleyEnabled, v -> c.volleyEnabled = v)),
			game(slider("volleyCooldownSeconds", 3, 120, c.volleyCooldownSeconds, v -> c.volleyCooldownSeconds = v, "seconds")),
			game(bool("infiniteArrows", c.infiniteArrows, v -> c.infiniteArrows = v)),
			game(slider("startingArrows", 0, 64, Math.min(64, c.startingArrows), v -> c.startingArrows = v, "count")),
			game(bool("pickUpArrows", c.pickUpArrows, v -> c.pickUpArrows = v))
		);

		list.addHeader(Component.translatable(P + "section.health"));
		list.addSmall(
			game(bool("autoEat", c.autoEat, v -> c.autoEat = v)),
			game(bool("realDeath", c.realDeath, v -> c.realDeath = v)),
			game(slider("knockoutSeconds", 5, 300, c.knockoutSeconds, v -> c.knockoutSeconds = v, "seconds"))
		);

		if (isOnOtherServer()) {
			Tooltip locked = Tooltip.create(Component.translatable(P + "server_controlled"));
			for (OptionInstance<?> option : gameplayOptions) {
				AbstractWidget widget = list.findOption(option);
				if (widget != null) {
					widget.active = false;
					widget.setTooltip(locked);
				}
			}
		}
	}

	@Override
	protected void addFooter() {
		LinearLayout footer = layout.addToFooter(LinearLayout.horizontal().spacing(8));
		footer.addChild(Button.builder(Component.translatable(P + "reset"), button -> {
			if (!isOnOtherServer()) {
				LinkleConfig.resetToDefaults();
			} else {
				LinkleConfig.resetClientOptions();
			}
			LinkleConfig.save();
			minecraft.gui.setScreen(new LinkleSettingsScreen(lastScreen));
		}).width(150).build());
		footer.addChild(Button.builder(CommonComponents.GUI_DONE, button -> onClose()).width(150).build());
	}

	@Override
	public void removed() {
		super.removed();
		LinkleConfig.get().validate();
		LinkleConfig.save();
	}

	/** True when connected to a server that this game isn't hosting. */
	private boolean isOnOtherServer() {
		Minecraft minecraft = Minecraft.getInstance();
		return minecraft.level != null && !minecraft.hasSingleplayerServer();
	}

	private <T> OptionInstance<T> game(OptionInstance<T> option) {
		gameplayOptions.add(option);
		return option;
	}

	// ---------------- option builders ----------------

	private static Tooltip tooltip(String key) {
		return Tooltip.create(Component.translatable(P + key + ".tooltip"));
	}

	private static OptionInstance<Boolean> bool(String key, boolean value, Consumer<Boolean> setter) {
		Tooltip tip = tooltip(key);
		return OptionInstance.createBoolean(P + key, v -> tip, value, setter::accept);
	}

	private static OptionInstance<String> choice(String key, String value, Consumer<String> setter, String... values) {
		Tooltip tip = tooltip(key);
		return new OptionInstance<>(P + key, v -> tip,
			// Cycle buttons add "Name: " themselves, so only the value goes here.
			(caption, v) -> Component.translatable(P + key + "." + v),
			new OptionInstance.Enum<>(List.of(values), Codec.STRING), value, setter::accept);
	}

	private static OptionInstance<Integer> slider(String key, int min, int max, int value, IntConsumer setter, String unit) {
		Tooltip tip = tooltip(key);
		return new OptionInstance<>(P + key, v -> tip,
			(caption, v) -> Options.genericValueLabel(caption, Component.translatable(P + "unit." + unit, v)),
			new OptionInstance.IntRange(min, max, true), Math.max(min, Math.min(max, value)), setter::accept);
	}
}
