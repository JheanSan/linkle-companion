package dev.linklecompanion.entity;

/** What Linkle is doing when nothing is attacking. Cycled by right-clicking her. */
public enum LinkleMode {
	/** Follows the owner and defends them. */
	FOLLOW,
	/** Sits down and waits. Only shoots back if something hurts her. */
	STAY,
	/** Holds her position and defends the area around it. */
	GUARD;

	public LinkleMode next() {
		return values()[(ordinal() + 1) % values().length];
	}

	public String id() {
		return name().toLowerCase(java.util.Locale.ROOT);
	}

	public static LinkleMode byOrdinal(int ordinal) {
		LinkleMode[] values = values();
		return ordinal >= 0 && ordinal < values.length ? values[ordinal] : FOLLOW;
	}

	public static LinkleMode byId(String id) {
		for (LinkleMode mode : values()) {
			if (mode.id().equalsIgnoreCase(id)) {
				return mode;
			}
		}
		return null;
	}
}
