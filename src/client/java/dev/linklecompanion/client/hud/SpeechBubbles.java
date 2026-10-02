package dev.linklecompanion.client.hud;

import dev.linklecompanion.config.LinkleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lines Linkles are currently saying, by entity id. The renderer draws them above her head like a
 * speech bubble. Lines are wrapped once when they arrive, so drawing allocates nothing.
 */
public final class SpeechBubbles {
	private static final int MAX_WIDTH = 140;

	private record Bubble(List<Component> lines, long until) {
	}

	private static final Map<Integer, Bubble> BUBBLES = new HashMap<>();
	/** True while a screen draws a preview of her (e.g. her inventory): no bubble there. */
	private static boolean suppressed;

	private SpeechBubbles() {
	}

	public static void show(int entityId, Component text) {
		long until = Util.getMillis() + LinkleConfig.get().dialogueSeconds * 1000L;
		BUBBLES.put(entityId, new Bubble(wrap(text.getString()), until));
	}

	/** The wrapped lines this Linkle is saying right now, or null. */
	public static @Nullable List<Component> get(int entityId) {
		if (suppressed) {
			return null;
		}
		Bubble bubble = BUBBLES.get(entityId);
		if (bubble == null) {
			return null;
		}
		if (Util.getMillis() > bubble.until) {
			BUBBLES.remove(entityId);
			return null;
		}
		return bubble.lines;
	}

	/** Runs a GUI preview render without speech bubbles. */
	public static void withoutBubbles(Runnable render) {
		suppressed = true;
		try {
			render.run();
		} finally {
			suppressed = false;
		}
	}

	public static void clear() {
		BUBBLES.clear();
	}

	/** Word-wraps a line to the bubble width. */
	private static List<Component> wrap(String text) {
		Font font = Minecraft.getInstance().font;
		List<Component> lines = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		for (String word : text.split(" ")) {
			String candidate = current.isEmpty() ? word : current + " " + word;
			if (!current.isEmpty() && font.width(candidate) > MAX_WIDTH) {
				lines.add(Component.literal(current.toString()));
				current = new StringBuilder(word);
			} else {
				current = new StringBuilder(candidate);
			}
		}
		if (!current.isEmpty()) {
			lines.add(Component.literal(current.toString()));
		}
		return List.copyOf(lines);
	}
}
