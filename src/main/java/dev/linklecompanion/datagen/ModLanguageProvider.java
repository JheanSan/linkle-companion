package dev.linklecompanion.datagen;

import dev.linklecompanion.dialogue.Topic;
import dev.linklecompanion.registry.ModEntities;
import dev.linklecompanion.registry.ModItems;
import dev.linklecompanion.registry.ModTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

/**
 * English text for everything the mod shows. Translators: copy the generated
 * {@code assets/linkle_companion/lang/en_us.json} to your language code and translate the values.
 */
public class ModLanguageProvider extends FabricLanguageProvider {
	public ModLanguageProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, "en_us", registries);
	}

	@Override
	public void generateTranslations(HolderLookup.Provider registries, TranslationBuilder builder) {
		builder.add(ModEntities.LINKLE, "Linkle");
		builder.add(ModItems.WANDERERS_COMPASS, "Wanderer's Compass");
		builder.add(ModTags.NEVER_TARGET, "Never targeted by Linkle");
		builder.add(ModTags.IGNORED_TARGETS, "Ignored by Linkle");

		// Modes
		builder.add("mode.linkle_companion.follow", "Follow");
		builder.add("mode.linkle_companion.stay", "Stay");
		builder.add("mode.linkle_companion.guard", "Guard");
		builder.add("message.linkle_companion.mode.follow", "Linkle: Right behind you! (Follow)");
		builder.add("message.linkle_companion.mode.stay", "Linkle: I'll wait right here. (Stay)");
		builder.add("message.linkle_companion.mode.guard", "Linkle: Nobody gets past me! (Guard)");

		// Messages
		builder.add("message.linkle_companion.summoned", "Linkle has joined you!");
		builder.add("message.linkle_companion.recalled", "Linkle is back at your side.");
		builder.add("message.linkle_companion.failed", "Linkle could not be summoned here.");
		builder.add("message.linkle_companion.elsewhere", "Your Linkle is somewhere far away (her area isn't loaded). Sneak and use the Wanderer's Compass to call a new Linkle; the old one will leave.");
		builder.add("message.linkle_companion.none", "You don't have a Linkle yet. Craft a Wanderer's Compass to summon her.");
		builder.add("message.linkle_companion.already_have", "You already have a Linkle.");
		builder.add("message.linkle_companion.not_yours", "This Linkle travels with someone else.");
		builder.add("message.linkle_companion.knocked_out_hint", "Linkle is knocked out. Give her some food to wake her up!");
		builder.add("message.linkle_companion.inventory_full", "Linkle's pockets are full.");
		builder.add("message.linkle_companion.unknown_mode", "Unknown mode. Use follow, stay or guard.");
		builder.add("message.linkle_companion.unknown_skin", "Unknown skin. Use classic, crimson, azure, violet, snow or custom.");
		builder.add("message.linkle_companion.skin_set", "Linkle's skin is now: %s");
		builder.add("message.linkle_companion.info", "Linkle - mode: %s, health: %s/%s, arrows: %s, at %s %s %s in %s, skin: %s");
		builder.add("message.linkle_companion.info_knocked_out", "She is knocked out and gets up in %s seconds (or feed her).");
		builder.add("message.linkle_companion.dismissed", "Linkle waves goodbye. Her items were dropped where she stood.");

		// Inventory screen
		builder.add("gui.linkle_companion.health", "HP %s");
		builder.add("gui.linkle_companion.arrows", "Arrows %s");
		builder.add("gui.linkle_companion.arrows_infinite", "Arrows ∞");

		// Dialogue box
		builder.add("hud.linkle_companion.name", "Linkle");
		builder.add("hud.linkle_companion.says", "Linkle: %s");
		for (Topic topic : Topic.values()) {
			for (int i = 0; i < topic.lineCount(); i++) {
				builder.add(topic.key(i), topic.englishLine(i));
			}
		}

		// Messages added with the settings screen and hotkeys
		builder.add("message.linkle_companion.summon_needs_cheats", "/linkle summon is a cheat command (needs operator / Allow Commands). In survival, craft a Wanderer's Compass instead: green dye on top, crossbow - compass - crossbow, gold ingot below. In single player: Esc > Open to LAN > Allow Commands: ON.");
		builder.add("message.linkle_companion.disabled", "Linkle is turned off in the Linkle Companion settings.");
		builder.add("message.linkle_companion.no_loaded", "Your Linkle isn't nearby (or isn't loaded). Use the Wanderer's Compass to summon her.");

		// Hotkeys (Options > Controls > Key Binds)
		builder.add("key.category.linkle_companion.keys", "Linkle Companion");
		builder.add("key.linkle_companion.recall", "Call Linkle to you");
		builder.add("key.linkle_companion.mode", "Switch Linkle's mode");
		builder.add("key.linkle_companion.settings", "Open Linkle settings");

		// Settings screen
		String o = "options.linkle_companion.";
		builder.add(o + "title", "Linkle Companion Settings");
		builder.add(o + "reset", "Reset to defaults");
		builder.add(o + "server_controlled", "Set by the server you're playing on. The server owner can change it in config/linkle_companion.json.");
		builder.add(o + "section.client", "Your game: looks and dialogue");
		builder.add(o + "section.general", "General");
		builder.add(o + "section.movement", "Following");
		builder.add(o + "section.combat", "Combat");
		builder.add(o + "section.health", "Health");
		builder.add(o + "unit.seconds", "%s s");
		builder.add(o + "unit.blocks", "%s blocks");
		builder.add(o + "unit.percent", "%s%%");
		builder.add(o + "unit.count", "%s");
		option(builder, "hairEnabled", "Twin braids", "Show Linkle's twin braids. Turn off if a custom skin looks odd with them.");
		option(builder, "hairSway", "Braid sway", "Let the braids swing as she walks and spins.");
		option(builder, "dialogueDisplay", "Her lines", "Where Linkle's lines appear: a small box with her face, the action bar above your hotbar, or nowhere.");
		builder.add(o + "dialogueDisplay.hud", "Speech box");
		builder.add(o + "dialogueDisplay.actionbar", "Action bar");
		builder.add(o + "dialogueDisplay.off", "Off");
		option(builder, "dialoguePosition", "Box position", "Where the speech box sits on your screen.");
		builder.add(o + "dialoguePosition.top_left", "Top left");
		builder.add(o + "dialoguePosition.top_center", "Top center");
		builder.add(o + "dialoguePosition.top_right", "Top right");
		option(builder, "dialogueSeconds", "Box time", "How long each line stays in the speech box.");
		option(builder, "enabled", "Linkle enabled", "Master switch. Off: Linkle can't be summoned, and an existing Linkle sits down and pauses (no fighting, no talking). Turn it back on and she carries on.");
		option(builder, "chattiness", "Chattiness", "How often she talks. Quiet: only important moments (knocked out, low health, gifts...). Chatty: twice as often.");
		builder.add(o + "chattiness.normal", "Normal");
		builder.add(o + "chattiness.quiet", "Quiet");
		builder.add(o + "chattiness.chatty", "Chatty");
		option(builder, "defaultVariant", "Default outfit", "The outfit of newly summoned Linkles. You can still change it later with a name tag or /linkle skin.");
		for (var variant : dev.linklecompanion.entity.LinkleVariant.values()) {
			String id = variant.id();
			builder.add(o + "defaultVariant." + id, Character.toUpperCase(id.charAt(0)) + id.substring(1));
		}
		option(builder, "onePerPlayer", "One per player", "On: each player has one Linkle (summoning again calls her back). Off: every summon creates another Linkle.");
		option(builder, "followStartDistance", "Start following at", "She starts walking toward you when you get farther than this.");
		option(builder, "followStopDistance", "Stop following at", "She stops when she is this close to you.");
		option(builder, "teleportToOwner", "Teleport when far", "Teleport to you when she falls far behind (Follow mode).");
		option(builder, "teleportDistance", "Teleport distance", "How far behind she can fall before teleporting to you.");
		option(builder, "followThroughPortals", "Follow through portals", "Go through Nether and End portals with you (Follow mode).");
		option(builder, "guardRadius", "Guard radius", "In Guard mode she defends this area around her spot.");
		option(builder, "damagePercent", "Bolt damage", "Damage of her bolts. 100% is like a normal crossbow.");
		option(builder, "volleyEnabled", "Twin Cyclone", "Her spinning volley when enemies crowd her.");
		option(builder, "volleyCooldownSeconds", "Cyclone cooldown", "Time between two Twin Cyclone volleys.");
		option(builder, "infiniteArrows", "Infinite arrows", "She never runs out of arrows (her bolts can't be picked up).");
		option(builder, "startingArrows", "Starting arrows", "Arrows a newly summoned Linkle brings with her.");
		option(builder, "pickUpArrows", "Pick up arrows", "She collects arrow items lying right next to her (needs the mobGriefing game rule).");
		option(builder, "autoEat", "Eat when hurt", "She eats food from her pockets when her health is low.");
		option(builder, "realDeath", "Real death", "Off: she gets knocked out instead of dying. On: she can really die (her items drop).");
		option(builder, "knockoutSeconds", "Knockout time", "How long she stays knocked out before getting up on her own. Food wakes her right away.");

		// Advancements
		builder.add("advancements.linkle_companion.root.title", "Linkle Companion");
		builder.add("advancements.linkle_companion.root.description", "Craft a Wanderer's Compass");
		builder.add("advancements.linkle_companion.friend.title", "A Friend in Green");
		builder.add("advancements.linkle_companion.friend.description", "Summon or befriend Linkle");
	}

	/** A settings option: its name and its tooltip. */
	private static void option(TranslationBuilder builder, String key, String name, String tooltip) {
		builder.add("options.linkle_companion." + key, name);
		builder.add("options.linkle_companion." + key + ".tooltip", tooltip);
	}
}
