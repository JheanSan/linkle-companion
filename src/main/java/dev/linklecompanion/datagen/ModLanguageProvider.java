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

		// Advancements
		builder.add("advancements.linkle_companion.root.title", "Linkle Companion");
		builder.add("advancements.linkle_companion.root.description", "Craft a Wanderer's Compass");
		builder.add("advancements.linkle_companion.friend.title", "A Friend in Green");
		builder.add("advancements.linkle_companion.friend.description", "Summon or befriend Linkle");
	}
}
