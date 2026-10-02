package dev.linklecompanion.gametest;

import dev.linklecompanion.LinkleCompanion;
import dev.linklecompanion.entity.LinkleEntity;
import dev.linklecompanion.registry.ModEntities;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Combat play-test ({@code ./gradlew runClientGameTest}): a real survival world, Linkle versus a
 * zombie wave, with a villager standing near the line of fire. Fails if she doesn't win, doesn't
 * use her arrows, or hurts the villager. Also triggers the volley and the knockout/feed cycle and
 * takes screenshots along the way.
 */
public class LinkleCombatClientTest implements FabricClientGameTest {
	private static final double Y = -60.0;

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			TestServerContext server = world.getServer();
			server.runCommand("difficulty normal");
			server.runCommand("time set noon");
			server.runCommand("gamerule advance_time false");
			server.runCommand("gamerule spawn_mobs false");
			server.runCommand("gamemode survival @a");
			server.runCommand("effect give @a resistance infinite 4 true");
			server.runCommand("effect give @a regeneration infinite 2 true");
			server.runCommand("tp @a 0.5 " + Y + " 0.5 0 5");
			context.waitTicks(40);

			// Linkle at the player's side, a villager off to the right of the firing line, zombies downrange.
			server.runOnServer(srv -> {
				ServerPlayer player = player(srv);
				LinkleEntity linkle = ModEntities.LINKLE.create(player.level(), EntitySpawnReason.COMMAND);
				linkle.snapTo(1.5, Y, 2.0, 0.0F, 0.0F);
				linkle.setupNew(player, 1);
				player.level().addFreshEntity(linkle);
			});
			// Accuracy: a stationary husk (no sun damage) 12 blocks away must go down with few arrows.
			context.waitTicks(20);
			int arrowsAtStart = server.computeOnServer(srv -> arrows(linkle(srv)));
			server.runCommand("summon minecraft:husk 1.5 " + Y + " 14.0 {NoAI:1b,PersistenceRequired:1b}");
			int huskTicks = server.waitFor(srv -> !anyAlive(srv, "husk"), 600);
			int arrowsForHusk = arrowsAtStart - server.computeOnServer(srv -> arrows(linkle(srv)));
			LinkleCompanion.LOGGER.info("Combat play-test: still husk at 12 blocks killed after {} ticks with {} arrows", huskTicks, arrowsForHusk);
			if (arrowsForHusk > 8) {
				throw new AssertionError("Too many arrows for a still target at 12 blocks: " + arrowsForHusk);
			}

			server.runCommand("summon minecraft:villager 4.5 " + Y + " 9.5 {NoAI:1b}");
			for (int i = 0; i < 4; i++) {
				server.runCommand("summon minecraft:zombie " + (-3 + i * 2) + ".5 " + Y + " 17.5 {PersistenceRequired:1b}");
			}
			int arrowsBefore = server.computeOnServer(srv -> arrows(linkle(srv)));
			float villagerHealthBefore = server.computeOnServer(srv -> villager(srv).getHealth());

			context.waitTicks(50);
			shot(context, "combat_1_engage");
			context.waitTicks(60);
			shot(context, "combat_2_firing");

			int ticks = server.waitFor(srv -> zombies(srv).isEmpty(), 900);
			LinkleCompanion.LOGGER.info("Combat play-test: wave cleared after {} ticks", ticks);
			context.waitTicks(20);
			shot(context, "combat_3_cleared");

			int arrowsAfter = server.computeOnServer(srv -> arrows(linkle(srv)));
			float villagerHealthAfter = server.computeOnServer(srv -> villager(srv).getHealth());
			LinkleCompanion.LOGGER.info("Combat play-test: arrows {} -> {}, villager health {} -> {}",
				arrowsBefore, arrowsAfter, villagerHealthBefore, villagerHealthAfter);
			if (arrowsAfter >= arrowsBefore) {
				throw new AssertionError("Linkle should have used arrows from her inventory");
			}
			if (villagerHealthAfter < villagerHealthBefore) {
				throw new AssertionError("Linkle hurt the villager");
			}

