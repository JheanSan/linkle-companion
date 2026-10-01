package dev.linklecompanion.entity.goal;

import dev.linklecompanion.config.LinkleConfig;
import dev.linklecompanion.entity.LinkleEntity;
import dev.linklecompanion.entity.LinkleMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

/**
 * Picks hostile mobs to shoot. Runs its scan once per second (not every tick):
 * <ul>
 *   <li>Follow mode: hostiles near the owner (14 blocks).</li>
 *   <li>Guard mode: hostiles near her guard point (config radius).</li>
 *   <li>Stay mode and wild Linkles: never picks fights (she still defends herself).</li>
 * </ul>
 * Mobs that are already attacking the owner or Linkle are picked first, then the closest one.
 * Neutral mobs (wolves, zombified piglins, ...) are left alone unless they are angry at the owner
 * or at her. What attacks the owner directly is handled first by vanilla OwnerHurtByTargetGoal.
 */
public class LinkleThreatTargetGoal extends TargetGoal {
	private static final double FOLLOW_RADIUS = 14.0;
	private static final int SCAN_INTERVAL = 20;

	private final LinkleEntity linkle;
	private final TargetingConditions conditions = TargetingConditions.forCombat();
	private LivingEntity candidate;
	private int scanDelay;

	public LinkleThreatTargetGoal(LinkleEntity linkle) {
		super(linkle, true);
		this.linkle = linkle;
		this.setFlags(EnumSet.of(Flag.TARGET));
	}

	@Override
	public boolean canUse() {
		if (--scanDelay > 0) {
			return false;
		}
		scanDelay = reducedTickDelay(SCAN_INTERVAL);
		if (!linkle.isTame() || linkle.isKnockedOut() || linkle.getTarget() != null) {
			return false;
		}

		LivingEntity owner = linkle.getOwner();
		double centerX;
		double centerY;
		double centerZ;
		double radius;
		if (linkle.getMode() == LinkleMode.FOLLOW) {
			if (owner == null || owner.level() != linkle.level() || linkle.distanceToSqr(owner) > 24.0 * 24.0) {
				return false;
			}
			centerX = owner.getX();
			centerY = owner.getY();
			centerZ = owner.getZ();
			radius = FOLLOW_RADIUS;
		} else if (linkle.getMode() == LinkleMode.GUARD && linkle.getGuardPos() != null) {
			BlockPos guard = linkle.getGuardPos();
			centerX = guard.getX() + 0.5;
			centerY = guard.getY();
			centerZ = guard.getZ() + 0.5;
			radius = LinkleConfig.get().guardRadius;
		} else {
			return false;
		}

		AABB area = new AABB(centerX - radius, centerY - 6.0, centerZ - radius, centerX + radius, centerY + 6.0, centerZ + radius);
		List<Mob> mobs = linkle.level().getEntitiesOfClass(Mob.class, area, mob -> mob instanceof Enemy && mob.isAlive());
		double radiusSqr = radius * radius;
		LivingEntity best = null;
		double bestScore = Double.MAX_VALUE;
		for (Mob mob : mobs) {
			if (mob.distanceToSqr(centerX, centerY, centerZ) > radiusSqr) {
				continue;
			}
			LivingEntity mobTarget = mob.getTarget();
			boolean threatening = mobTarget != null && (mobTarget == owner || mobTarget == linkle);
			if (mob instanceof NeutralMob && !threatening) {
				continue;
			}
			if (!this.canAttack(mob, conditions) || !linkle.getSensing().hasLineOfSight(mob)) {
				continue;
			}
			double score = linkle.distanceToSqr(mob) - (threatening ? 10_000.0 : 0.0);
			if (score < bestScore) {
				bestScore = score;
				best = mob;
			}
		}
		candidate = best;
		return best != null;
	}

	@Override
	public void start() {
		linkle.setTarget(candidate);
		super.start();
	}

	@Override
	public void stop() {
		super.stop();
		candidate = null;
	}
}
