package dev.linklecompanion.entity.goal;

import dev.linklecompanion.config.LinkleConfig;
import dev.linklecompanion.dialogue.Topic;
import dev.linklecompanion.entity.CrossbowShooting;
import dev.linklecompanion.entity.LinkleEntity;
import dev.linklecompanion.entity.LinkleMode;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

/**
 * Signature move, "Twin Cyclone": when enemies crowd her, Linkle spins twice and fires a ring of
 * twelve bolts in every direction. Vanilla particles and sounds only.
 *
 * <p>Never hits friends: bolts aimed toward the owner, pets or villagers within 16 blocks are
 * skipped, and the damage guard in LinkleEvents blocks anything that slips through.
 */
public class VolleyGoal extends Goal {
	public static final int DURATION = 20;
	private static final int BOLTS = 12;
	private static final int FIRST_BOLT_TICK = 4;
	private static final double CROWD_RADIUS = 6.0;
	private static final double FRIEND_CHECK_RADIUS = 16.0;
	private static final double SKIP_COS = Math.cos(Math.toRadians(16.0));

	private final LinkleEntity linkle;
	private int ticks;
	private int checkDelay;
	private float startYaw;
	private List<Entity> friends = List.of();

	public VolleyGoal(LinkleEntity linkle) {
		this.linkle = linkle;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		if (linkle.getVolleyCooldown() > 0 || !linkle.isTame() || linkle.isKnockedOut() || linkle.getMode() == LinkleMode.STAY) {
			return false;
		}
		LivingEntity target = linkle.getTarget();
		if (target == null || !target.isAlive()) {
			return false;
		}
		if (--checkDelay > 0) {
			return false;
		}
		checkDelay = 10;
		// Use it when she's being crowded: the target is close or several enemies are near her.
		if (linkle.distanceToSqr(target) < 4.5 * 4.5) {
			return true;
		}
		AABB around = linkle.getBoundingBox().inflate(CROWD_RADIUS, 3.0, CROWD_RADIUS);
		List<Mob> enemies = linkle.level().getEntitiesOfClass(Mob.class, around, mob -> mob instanceof Enemy && mob.isAlive() && linkle.canAttack(mob));
		return enemies.size() >= 2;
	}

	@Override
	public boolean canContinueToUse() {
		return ticks < DURATION && !linkle.isKnockedOut();
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void start() {
		ticks = 0;
		startYaw = linkle.getYRot();
		if (linkle.isUsingItem()) {
			linkle.stopUsingItem();
			linkle.setChargingCrossbow(false);
		}
		linkle.setAiming(false);
		linkle.getNavigation().stop();
		linkle.setVolleying(true);
		AABB around = linkle.getBoundingBox().inflate(FRIEND_CHECK_RADIUS);
		friends = linkle.level().getEntities(linkle, around, linkle::isFriendlyTo);
		linkle.playSound(SoundEvents.CROSSBOW_QUICK_CHARGE_3.value(), 1.0F, 1.0F);
		linkle.dialogue().say(Topic.VOLLEY);
	}

	@Override
	public void stop() {
		linkle.setVolleying(false);
		linkle.setVolleyCooldown(LinkleConfig.get().volleyCooldownSeconds * 20);
		friends = List.of();
		if (linkle.level() instanceof ServerLevel level) {
			level.sendParticles(ParticleTypes.CLOUD, linkle.getX(), linkle.getY() + 0.2, linkle.getZ(), 24, 1.2, 0.05, 1.2, 0.03);
		}
	}

	@Override
	public void tick() {
		ticks++;
		if (!(linkle.level() instanceof ServerLevel level)) {
			return;
		}
		// Sweep particles circling her as she spins.
		float spin = startYaw + ticks * (720.0F / DURATION);
		double sx = -Mth.sin(spin * Mth.DEG_TO_RAD);
		double sz = Mth.cos(spin * Mth.DEG_TO_RAD);
		level.sendParticles(ParticleTypes.SWEEP_ATTACK, linkle.getX() + sx * 1.1, linkle.getY() + 1.0, linkle.getZ() + sz * 1.1, 1, 0.0, 0.0, 0.0, 0.0);

		int bolt = ticks - FIRST_BOLT_TICK;
		if (bolt < 0 || bolt >= BOLTS) {
			return;
		}
		float yaw = startYaw + bolt * (360.0F / BOLTS);
		double dirX = -Mth.sin(yaw * Mth.DEG_TO_RAD);
		double dirZ = Mth.cos(yaw * Mth.DEG_TO_RAD);
		if (!friendInDirection(dirX, dirZ)) {
			CrossbowShooting.fireVolleyBolt(linkle, dirX, dirZ);
		}
		linkle.playSound(SoundEvents.CROSSBOW_SHOOT, 0.7F, 1.0F + bolt * 0.04F);
	}

	private boolean friendInDirection(double dirX, double dirZ) {
		for (Entity friend : friends) {
			double dx = friend.getX() - linkle.getX();
			double dz = friend.getZ() - linkle.getZ();
			double length = Math.sqrt(dx * dx + dz * dz);
			if (length < 1.0) {
				// Standing right on top of her: too close to tell; skip nothing but rely on the damage guard.
				continue;
			}
			if ((dx * dirX + dz * dirZ) / length > SKIP_COS) {
				return true;
			}
		}
		return false;
	}
}
