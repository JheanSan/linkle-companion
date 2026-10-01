package dev.linklecompanion.client.hud;

import dev.linklecompanion.LinkleCompanion;
import dev.linklecompanion.config.LinkleConfig;
import dev.linklecompanion.entity.LinkleVariant;
import dev.linklecompanion.network.DialoguePayload;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;

import java.util.List;

/**
 * The small speech box in the top-left corner: Linkle's face (cut from her own skin, so resource
 * pack skins show up too), her name and the line. Fades in and out; one line at a time.
 * Players can switch to the action bar or turn lines off with {@code dialogueDisplay} in the config.
 */
public final class DialogueHud implements HudElement {
	public static final Identifier ID = LinkleCompanion.id("dialogue");

	private static final long SHOW_MILLIS = 5500;
	private static final long FADE_MILLIS = 600;
	private static final int MAX_TEXT_WIDTH = 170;
	private static final int FACE_SIZE = 24;
	private static final int NAME_COLOR = 0x7CD65C;
	private static final int BORDER_COLOR = 0x4E9A3C;

	private static Component line;
	private static List<FormattedCharSequence> wrapped = List.of();
	private static int textWidth;
	private static Identifier face = skinFor(LinkleVariant.CLASSIC);
	private static long shownAt;

	/** Called when the server says Linkle spoke. */
	public static void show(DialoguePayload payload) {
		String mode = LinkleConfig.get().dialogueDisplay;
		Component text = Component.translatable(payload.key());
		if ("off".equals(mode)) {
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		if ("actionbar".equals(mode)) {
			minecraft.gui.hud.setOverlayMessage(Component.translatable("hud.linkle_companion.says", text), false);
			return;
		}
		line = text;
		face = skinFor(LinkleVariant.byId(payload.variant()));
		Font font = minecraft.font;
		wrapped = font.split(text, MAX_TEXT_WIDTH);
		textWidth = Math.max(font.width(Component.translatable("hud.linkle_companion.name")), wrapped.stream().mapToInt(font::width).max().orElse(0));
		shownAt = Util.getMillis();
	}

	private static Identifier skinFor(LinkleVariant variant) {
		return LinkleCompanion.id("textures/entity/linkle/" + variant.id() + ".png");
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		if (line == null) {
			return;
		}
		long age = Util.getMillis() - shownAt;
		if (age > SHOW_MILLIS) {
			line = null;
			return;
		}
		float alpha = age < 200 ? age / 200.0F : age > SHOW_MILLIS - FADE_MILLIS ? (SHOW_MILLIS - age) / (float) FADE_MILLIS : 1.0F;
		alpha = Math.max(0.05F, Math.min(1.0F, alpha));

		Font font = Minecraft.getInstance().font;
		int x = 6;
		int y = 6;
		int width = FACE_SIZE + 14 + textWidth;
		int height = Math.max(FACE_SIZE + 8, 18 + wrapped.size() * font.lineHeight);

		graphics.fill(x, y, x + width, y + height, ARGB.color((int) (alpha * 180), 12, 20, 12));
		graphics.outline(x, y, width, height, ARGB.color(alpha, BORDER_COLOR));

		// Face (8x8 at 8,8) and hat layer (8x8 at 40,8) from her skin, drawn 3x.
		int faceColor = ARGB.color(alpha, 0xFFFFFF);
		graphics.blit(RenderPipelines.GUI_TEXTURED, face, x + 4, y + 4, 8.0F, 8.0F, FACE_SIZE, FACE_SIZE, 8, 8, 64, 64, faceColor);
		graphics.blit(RenderPipelines.GUI_TEXTURED, face, x + 4, y + 4, 40.0F, 8.0F, FACE_SIZE, FACE_SIZE, 8, 8, 64, 64, faceColor);

		int textX = x + FACE_SIZE + 10;
		graphics.text(font, Component.translatable("hud.linkle_companion.name"), textX, y + 5, ARGB.color(alpha, NAME_COLOR), true);
		for (int i = 0; i < wrapped.size(); i++) {
			graphics.text(font, wrapped.get(i), textX, y + 16 + i * font.lineHeight, ARGB.color(alpha, 0xFFFFFF), false);
		}
	}
}
