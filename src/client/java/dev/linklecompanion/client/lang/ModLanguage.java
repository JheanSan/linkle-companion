package dev.linklecompanion.client.lang;

import dev.linklecompanion.LinkleCompanion;
import dev.linklecompanion.client.compat.ClientCompat;
import dev.linklecompanion.config.LinkleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Lets players read Linkle in a language other than the game's ("Mod language" in her settings).
 *
 * <p>Languages are found, not listed: every {@code assets/linkle_companion/lang/*.json} in the jar
 * or in a resource pack shows up in the picker. Adding a translation means adding one JSON file.
 *
 * <p>How it works: after Minecraft loads its language, this wraps it in a thin {@link Language} that
 * answers only this mod's keys from the chosen language and passes every other key through
 * untouched, so the rest of the game keeps its own language. With "Same as game" and a language we
 * ship, nothing is wrapped at all. A game language we don't ship borrows a close one (es_mx uses
 * es_es, pt_pt uses pt_br, zh_hk uses zh_tw...) before falling back to English.
 */
public final class ModLanguage {
	public static final Identifier RELOAD_ID = LinkleCompanion.id("mod_language");
	public static final String AUTO = "auto";

	private static final String LANG_DIR = "lang";
	private static final String DEFAULT = "en_us";
	/** Game languages that should borrow a specific shipped language (checked before the same-prefix rule). */
	private static final Map<String, String> CLOSEST = Map.of(
		"zh_hk", "zh_tw",
		"lzh", "zh_tw"
	);

	/** Language codes that have a Linkle language file, sorted. */
	private static List<String> available = List.of(DEFAULT);
	private static @Nullable String warnedCode;

	private ModLanguage() {
	}

	/** Reload listener body; runs right after Minecraft's own language reload. */
	public static void onReload(ResourceManager resources) {
		available = resources.listResources(LANG_DIR, id -> id.getPath().endsWith(".json")).keySet().stream()
			.filter(id -> id.getNamespace().equals(LinkleCompanion.MOD_ID))
			.map(id -> id.getPath().substring(LANG_DIR.length() + 1, id.getPath().length() - ".json".length()))
			.sorted()
			.toList();
		apply(resources);
	}

	/** Re-applies the configured language now (after the player picks one). */
	public static void apply() {
		apply(Minecraft.getInstance().getResourceManager());
		var player = Minecraft.getInstance().player;
		if (player != null) {
			// Creative search uses item names; vanilla refreshes it the same way on a language change.
			player.connection.updateSearchTrees();
		}
	}

	private static void apply(ResourceManager resources) {
		Language current = Language.getInstance();
		Language base = current instanceof Overlay overlay ? overlay.base : current;
		String gameCode = Minecraft.getInstance().getLanguageManager().getSelected();
		String target = target(gameCode);

		Map<String, String> strings = target == null || target.equals(gameCode) ? null : load(resources, target);
		ClientCompat.installLanguage(strings == null ? base : new Overlay(base, strings));
	}

	/** The shipped language to show, or null for "whatever the game shows" (English if we don't have it). */
	private static @Nullable String target(String gameCode) {
		String wanted = LinkleConfig.get().language;
		if (!AUTO.equals(wanted)) {
			if (available.contains(wanted)) {
				return wanted;
			}
			if (!wanted.equals(warnedCode)) {
				warnedCode = wanted;
				LinkleCompanion.LOGGER.warn("Mod language '{}' has no translation file; following the game language.", wanted);
			}
		}
		return closest(gameCode);
	}

	private static @Nullable String closest(String code) {
		if (available.contains(code)) {
			return code;
		}
		String borrowed = CLOSEST.get(code);
		if (borrowed != null && available.contains(borrowed)) {
			return borrowed;
		}
		int underscore = code.indexOf('_');
		if (underscore > 0) {
			String prefix = code.substring(0, underscore + 1);
			for (String candidate : available) {
				if (candidate.startsWith(prefix)) {
					return candidate;
				}
			}
		}
		return null;
	}

	/** This mod's strings in the given language, on top of English for anything not translated yet. */
	private static @Nullable Map<String, String> load(ResourceManager resources, String code) {
		Map<String, String> strings = new HashMap<>();
		for (String layer : code.equals(DEFAULT) ? List.of(DEFAULT) : List.of(DEFAULT, code)) {
			for (Resource resource : resources.getResourceStack(LinkleCompanion.id(LANG_DIR + "/" + layer + ".json"))) {
				try (InputStream stream = resource.open()) {
					Language.loadFromJson(stream, strings::put);
				} catch (IOException | RuntimeException e) {
					LinkleCompanion.LOGGER.warn("Could not read Linkle translations {} from {}: {}", layer, resource.sourcePackId(), e.getMessage());
				}
			}
		}
		return strings.isEmpty() ? null : Map.copyOf(strings);
	}

	/** Shipped language codes, for the picker. */
	public static List<String> available() {
		return available;
	}

	/** A language's own name, e.g. "Deutsch (Deutschland)"; the code itself if Minecraft doesn't know it. */
	public static Component displayName(String code) {
		LanguageInfo info = Minecraft.getInstance().getLanguageManager().getLanguage(code);
		return info != null ? info.toComponent() : Component.literal(code);
	}

	/** What the settings button shows for the current choice. */
	public static Component currentName() {
		String code = LinkleConfig.get().language;
		return AUTO.equals(code) ? Component.translatable("options.linkle_companion.language.auto") : displayName(code);
	}

	/** Answers this mod's keys from the chosen language and hands everything else to the game's language. */
	private static final class Overlay extends Language {
		private final Language base;
		private final Map<String, String> strings;

		private Overlay(Language base, Map<String, String> strings) {
			this.base = Objects.requireNonNull(base);
			this.strings = strings;
		}

		@Override
		public String getOrDefault(String key, String defaultValue) {
			String value = strings.get(key);
			return value != null ? value : base.getOrDefault(key, defaultValue);
		}

		@Override
		public boolean has(String key) {
			return strings.containsKey(key) || base.has(key);
		}

		@Override
		public boolean isDefaultRightToLeft() {
			return base.isDefaultRightToLeft();
		}

		@Override
		public FormattedCharSequence getVisualOrder(FormattedText text) {
			return base.getVisualOrder(text);
		}
	}
}
