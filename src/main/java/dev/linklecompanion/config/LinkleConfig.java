package dev.linklecompanion.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import dev.linklecompanion.LinkleCompanion;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The small JSON config at {@code config/linkle_companion.json}.
 *
 * <p>Every field has a safe default, so players never have to edit it. Broken or out-of-range
 * values are logged once and replaced by defaults instead of crashing the game.
 *
 * <p>Server-side options are read by the server (or the single-player integrated server).
 * Client-side options (hair, dialogue display) are read by each player's own game.
 */
public final class LinkleConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = LinkleCompanion.MOD_ID + ".json";

	private static LinkleConfig instance = new LinkleConfig();

	// ---------------- server / gameplay ----------------

	/** Linkle starts walking toward you when she is farther than this (blocks). */
	public double followStartDistance = 6.0;
	/** Linkle stops walking once she is this close to you (blocks). */
	public double followStopDistance = 3.0;
	/** Linkle teleports to you when she is farther than this (blocks). */
	public double teleportDistance = 16.0;
	/** Multiplier for the damage of Linkle's bolts. 1.0 = like a normal crossbow. */
	public double damageMultiplier = 1.0;
	/** If true, Linkle never runs out of arrows (and her bolts can't be picked up). */
	public boolean infiniteArrows = false;
	/** How many arrows a freshly summoned Linkle brings with her. */
	public int startingArrows = 32;
	/** Seconds between two signature volleys. */
	public int volleyCooldownSeconds = 20;
	/** If true, Linkle really dies instead of being knocked out. */
	public boolean realDeath = false;
	/** Seconds Linkle stays knocked out before getting back up on her own. */
	public int knockoutSeconds = 60;
	/** Radius (blocks) Linkle defends around her guard point in Guard mode. */
	public double guardRadius = 12.0;
	/**
	 * If true (default), each player has one Linkle: summoning again calls the existing one back,
	 * and an old copy left somewhere else leaves when the new one is summoned.
	 * If false, every summon creates another Linkle; commands control the newest one.
	 */
	public boolean onePerPlayer = true;
	/** Skin variant for newly summoned Linkles: classic, crimson, azure, violet, snow or custom. */
	public String defaultVariant = "classic";

	// ---------------- client / looks ----------------

	/** Show the twin-tail hair layer. */
	public boolean hairEnabled = true;
	/** Let the twin tails sway with movement. */
	public boolean hairSway = true;
	/** Where Linkle's lines appear: "hud" (small box), "actionbar" or "off". */
	public String dialogueDisplay = "hud";

	public static LinkleConfig get() {
		return instance;
	}

	public static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
	}

	/** Loads the config (creating it with defaults if missing) and writes back a cleaned-up copy. */
	public static void load() {
		Path path = path();
		LinkleConfig loaded = null;
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				loaded = GSON.fromJson(reader, LinkleConfig.class);
			} catch (IOException | JsonParseException e) {
				LinkleCompanion.LOGGER.warn("Could not read {} ({}); using default settings.", path, e.getMessage());
			}
		}

		instance = loaded == null ? new LinkleConfig() : loaded;
		instance.validate();
		save();
	}

	public static void save() {
		Path path = path();
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				GSON.toJson(instance, writer);
			}
		} catch (IOException e) {
			LinkleCompanion.LOGGER.warn("Could not write {}: {}", path, e.getMessage());
		}
	}

	/** Clamps every value into a sane range so a typo can't break gameplay. */
	void validate() {
		followStopDistance = clamp(followStopDistance, 1.0, 16.0, 3.0, "followStopDistance");
		followStartDistance = clamp(followStartDistance, followStopDistance + 1.0, 32.0, Math.max(6.0, followStopDistance + 1.0), "followStartDistance");
		teleportDistance = clamp(teleportDistance, followStartDistance + 2.0, 128.0, Math.max(16.0, followStartDistance + 2.0), "teleportDistance");
		damageMultiplier = clamp(damageMultiplier, 0.0, 10.0, 1.0, "damageMultiplier");
		startingArrows = (int) clamp(startingArrows, 0, 64 * 9, 32, "startingArrows");
		volleyCooldownSeconds = (int) clamp(volleyCooldownSeconds, 3, 3600, 20, "volleyCooldownSeconds");
		knockoutSeconds = (int) clamp(knockoutSeconds, 5, 3600, 60, "knockoutSeconds");
		guardRadius = clamp(guardRadius, 4.0, 48.0, 12.0, "guardRadius");
		if (defaultVariant == null || !dev.linklecompanion.entity.LinkleVariant.isKnown(defaultVariant)) {
			LinkleCompanion.LOGGER.warn("Config: unknown defaultVariant '{}', using 'classic'.", defaultVariant);
			defaultVariant = "classic";
		}
		if (dialogueDisplay == null || !(dialogueDisplay.equals("hud") || dialogueDisplay.equals("actionbar") || dialogueDisplay.equals("off"))) {
			LinkleCompanion.LOGGER.warn("Config: dialogueDisplay must be hud, actionbar or off; using 'hud'.");
			dialogueDisplay = "hud";
		}
	}

	private static double clamp(double value, double min, double max, double fallback, String name) {
		if (Double.isNaN(value) || value < min || value > max) {
			LinkleCompanion.LOGGER.warn("Config: {} = {} is outside {}..{}; using {}.", name, value, min, max, fallback);
			return fallback;
		}
		return value;
	}
}
