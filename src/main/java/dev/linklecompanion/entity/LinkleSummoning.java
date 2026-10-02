package dev.linklecompanion.entity;

import dev.linklecompanion.config.LinkleConfig;
import dev.linklecompanion.dialogue.Topic;
import dev.linklecompanion.registry.ModAttachments;
import dev.linklecompanion.registry.ModEntities;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * Creating, calling back, befriending and teleporting Linkle. Shared by the Wanderer's Compass,
 * the {@code /linkle} command and the dimension-change event.
 */
public final class LinkleSummoning {
	public enum Result {
		SUMMONED,
		RECALLED,
		/** The player's Linkle exists but is in an unloaded area; nothing happened. */
		ELSEWHERE,
		FAILED
	}

	private LinkleSummoning() {
	}

	/** The player's current Linkle if she is loaded in any dimension, else null. */
	public static @Nullable LinkleEntity findLoaded(ServerPlayer player) {
		ModAttachments.CompanionData data = player.getAttached(ModAttachments.COMPANION);
		return data == null ? null : findLoaded(player.level().getServer(), data.linkleId());
	}

	public static @Nullable LinkleEntity findLoaded(MinecraftServer server, UUID id) {
		for (ServerLevel level : server.getAllLevels()) {
			Entity entity = level.getEntity(id);
			if (entity instanceof LinkleEntity linkle && linkle.isAlive()) {
				return linkle;
			}
		}
		return null;
	}

	/**
	 * Recalls the player's Linkle if she is loaded; otherwise creates a new one.
	 *
	 * @param forceNew create a new Linkle even if the old one is somewhere unloaded
	 *                 (the old one leaves the next time her area loads)
	 */
	public static Result summonOrRecall(ServerPlayer player, boolean forceNew) {
		LinkleConfig config = LinkleConfig.get();
		if (!config.enabled) {
			player.sendOverlayMessage(Component.translatable("message.linkle_companion.disabled"));
			return Result.FAILED;
		}
		ModAttachments.CompanionData data = player.getAttached(ModAttachments.COMPANION);
		LinkleEntity existing = findLoaded(player);

		if (existing != null && existing.isOwnedByPlayer(player) && (config.onePerPlayer || !forceNew)) {
			teleportToOwner(existing, player).dialogue().say(Topic.RECALL);
			return Result.RECALLED;
		}
		if (data != null && config.onePerPlayer && !forceNew) {
			player.sendSystemMessage(Component.translatable("message.linkle_companion.elsewhere"));
			return Result.ELSEWHERE;
		}

		ServerLevel level = player.level();
		LinkleEntity linkle = ModEntities.LINKLE.create(level, EntitySpawnReason.MOB_SUMMONED);
		if (linkle == null) {
			return Result.FAILED;
		}
		int generation = data == null ? 1 : data.generation() + 1;
		Vec3 spot = findSpotNear(linkle, level, player.blockPosition());
		linkle.snapTo(spot.x, spot.y, spot.z, player.getYRot() + 180.0F, 0.0F);
		linkle.setupNew(player, generation);
		level.addFreshEntityWithPassengers(linkle);
		player.setAttached(ModAttachments.COMPANION, new ModAttachments.CompanionData(linkle.getUUID(), generation));

		level.sendParticles(ParticleTypes.HAPPY_VILLAGER, linkle.getX(), linkle.getY() + 1.0, linkle.getZ(), 16, 0.4, 0.8, 0.4, 0.0);
		level.playSound(null, linkle.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.0F, 1.2F);
		CriteriaTriggers.SUMMONED_ENTITY.trigger(player, linkle);
		linkle.dialogue().say(Topic.GREETING);
		return Result.SUMMONED;
	}

	/** A wild (ownerless) Linkle, e.g. from /summon, joins a player who has no Linkle yet. */
	public static boolean befriend(LinkleEntity linkle, ServerPlayer player) {
		LinkleConfig config = LinkleConfig.get();
		if (!config.enabled) {
			player.sendOverlayMessage(Component.translatable("message.linkle_companion.disabled"));
			return false;
		}
		ModAttachments.CompanionData data = player.getAttached(ModAttachments.COMPANION);
		if (config.onePerPlayer && data != null && findLoaded(player) != null) {
			player.sendOverlayMessage(Component.translatable("message.linkle_companion.already_have"));
			return false;
		}
		int generation = data == null ? 1 : data.generation() + 1;
		linkle.setupBefriended(player, generation);
		player.setAttached(ModAttachments.COMPANION, new ModAttachments.CompanionData(linkle.getUUID(), generation));
		player.level().sendParticles(ParticleTypes.HEART, linkle.getX(), linkle.getEyeY() + 0.3, linkle.getZ(), 6, 0.4, 0.3, 0.4, 0.0);
		CriteriaTriggers.SUMMONED_ENTITY.trigger(player, linkle);
		linkle.dialogue().say(Topic.GREETING);
		return true;
	}

	/**
	 * Moves Linkle next to her owner, across dimensions if needed. Changing dimension creates a new
	 * entity object, so callers must use the returned Linkle afterwards.
	 */
	public static LinkleEntity teleportToOwner(LinkleEntity linkle, ServerPlayer owner) {
		ServerLevel target = owner.level();
		Vec3 spot = linkle.level() == target ? findSpotNear(linkle, target, owner.blockPosition()) : owner.position();
		linkle.getNavigation().stop();
		Entity moved = linkle.teleport(new TeleportTransition(target, spot, Vec3.ZERO, linkle.getYRot(), linkle.getXRot(), TeleportTransition.DO_NOTHING));
		return moved instanceof LinkleEntity movedLinkle ? movedLinkle : linkle;
	}

	/**
	 * Finds a safe standing spot 2-3 blocks from {@code center}: walkable (no lava, fire or open
	 * drop), not on leaves, with room for her. Falls back to the center itself.
	 */
	public static Vec3 findSpotNear(LinkleEntity linkle, ServerLevel level, BlockPos center) {
		if (linkle.level() == level) {
			for (int attempt = 0; attempt < 12; attempt++) {
				int dx = Mth.randomBetweenInclusive(linkle.getRandom(), -3, 3);
				int dz = Mth.randomBetweenInclusive(linkle.getRandom(), -3, 3);
				if (Math.abs(dx) < 2 && Math.abs(dz) < 2) {
					continue;
				}
				int dy = Mth.randomBetweenInclusive(linkle.getRandom(), -1, 1);
				BlockPos pos = center.offset(dx, dy, dz);
				if (isSafeSpot(linkle, level, pos)) {
					return Vec3.atBottomCenterOf(pos);
				}
			}
		}
		return Vec3.atBottomCenterOf(center);
	}

	private static boolean isSafeSpot(LinkleEntity linkle, ServerLevel level, BlockPos pos) {
		if (WalkNodeEvaluator.getPathTypeStatic(linkle, pos) != PathType.WALKABLE) {
			return false;
		}
		if (level.getBlockState(pos.below()).getBlock() instanceof LeavesBlock) {
			return false;
		}
		BlockPos delta = pos.subtract(linkle.blockPosition());
		return level.noCollision(linkle, linkle.getBoundingBox().move(delta));
	}
}
