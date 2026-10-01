package dev.linklecompanion.datagen;

import dev.linklecompanion.registry.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

/**
 * The Wanderer's Compass recipe. Uses common (c:) tags so modded crossbows, gold and dyes work.
 * <pre>
 *  . D .      D = green dye (#c:dyes/green)
 *  C M C      C = crossbow (#c:tools/crossbow), M = compass
 *  . G .      G = gold ingot (#c:ingots/gold)
 * </pre>
 * Unlocked in the recipe book as soon as you hold a compass or a crossbow.
 */
public class ModRecipeProvider extends FabricRecipeProvider {
	public ModRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, BootstrapContext<Recipe<?>> recipes,
			BootstrapContext<Advancement> advancements) {
		return new RecipeProvider(recipes, advancements) {
			@Override
			public void buildRecipes() {
				shaped(RecipeCategory.TOOLS, ModItems.WANDERERS_COMPASS)
					.pattern(" D ")
					.pattern("CMC")
					.pattern(" G ")
					.define('D', ConventionalItemTags.GREEN_DYES)
					.define('C', ConventionalItemTags.CROSSBOW_TOOLS)
					.define('M', Items.COMPASS)
					.define('G', ConventionalItemTags.GOLD_INGOTS)
					.unlockedBy(getHasName(Items.COMPASS), has(Items.COMPASS))
					.unlockedBy(getHasName(Items.CROSSBOW), has(Items.CROSSBOW))
					.save(this.output);
			}
		};
	}

	@Override
	public String getName() {
		return "Linkle Companion recipes";
	}
}
