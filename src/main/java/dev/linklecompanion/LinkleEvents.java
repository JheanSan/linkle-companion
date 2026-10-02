package dev.linklecompanion;

import dev.linklecompanion.command.LinkleCommand;
import dev.linklecompanion.entity.LinkleEntity;
import dev.linklecompanion.entity.LinkleMode;
import dev.linklecompanion.entity.LinkleSummoning;
import dev.linklecompanion.registry.ModAttachments;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * Fabric API event hooks (no mixins needed):
 * <ul>
 *   <li>Damage guard: Linkle's bolts never hurt her friends, and her owner can't hurt her by accident.</li>
 *   <li>Dimension change: a following Linkle goes through portals with her owner.</li>
 *   <li>Registers the {@code /linkle} command.</li>
 * </ul>
 */
public final class LinkleEvents {
	private LinkleEvents() {
	}

	public static void register() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			Entity attacker = source.getEntity();
			if (attacker instanceof LinkleEntity linkle && linkle != entity && linkle.isFriendlyTo(entity)) {
				return false;
			}
			if (entity instanceof LinkleEntity linkle && attacker != null && attacker == linkle.getOwner()) {
				return false;
			}
			return true;
		});

		ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register(LinkleEvents::onPlayerChangedLevel);

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> LinkleCommand.register(dispatcher));
	}

	private static void onPlayerChangedLevel(ServerPlayer player, ServerLevel origin, ServerLevel destination) {
		ModAttachments.CompanionData data = player.getAttached(ModAttachments.COMPANION);
		if (data == null) {
			return;
		}
		if (!(origin.getEntity(data.linkleId()) instanceof LinkleEntity linkle) || !linkle.isAlive() || !linkle.isOwnedByPlayer(player)) {
			return;
		}
		if (!dev.linklecompanion.config.LinkleConfig.get().followThroughPortals
			|| linkle.getMode() != LinkleMode.FOLLOW || linkle.isKnockedOut() || linkle.isPaused()) {
			return;
		}
		// The player has already left, so use what she noticed during her last check (once a second).
		if (!linkle.wasNearOwner()) {
			return;
		}
		LinkleSummoning.teleportToOwner(linkle, player);
	}
}
