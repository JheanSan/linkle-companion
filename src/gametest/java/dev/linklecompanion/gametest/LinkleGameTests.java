package dev.linklecompanion.gametest;

import dev.linklecompanion.config.LinkleConfig;
import dev.linklecompanion.entity.LinkleEntity;
import dev.linklecompanion.entity.LinkleMode;
import dev.linklecompanion.entity.LinkleSummoning;
import dev.linklecompanion.registry.ModAttachments;
import dev.linklecompanion.registry.ModEntities;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Method;

/**
 * Server game tests, run automatically by {@code ./gradlew build}.
 * Each test gets a fresh stone floor so Linkle and the mock player have ground to stand on.
 */
public class LinkleGameTests implements CustomTestMethodInvoker {
	private static final int FLOOR = 24;

	@Override
	public void invokeTestMethod(GameTestHelper helper, Method method) throws ReflectiveOperationException {
		for (int x = -FLOOR / 2; x < FLOOR; x++) {
			for (int z = -FLOOR / 2; z < FLOOR + 8; z++) {
				helper.setBlock(x, 0, z, Blocks.STONE);
			}
		}
		method.invoke(this, helper);
	}

	private static ServerPlayer player(GameTestHelper helper, double x, double z) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		Vec3 pos = helper.absoluteVec(new Vec3(x, 1, z));
		player.snapTo(pos.x, pos.y, pos.z);
		return player;
	}

	private static LinkleEntity linkleFor(GameTestHelper helper, ServerPlayer owner, int x, int z) {
		LinkleEntity linkle = helper.spawn(ModEntities.LINKLE, new BlockPos(x, 1, z));
		linkle.setupNew(owner, 1);
		owner.setAttached(ModAttachments.COMPANION, new ModAttachments.CompanionData(linkle.getUUID(), 1));
		return linkle;
	}

	/** Ownership is set, survives a save/load round trip, and so do mode, inventory and skin. */
	@GameTest
	public void ownershipIsSaved(GameTestHelper helper) {
		ServerPlayer owner = player(helper, 2, 2);
		LinkleEntity linkle = linkleFor(helper, owner, 3, 3);
		linkle.setMode(LinkleMode.GUARD);
		helper.assertTrue(linkle.isTame(), "Linkle should be tame");
		helper.assertTrue(linkle.isOwnedByPlayer(owner), "Linkle should belong to the player");
		helper.assertTrue(linkle.hasAmmo(), "A new Linkle brings arrows");

		ServerLevel level = helper.getLevel();
		TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		linkle.saveWithoutId(output);
		CompoundTag saved = output.buildResult();

		LinkleEntity copy = ModEntities.LINKLE.create(level, EntitySpawnReason.LOAD);
		helper.assertTrue(copy != null, "Could not create a Linkle");
		copy.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
		helper.assertTrue(copy.isOwnedByPlayer(owner), "Owner must survive save/load");
		helper.assertTrue(copy.getMode() == LinkleMode.GUARD, "Mode must survive save/load");
		helper.assertTrue(copy.getGuardPos() != null, "Guard point must survive save/load");
		helper.assertTrue(copy.hasAmmo(), "Inventory must survive save/load");
		helper.assertTrue(copy.getVariant() == linkle.getVariant(), "Skin must survive save/load");
		helper.succeed();
	}

	/** Right-click cycles Follow -> Stay -> Guard -> Follow, with sitting and guard point set correctly. */
	@GameTest
	public void rightClickCyclesModes(GameTestHelper helper) {
		ServerPlayer owner = player(helper, 2, 2);
		LinkleEntity linkle = linkleFor(helper, owner, 3, 3);
		owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		helper.assertTrue(linkle.getMode() == LinkleMode.FOLLOW, "Starts in Follow");

		linkle.mobInteract(owner, InteractionHand.MAIN_HAND);
		helper.assertTrue(linkle.getMode() == LinkleMode.STAY, "Second mode is Stay");
		helper.assertTrue(linkle.isOrderedToSit(), "Stay makes her sit");

		linkle.mobInteract(owner, InteractionHand.MAIN_HAND);
		helper.assertTrue(linkle.getMode() == LinkleMode.GUARD, "Third mode is Guard");
		helper.assertFalse(linkle.isOrderedToSit(), "Guard is standing");
		helper.assertTrue(linkle.getGuardPos() != null, "Guard sets a guard point");

		linkle.mobInteract(owner, InteractionHand.MAIN_HAND);
		helper.assertTrue(linkle.getMode() == LinkleMode.FOLLOW, "Back to Follow");
		helper.assertTrue(linkle.getGuardPos() == null, "Follow clears the guard point");

		ServerPlayer stranger = player(helper, 4, 4);
		linkle.mobInteract(stranger, InteractionHand.MAIN_HAND);
		helper.assertTrue(linkle.getMode() == LinkleMode.FOLLOW, "Someone else can't change her mode");
		helper.succeed();
	}

	/** Recall puts her next to the owner; in Follow mode she teleports by herself when left far behind. */
	@GameTest(maxTicks = 200)
	public void teleportsToOwner(GameTestHelper helper) {
		ServerPlayer owner = player(helper, 2, 2);
		LinkleEntity linkle = linkleFor(helper, owner, 2, 4);

		owner.snapTo(helper.absoluteVec(new Vec3(4, 1, 26)));
		LinkleEntity recalled = LinkleSummoning.teleportToOwner(linkle, owner);
		helper.assertTrue(recalled.distanceTo(owner) < 4.5F, "Recall should land within a few blocks of the owner");

		// Now walk away from her and let her own AI catch up. (She stays inside the test area so her
		// chunk keeps ticking; the owner moves away instead.)
		recalled.snapTo(helper.absoluteVec(new Vec3(2, 1, 2)));
		owner.snapTo(helper.absoluteVec(new Vec3(4, 1, 26)));
		double limit = LinkleConfig.get().teleportDistance;
		helper.assertTrue(recalled.distanceTo(owner) > limit, "Setup: she must start beyond teleport range");
		helper.succeedWhen(() -> helper.assertTrue(recalled.distanceTo(owner) < limit, "Linkle should teleport back to her owner"));
	}

	/** Lethal damage knocks her out instead of killing her; food wakes her up. */
	@GameTest
	public void knockoutAndFoodRevive(GameTestHelper helper) {
		ServerPlayer owner = player(helper, 2, 2);
		LinkleEntity linkle = linkleFor(helper, owner, 3, 3);
		ServerLevel level = helper.getLevel();
		boolean realDeath = LinkleConfig.get().realDeath;
		LinkleConfig.get().realDeath = false;
		try {
			linkle.hurtServer(level, level.damageSources().generic(), 1000.0F);
			helper.assertTrue(linkle.isAlive(), "She must not die by default");
			helper.assertTrue(linkle.isKnockedOut(), "She must be knocked out");
			helper.assertFalse(linkle.hurtServer(level, level.damageSources().generic(), 5.0F), "Knocked out = no more damage");

			owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.COOKED_BEEF));
			linkle.mobInteract(owner, InteractionHand.MAIN_HAND);
			helper.assertFalse(linkle.isKnockedOut(), "Food wakes her up");
			helper.assertTrue(linkle.getHealth() >= linkle.getMaxHealth() * 0.5F, "Fed recovery gives at least half health");
		} finally {
			LinkleConfig.get().realDeath = realDeath;
		}
		helper.succeed();
	}

	/** Without food she gets up on her own when the knockout timer runs out. */
	@GameTest(maxTicks = 100)
	public void knockoutTimerRecovers(GameTestHelper helper) {
		ServerPlayer owner = player(helper, 2, 2);
		LinkleEntity linkle = linkleFor(helper, owner, 3, 3);
		int seconds = LinkleConfig.get().knockoutSeconds;
		LinkleConfig.get().knockoutSeconds = 1;
		try {
			linkle.knockOut();
		} finally {
			LinkleConfig.get().knockoutSeconds = seconds;
		}
		helper.assertTrue(linkle.isKnockedOut(), "Setup: knocked out");
		helper.succeedWhen(() -> helper.assertFalse(linkle.isKnockedOut(), "She should get up after the timer"));
	}

	/** With realDeath on, lethal damage really kills her. */
	@GameTest
	public void realDeathOption(GameTestHelper helper) {
		ServerPlayer owner = player(helper, 2, 2);
		LinkleEntity linkle = linkleFor(helper, owner, 3, 3);
		ServerLevel level = helper.getLevel();
		boolean realDeath = LinkleConfig.get().realDeath;
		LinkleConfig.get().realDeath = true;
		try {
			linkle.hurtServer(level, level.damageSources().generic(), 1000.0F);
			helper.assertFalse(linkle.isAlive(), "With realDeath she dies");
		} finally {
			LinkleConfig.get().realDeath = realDeath;
		}
		helper.succeed();
	}

	/** A name ending in an outfit name picks that outfit; other names keep the outfit. */
	@GameTest
	public void nameTagPicksSkin(GameTestHelper helper) {
		ServerPlayer owner = player(helper, 2, 2);
		LinkleEntity linkle = linkleFor(helper, owner, 3, 3);
		linkle.setCustomName(net.minecraft.network.chat.Component.literal("Linkle Azure"));
		helper.assertTrue(linkle.getVariant() == dev.linklecompanion.entity.LinkleVariant.AZURE, "'Linkle Azure' should pick azure");
		linkle.setCustomName(net.minecraft.network.chat.Component.literal("Bob"));
		helper.assertTrue(linkle.getVariant() == dev.linklecompanion.entity.LinkleVariant.AZURE, "Other names keep the outfit");
		helper.succeed();
	}

	/** When hurt, she eats food from her pockets on her own. */
	@GameTest(maxTicks = 120)
	public void eatsFoodWhenHurt(GameTestHelper helper) {
		ServerPlayer owner = player(helper, 2, 2);
		LinkleEntity linkle = linkleFor(helper, owner, 3, 3);
		linkle.getInventory().addItem(new ItemStack(Items.BREAD, 3));
		linkle.setHealth(8.0F);
		helper.succeedWhen(() -> {
			int bread = 0;
			for (int slot = 0; slot < linkle.getInventory().getContainerSize(); slot++) {
				if (linkle.getInventory().getItem(slot).is(Items.BREAD)) {
					bread += linkle.getInventory().getItem(slot).getCount();
				}
			}
			helper.assertTrue(bread < 3, "She should eat some bread");
			helper.assertTrue(linkle.getHealth() > 8.0F, "Eating should heal her");
		});
	}

	/** Master switch off: she sits, can't target anything, and summoning is refused. On again: back to normal. */
	@GameTest
	public void masterSwitchPauses(GameTestHelper helper) {
		ServerPlayer owner = player(helper, 2, 2);
		LinkleEntity linkle = linkleFor(helper, owner, 3, 3);
		Zombie zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(6, 1, 6));
		boolean enabled = LinkleConfig.get().enabled;
		LinkleConfig.get().enabled = false;
		try {
			helper.assertTrue(linkle.isOrderedToSit(), "Paused Linkle should sit");
			helper.assertFalse(linkle.canAttack(zombie), "Paused Linkle can't attack");
			helper.assertTrue(linkle.getMode() == LinkleMode.FOLLOW, "Pausing must not change her saved mode");
			helper.assertTrue(LinkleSummoning.summonOrRecall(owner, true) == LinkleSummoning.Result.FAILED, "Summoning is refused while off");
		} finally {
			LinkleConfig.get().enabled = enabled;
		}
		helper.assertFalse(linkle.isOrderedToSit(), "Back on: she stands up again");
		helper.assertTrue(linkle.canAttack(zombie), "Back on: she can fight again");
		helper.succeed();
	}

	/** Her bolts can't hurt villagers (or other friends), but still hurt monsters. */
	@GameTest
	public void noFriendlyFire(GameTestHelper helper) {
		ServerPlayer owner = player(helper, 2, 2);
		LinkleEntity linkle = linkleFor(helper, owner, 3, 3);
		ServerLevel level = helper.getLevel();
		Villager villager = helper.spawnWithNoFreeWill(EntityTypes.VILLAGER, new BlockPos(6, 1, 6));
		Zombie zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(8, 1, 8));
		Arrow bolt = new Arrow(level, linkle, new ItemStack(Items.ARROW), linkle.getMainHandItem());
		DamageSource source = level.damageSources().arrow(bolt, linkle);

		float villagerHealth = villager.getHealth();
		villager.hurtServer(level, source, 6.0F);
		helper.assertTrue(villager.getHealth() == villagerHealth, "Linkle's bolt must not hurt a villager");
		helper.assertFalse(linkle.canAttack(villager), "Villagers are never targets");
		helper.assertFalse(linkle.canAttack(owner), "The owner is never a target");

		float zombieHealth = zombie.getHealth();
		zombie.hurtServer(level, source, 6.0F);
		helper.assertTrue(zombie.getHealth() < zombieHealth, "Linkle's bolt should still hurt a zombie");
		helper.succeed();
	}
}
