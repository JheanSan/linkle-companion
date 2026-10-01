package dev.linklecompanion.datagen;

import dev.linklecompanion.LinkleCompanion;
import dev.linklecompanion.registry.ModEntities;
import dev.linklecompanion.registry.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.advancements.triggers.SummonedEntityTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/** A small advancement tab of its own, so vanilla tabs stay untouched. */
public class ModAdvancementProvider extends FabricAdvancementProvider {
	public ModAdvancementProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	public void generateAdvancement(HolderLookup.Provider registries, Consumer<AdvancementHolder> consumer) {
		AdvancementHolder root = Advancement.Builder.advancement()
			.rootDisplay(ModItems.WANDERERS_COMPASS,
				Component.translatable("advancements.linkle_companion.root.title"),
				Component.translatable("advancements.linkle_companion.root.description"),
				Identifier.withDefaultNamespace("gui/advancements/backgrounds/adventure"),
				AdvancementType.TASK, true, false, false)
			.addCriterion("has_compass", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.WANDERERS_COMPASS))
			.build(LinkleCompanion.id("root"));
		consumer.accept(root);

		AdvancementHolder friend = Advancement.Builder.advancement()
			.parent(root)
			.display(ModItems.WANDERERS_COMPASS,
				Component.translatable("advancements.linkle_companion.friend.title"),
				Component.translatable("advancements.linkle_companion.friend.description"),
				AdvancementType.GOAL, true, true, false)
			.addCriterion("summoned_linkle", SummonedEntityTrigger.TriggerInstance.summonedEntity(
				EntityPredicate.Builder.entity().of(registries.lookupOrThrow(Registries.ENTITY_TYPE), ModEntities.LINKLE)))
			.build(LinkleCompanion.id("friend_in_green"));
		consumer.accept(friend);
	}
}
