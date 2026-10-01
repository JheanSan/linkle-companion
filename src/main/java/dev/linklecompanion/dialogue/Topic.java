package dev.linklecompanion.dialogue;

import java.util.Locale;

/**
 * Everything Linkle can talk about, with her (original) English lines.
 *
 * <p>This enum is the single source of truth: the language-file generator writes these lines to
 * {@code en_us.json}, and the server only needs the number of lines per topic. Translators edit
 * the generated lang file keys {@code dialogue.linkle_companion.<topic>.<n>}.
 */
public enum Topic {
	// ---- important moments: never blocked by the global "don't chatter" gap ----
	GREETING(0, true,
		"Ready when you are! Lead the way.",
		"Two crossbows, one adventure. Let's go!",
		"I'm here! I only got lost twice on the way."),
	RECALL(0, true,
		"Coming, coming! Was I lost? ...Maybe a little.",
		"You called? I was definitely not going the wrong way.",
		"Back at your side!"),
	KNOCKED_OUT(0, true,
		"Ugh... just... need a minute...",
		"Everything's spinning...",
		"Ow. Remind me not to do that again..."),
	REVIVED(0, true,
		"I'm up! Where were we?",
		"Back on my feet! Thanks for waiting.",
		"Okay, round two. I'm ready!"),
	GIFT(100, true,
		"For me? Thank you!",
		"Yum! You're the best.",
		"Food! My favorite kind of present."),
	GIFT_SAVED(100, true,
		"I'm full, but I'll save it for later!",
		"Into the pocket it goes. Thanks!"),
	ARROWS(100, true,
		"More bolts! Now we're talking.",
		"Arrows! You always know what I need."),
	OUT_OF_AMMO(1200, true,
		"I'm out of arrows! Toss me some?",
		"Empty quiver! I need arrows!"),
	LOW_HEALTH(1200, true,
		"Ow! I could use a breather...",
		"That one hurt! Got anything to eat?",
		"I'm okay! ...Mostly okay."),
	OWNER_HURT(1200, true,
		"You're hurt! Fall back, I'll cover you!",
		"Careful! Eat something, I've got your back."),
	VOLLEY(400, true,
		"Spin time!",
		"Back off, all of you!",
		"Twin cyclone!"),

	// ---- chatter: also waits for the global gap so she never spams ----
	NIGHT(2400, false,
		"Sun's going down. Stay close, okay?",
		"Night already? I'll keep my bolts ready.",
		"Monsters love the dark. Good thing I love target practice.",
		"Stars are out. Pretty... but watch the shadows."),
	FIGHT_WON(1200, false,
		"Ha! Did you see that? Two crossbows, zero problems.",
		"Phew. That was a real fight!",
		"All clear! Anyone else want a turn?",
		"Nice teamwork! We make a good pair."),
	EAT(2400, false,
		"Quick snack!",
		"Mmm. Much better."),
	IDLE(12000, false,
		"Have you ever counted how many arrows a skeleton carries?",
		"I'm not lost. I'm exploring with style.",
		"My crossbows are clean, loaded and ready. Just saying.",
		"Snack break soon? Asking for a friend. The friend is me."),
	BIOME_GENERIC(1200, false,
		"Ooh, somewhere new! Which way is north again?",
		"Never been here before. I think."),
	BIOME_FOREST(1200, false,
		"Trees everywhere... perfect for getting lost.",
		"I like it here. Smells like pine and adventure."),
	BIOME_DESERT(1200, false,
		"So hot! My boots are full of sand already.",
		"Sand, sand and more sand."),
	BIOME_SNOWY(1200, false,
		"Brrr! I should have packed a scarf.",
		"Snow! Race you to the next hill!"),
	BIOME_OCEAN(1200, false,
		"That's a LOT of water.",
		"Do crossbows work underwater? Let's not find out."),
	BIOME_BEACH(1200, false,
		"A beach! Five minutes of rest? No? Okay."),
	BIOME_JUNGLE(1200, false,
		"Vines, vines, vines. Watch your step!",
		"It's so loud here! Birds? Bugs? Both?"),
	BIOME_SWAMP(1200, false,
		"Eww, squishy. Don't step in the dark puddles."),
	BIOME_MOUNTAIN(1200, false,
		"What a view! Let's not fall off, though."),
	BIOME_BADLANDS(1200, false,
		"Red rocks everywhere. It's like walking on a sunset."),
	BIOME_MUSHROOM(1200, false,
		"Giant mushrooms?! This place is wonderful."),
	BIOME_CAVE(1200, false,
		"Dark and echoey... hello? Hello? Hello?",
		"Underground. Keep the torches coming!"),
	BIOME_NETHER(1200, false,
		"It's so hot here, even my crossbows are sweating.",
		"Everything here wants to bite us. Stay sharp!"),
	BIOME_END(1200, false,
		"So quiet... and so many eyes watching."),
	BIOME_PLAINS(1200, false,
		"Open fields! Easy to spot trouble from here.");

	/** Minimum ticks between two lines of this topic. */
	public final int cooldownTicks;
	/** Important topics ignore the global gap between lines. */
	public final boolean important;
	private final String[] englishLines;

	Topic(int cooldownTicks, boolean important, String... englishLines) {
		this.cooldownTicks = cooldownTicks;
		this.important = important;
		this.englishLines = englishLines;
	}

	public int lineCount() {
		return englishLines.length;
	}

	public String englishLine(int index) {
		return englishLines[index];
	}

	/** Lang key of line {@code index}, e.g. {@code dialogue.linkle_companion.night.2}. */
	public String key(int index) {
		return "dialogue.linkle_companion." + name().toLowerCase(Locale.ROOT) + "." + (index + 1);
	}
}
