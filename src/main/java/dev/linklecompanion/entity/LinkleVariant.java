package dev.linklecompanion.entity;

import java.util.Locale;

/**
 * Linkle's skins. Each one is a texture at
 * {@code assets/linkle_companion/textures/entity/linkle/<id>.png}, so a resource pack can replace
 * any of them. {@link #CUSTOM} ships as a copy of {@link #CLASSIC} and exists only so players can
 * drop their own skin in without losing the built-in looks.
 */
public enum LinkleVariant {
	CLASSIC("classic"),
	CRIMSON("crimson"),
	AZURE("azure"),
	VIOLET("violet"),
	SNOW("snow"),
	CUSTOM("custom");

	private final String id;

	LinkleVariant(String id) {
		this.id = id;
	}

	public String id() {
		return id;
	}

	public static boolean isKnown(String id) {
		return byIdOrNull(id) != null;
	}

	/** Returns the variant with this id, or {@link #CLASSIC} if unknown. */
	public static LinkleVariant byId(String id) {
		LinkleVariant variant = byIdOrNull(id);
		return variant == null ? CLASSIC : variant;
	}

	public static LinkleVariant byIdOrNull(String id) {
		if (id == null) {
			return null;
		}
		String wanted = id.trim().toLowerCase(Locale.ROOT);
		for (LinkleVariant variant : values()) {
			if (variant.id.equals(wanted)) {
				return variant;
			}
		}
		return null;
	}

	/**
	 * Name-tag selection: if the last word of the new name is a variant id ("Azure", "Linkle Snow"),
	 * returns that variant, otherwise null (the name is kept either way).
	 */
	public static LinkleVariant fromName(String name) {
		if (name == null || name.isBlank()) {
			return null;
		}
		String trimmed = name.trim();
		int space = trimmed.lastIndexOf(' ');
		return byIdOrNull(space < 0 ? trimmed : trimmed.substring(space + 1));
	}
}
