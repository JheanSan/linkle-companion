package dev.linklecompanion.gametest;

import dev.linklecompanion.client.compat.ClientCompat;
import dev.linklecompanion.LinkleCompanion;
import dev.linklecompanion.entity.LinkleEntity;
import dev.linklecompanion.entity.LinkleMode;
import dev.linklecompanion.entity.LinkleVariant;
import dev.linklecompanion.network.DialoguePayload;
import dev.linklecompanion.registry.ModEntities;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;

import java.nio.file.Path;

/**
 * Client "showcase" test ({@code ./gradlew runClientGameTest}): builds small scenes and takes
 * screenshots of Linkle's look, poses, skins, inventory and dialogue box. Used to check the art
 * after changes; the pictures are also handy for the README and Modrinth gallery.
 * Screenshots land in {@code build/run/clientGameTest/screenshots}.
 */
public class LinkleShowcaseClientTest implements FabricClientGameTest {
	private static final double Y = -60.0;

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			TestServerContext server = world.getServer();
			server.runCommand("time set noon");
			server.runCommand("gamerule advance_time false");
			server.runCommand("gamerule spawn_mobs false");
			server.runCommand("weather clear");
			server.runCommand("gamemode creative @a");
			server.runCommand("tp @a 0.5 " + Y + " 0.5 0 12");
			context.waitTicks(40);
			setHudHidden(context, true);

			// Front, back and side views at portrait distance.
			spawn(server, 0.5, 3.2, 180.0F, LinkleVariant.CLASSIC);
			context.waitTicks(10);
			shot(context, "01_front");
			setBodyYaw(server, 0.0F);
			context.waitTicks(25);
			shot(context, "02_back");
			setBodyYaw(server, 90.0F);
			context.waitTicks(25);
			shot(context, "03_side");

			// Poses.
			setBodyYaw(server, 180.0F);
			context.waitTicks(25);
			edit(server, linkle -> linkle.setAiming(true));
			context.waitTicks(5);
			shot(context, "04_dual_aim");
			edit(server, linkle -> {
				linkle.setAiming(false);
				linkle.startUsingItem(InteractionHand.MAIN_HAND);
			});
			context.waitTicks(8);
			shot(context, "05_charging");
			edit(server, linkle -> {
				linkle.stopUsingItem();
				linkle.setInspecting(true);
			});
			context.waitTicks(5);
			shot(context, "06_inspect");
			edit(server, linkle -> {
				linkle.setInspecting(false);
				linkle.setVolleying(true);
			});
			context.waitTicks(4);
			shot(context, "07_volley");
			edit(server, linkle -> {
				linkle.setVolleying(false);
				linkle.setMode(LinkleMode.STAY);
				linkle.setInSittingPose(true);
			});
			context.waitTicks(5);
			shot(context, "08_sitting");
			edit(server, linkle -> {
				linkle.setInSittingPose(false);
				linkle.setMode(LinkleMode.FOLLOW);
				linkle.knockOut();
			});
			context.waitTicks(5);
			shot(context, "09_knocked_out");
			clear(server);
			context.waitTicks(5);

			// Skin lineup.
			LinkleVariant[] lineup = {LinkleVariant.CLASSIC, LinkleVariant.CRIMSON, LinkleVariant.AZURE, LinkleVariant.VIOLET, LinkleVariant.SNOW};
			for (int i = 0; i < lineup.length; i++) {
				spawn(server, -2.5 + i * 1.5, 5.5, 180.0F, lineup[i]);
			}
			context.waitTicks(10);
			shot(context, "10_skins");
			clear(server);
			context.waitTicks(5);

			// Game distance: how she reads ~10 blocks away.
			spawn(server, 1.5, 10.0, 200.0F, LinkleVariant.CLASSIC);
			context.waitTicks(10);
			shot(context, "11_distance_10");
			clear(server);
			context.waitTicks(5);

