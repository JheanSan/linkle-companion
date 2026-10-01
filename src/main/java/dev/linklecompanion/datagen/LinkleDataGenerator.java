package dev.linklecompanion.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

/**
 * Data generation entry point ({@code ./gradlew runDatagen}). Writes recipes, the English language
 * file, tags and advancements into {@code src/main/generated}. Only runs during datagen, never in game.
 */
public class LinkleDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		FabricDataGenerator.Pack pack = generator.createPack();
		pack.addProvider(ModRecipeProvider::new);
		pack.addProvider(ModLanguageProvider::new);
		pack.addProvider(ModEntityTypeTagProvider::new);
		pack.addProvider(ModAdvancementProvider::new);
	}
}
