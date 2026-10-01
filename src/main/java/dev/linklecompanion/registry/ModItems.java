package dev.linklecompanion.registry;

import dev.linklecompanion.LinkleCompanion;
import dev.linklecompanion.item.WanderersCompassItem;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;

public final class ModItems {
	public static final ResourceKey<Item> WANDERERS_COMPASS_KEY = ResourceKey.create(Registries.ITEM, LinkleCompanion.id("wanderers_compass"));

	public static final Item WANDERERS_COMPASS = Registry.register(
		BuiltInRegistries.ITEM,
		WANDERERS_COMPASS_KEY,
		new WanderersCompassItem(new Item.Properties().setId(WANDERERS_COMPASS_KEY).stacksTo(1).rarity(Rarity.UNCOMMON))
	);

	private ModItems() {
	}

	public static void register() {
		// Add to the vanilla Tools & Utilities tab, right after the recovery compass.
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
			.register(output -> output.insertAfter(Items.RECOVERY_COMPASS, WANDERERS_COMPASS));
	}
}