			// Crowd her: the Twin Cyclone volley should fire.
			server.runOnServer(srv -> {
				LinkleEntity linkle = linkle(srv);
				LinkleCompanion.LOGGER.info("Combat play-test: volley cooldown left after wave 1: {} ticks (>0 means she already used it)", linkle.getVolleyCooldown());
				linkle.setVolleyCooldown(0);
				for (int i = 0; i < 3; i++) {
					Zombie zombie = EntityTypes.ZOMBIE.create(srv.overworld(), EntitySpawnReason.COMMAND);
					zombie.snapTo(linkle.getX() + (i - 1) * 2.5, Y, linkle.getZ() + 3.0, 180.0F, 0.0F);
					zombie.setPersistenceRequired();
					srv.overworld().addFreshEntity(zombie);
				}
			});
			int volleyTicks = server.waitFor(srv -> linkle(srv).isVolleying(), 200);
			context.waitTicks(6);
			shot(context, "combat_4_volley");
			LinkleCompanion.LOGGER.info("Combat play-test: volley started after {} ticks", volleyTicks);
			server.waitFor(srv -> zombies(srv).isEmpty(), 900);

			// Knock her out, then wake her with food.
			server.runOnServer(srv -> {
				LinkleEntity linkle = linkle(srv);
				linkle.hurtServer(srv.overworld(), srv.overworld().damageSources().generic(), 1000.0F);
			});
			context.waitTicks(20);
			shot(context, "combat_5_knocked_out");
			boolean knockedOut = server.computeOnServer(srv -> linkle(srv).isKnockedOut());
			server.runOnServer(srv -> {
				ServerPlayer player = player(srv);
				player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD, 4));
				linkle(srv).mobInteract(player, InteractionHand.MAIN_HAND);
			});
			context.waitTicks(30);
			shot(context, "combat_6_revived");
			boolean revived = server.computeOnServer(srv -> !linkle(srv).isKnockedOut() && linkle(srv).isAlive());
			if (!knockedOut || !revived) {
				throw new AssertionError("Knockout/revive cycle failed: knockedOut=" + knockedOut + " revived=" + revived);
			}
			LinkleCompanion.LOGGER.info("Combat play-test: PASSED");
		}
	}

	private static void shot(ClientGameTestContext context, String name) {
		LinkleCompanion.LOGGER.info("Combat screenshot: {}", context.takeScreenshot("linkle_" + name));
	}

	private static ServerPlayer player(MinecraftServer server) {
		return server.getPlayerList().getPlayers().get(0);
	}

	private static List<Entity> all(MinecraftServer server) {
		ServerLevel level = server.overworld();
		List<Entity> list = new ArrayList<>();
		level.getAllEntities().forEach(list::add);
		return list;
	}

	private static LinkleEntity linkle(MinecraftServer server) {
		for (Entity entity : all(server)) {
			if (entity instanceof LinkleEntity linkle) {
				return linkle;
			}
		}
		throw new AssertionError("Linkle disappeared");
	}

	private static Villager villager(MinecraftServer server) {
		for (Entity entity : all(server)) {
			if (entity instanceof Villager villager) {
				return villager;
			}
		}
		throw new AssertionError("Villager disappeared (killed?)");
	}

	private static List<Zombie> zombies(MinecraftServer server) {
		List<Zombie> zombies = new ArrayList<>();
		for (Entity entity : all(server)) {
			if (entity instanceof Zombie zombie && zombie.isAlive()) {
				zombies.add(zombie);
			}
		}
		return zombies;
	}

	private static boolean anyAlive(MinecraftServer server, String path) {
		for (Entity entity : all(server)) {
			if (entity.isAlive() && entity.getType().builtInRegistryHolder().key().identifier().getPath().equals(path)) {
				return true;
			}
		}
		return false;
	}

	private static int arrows(LinkleEntity linkle) {
		int count = 0;
		for (int slot = 0; slot < linkle.getInventory().getContainerSize(); slot++) {
			ItemStack stack = linkle.getInventory().getItem(slot);
			if (stack.is(ItemTags.ARROWS)) {
				count += stack.getCount();
			}
		}
		return count;
	}
}