			// HUD: dialogue box and inventory screen (owned Linkle).
			setHudHidden(context, false);
			server.runOnServer(srv -> {
				ServerPlayer player = srv.getPlayerList().getPlayers().get(0);
				LinkleEntity linkle = ModEntities.LINKLE.create(player.level(), EntitySpawnReason.COMMAND);
				linkle.snapTo(0.5, Y, 3.2, 180.0F, 0.0F);
				linkle.setNoAi(true);
				linkle.setupNew(player, 1);
				player.level().addFreshEntity(linkle);
			});
			context.waitTicks(10);
			// Speech bubble (default), then the corner box.
			server.runOnServer(srv -> ServerPlayNetworking.send(srv.getPlayerList().getPlayers().get(0),
				new DialoguePayload(find(srv).getId(), "classic", "dialogue.linkle_companion.greeting.2")));
			context.waitTicks(20);
			shot(context, "12a_speech_bubble");
			context.runOnClient(client -> dev.linklecompanion.config.LinkleConfig.get().dialogueDisplay = "hud");
			server.runOnServer(srv -> ServerPlayNetworking.send(srv.getPlayerList().getPlayers().get(0),
				new DialoguePayload(find(srv).getId(), "classic", "dialogue.linkle_companion.greeting.2")));
			context.waitTicks(20);
			shot(context, "12b_dialogue_box");
			context.runOnClient(client -> dev.linklecompanion.config.LinkleConfig.get().dialogueDisplay = "bubble");
			server.runOnServer(srv -> {
				ServerPlayer player = srv.getPlayerList().getPlayers().get(0);
				LinkleEntity linkle = find(srv);
				if (linkle != null) {
					linkle.openInventory(player);
				}
			});
			context.waitTicks(20);
			shot(context, "13_inventory");
			context.setScreen(() -> new dev.linklecompanion.client.screen.LinkleSettingsScreen(null));
			context.waitTicks(10);
			shot(context, "14_settings");
		}
	}

	private static void setHudHidden(ClientGameTestContext context, boolean hidden) {
		context.runOnClient(client -> ClientCompat.setHudHidden(hidden));
	}

	private static void shot(ClientGameTestContext context, String name) {
		// World scenes in Full HD for the README and store galleries. Menus and HUD only lay out at the
		// window's own size, so those shots are taken at normal size.
		boolean hasGui = name.contains("dialogue") || name.contains("inventory") || name.contains("settings");
		TestScreenshotOptions options = TestScreenshotOptions.of("linkle_" + name);
		Path path = context.takeScreenshot(hasGui ? options : options.withSize(1920, 1080));
		LinkleCompanion.LOGGER.info("Showcase screenshot: {}", path);
	}

	private static void spawn(TestServerContext server, double x, double z, float yaw, LinkleVariant variant) {
		server.runOnServer(srv -> {
			ServerLevel level = srv.overworld();
			LinkleEntity linkle = ModEntities.LINKLE.create(level, EntitySpawnReason.COMMAND);
			linkle.snapTo(x, Y, z, yaw, 0.0F);
			linkle.setYHeadRot(yaw);
			linkle.setYBodyRot(yaw);
			linkle.setNoAi(true);
			linkle.giveCrossbows();
			linkle.setVariant(variant);
			level.addFreshEntity(linkle);
		});
	}

	/** Removes all Linkles without a death animation, so the next scene is clean. */
	private static void clear(TestServerContext server) {
		server.runOnServer(srv -> {
			java.util.List<LinkleEntity> found = new java.util.ArrayList<>();
			for (var entity : srv.overworld().getAllEntities()) {
				if (entity instanceof LinkleEntity linkle) {
					found.add(linkle);
				}
			}
			found.forEach(LinkleEntity::discard);
		});
	}

	private static void setBodyYaw(TestServerContext server, float yaw) {
		edit(server, linkle -> {
			linkle.setYRot(yaw);
			linkle.setYHeadRot(yaw);
			linkle.setYBodyRot(yaw);
		});
	}

	private static void edit(TestServerContext server, java.util.function.Consumer<LinkleEntity> action) {
		server.runOnServer(srv -> {
			LinkleEntity linkle = find(srv);
			if (linkle != null) {
				action.accept(linkle);
			}
		});
	}

	private static LinkleEntity find(MinecraftServer server) {
		for (var entity : server.overworld().getAllEntities()) {
			if (entity instanceof LinkleEntity linkle && linkle.isAlive()) {
				return linkle;
			}
		}
		return null;
	}
}
