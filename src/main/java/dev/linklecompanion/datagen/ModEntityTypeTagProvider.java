package dev.linklecompanion.datagen;

import dev.linklecompanion.registry.ModTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.EntityTypeIds;

import java.util.concurrent.CompletableFuture;

/** Generates this mod's own entity tags. It never touches vanilla tags. */
public class ModEntityTypeTagProvider extends FabricTagsProvider.EntityTypeTagsProvider {
	public ModEntityTypeTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected void addTags(HolderLookup.Provider registries) {
		builder(ModTags.NEVER_TARGET)
			.add(EntityTypeIds.VILLAGER)
			.add(EntityTypeIds.WANDERING_TRADER)
			.add(EntityTypeIds.IRON_GOLEM)
			.add(EntityTypeIds.SNOW_GOLEM)
			.add(EntityTypeIds.ALLAY)
			.add(EntityTypeIds.COPPER_GOLEM);
		builder(ModTags.IGNORED_TARGETS)
			.add(EntityTypeIds.ENDERMAN)
			.add(EntityTypeIds.BREEZE)
			.add(EntityTypeIds.CREAKING);
	}
}
