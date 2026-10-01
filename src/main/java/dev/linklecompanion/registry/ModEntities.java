package dev.linklecompanion.registry;

import dev.linklecompanion.LinkleCompanion;
import dev.linklecompanion.entity.LinkleEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
	public static final ResourceKey<EntityType<?>> LINKLE_KEY = ResourceKey.create(Registries.ENTITY_TYPE, LinkleCompanion.id("linkle"));

	/**
	 * Linkle herself. MISC category (like villagers) so she never counts toward natural mob caps.
	 * Slightly smaller than a player; the renderer scales the player model to match.
	 */
	public static final EntityType<LinkleEntity> LINKLE = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		LINKLE_KEY,
		EntityType.Builder.of(LinkleEntity::new, MobCategory.MISC)
			.sized(0.6F, 1.75F)
			.eyeHeight(1.55F)
			.clientTrackingRange(10)
			.build(LINKLE_KEY)
	);

	private ModEntities() {
	}

	public static void register() {
		FabricDefaultAttributeRegistry.register(LINKLE, LinkleEntity.createAttributes());
	}
}
