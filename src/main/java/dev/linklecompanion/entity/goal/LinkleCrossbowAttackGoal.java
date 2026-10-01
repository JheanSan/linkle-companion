package dev.linklecompanion.entity.goal;

import dev.linklecompanion.config.LinkleConfig;
import dev.linklecompanion.dialogue.Topic;
import dev.linklecompanion.entity.CrossbowShooting;
import dev.linklecompanion.entity.LinkleEntity;
import dev.linklecompanion.entity.LinkleMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Linkle's ranged combat with two crossbows.
 *
 * <p>Weapon cycle: load the main-hand crossbow, load the off-hand crossbow, raise both, fire right,
 * then left a few ticks later. Loading uses vanilla crossbow item use, so the charging animation
 * and sounds are the real ones.
 *
 * <p>Movement: keeps 7-12 blocks away, strafes sideways while in range, backs off when something
 * gets closer than 4.5 blocks and closes in when the target is far or out of sight. In Stay mode
 * she doesn't move, she only turns and shoots. She never fires when a friend is in the line of fire.
 */
public class LinkleCrossbowAttackGoal extends Goal {
	private static final double RETREAT_DISTANCE_SQR = 4.5 * 4.5;
	private static final double PREFERRED_MIN_SQR = 7.0 * 7.0;
	private static final double PREFERRED_MAX_SQR = 12.0 * 12.0;
	private static final double APPROACH_DISTANCE_SQR = 16.0 * 16.0;
	private static final double FOLLOW_LEASH_SQR = 20.0 * 20.0;

	private enum Phase { IDLE, CHARGING, AIMING, FIRING }

	private final LinkleEntity linkle;
	private Phase phase = Phase.IDLE;
	private int seeTime;
	private int pathDelay;
	private int strafeTicks;
	private boolean strafeRight;
	private int chargeTicks;
	private int aimTicks;
	private int fireDelay;
	private int cooldown;
	private int blockedTicks;

	public LinkleCrossbowAttackGoal(LinkleEntity linkle) {
		this.linkle = linkle;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		LivingEntity target = linkle.getTarget();
		return target != null && target.isAlive() && !linkle.isKnockedOut()
			&& linkle.isHolding(Items.CROSSBOW)
			&& (linkle.hasAmmo() || anyCharged())
			&& insideLeash(target);
	}

	@Override
	public boolean canContinueToUse() {
		return canUse();
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void start() {
		linkle.setAggressive(true);
		phase = Phase.IDLE;
		seeTime = 0;
		pathDelay = 0;
		cooldown = 0;
	}

	@Override
	public void stop() {
		linkle.setAggressive(false);
		if (linkle.isUsingItem()) {
			linkle.stopUsingItem();
		}
		linkle.setChargingCrossbow(false);
		linkle.setAiming(false);
		linkle.getNavigation().stop();
		phase = Phase.IDLE;

		LivingEntity target = linkle.getTarget();
		if (target == null || !target.isAlive() || !insideLeash(target)) {
			linkle.setTarget(null);
		} else if (!linkle.hasAmmo() && !anyCharged()) {
			linkle.dialogue().say(Topic.OUT_OF_AMMO);
		}
	}

	/** Don't let a fight drag her away from her owner or her guard point. */
	private boolean insideLeash(LivingEntity target) {
		LinkleMode mode = linkle.getMode();
		if (mode == LinkleMode.FOLLOW) {
			LivingEntity owner = linkle.getOwner();
			return owner == null || (owner.level() == linkle.level() && linkle.distanceToSqr(owner) < FOLLOW_LEASH_SQR);
		}
		if (mode == LinkleMode.GUARD) {
			BlockPos guard = linkle.getGuardPos();
			double radius = LinkleConfig.get().guardRadius + 8.0;
			return guard == null || target.distanceToSqr(guard.getX() + 0.5, guard.getY(), guard.getZ() + 0.5) < radius * radius;
		}
		return linkle.distanceToSqr(target) < 24.0 * 24.0;
	}

	private boolean anyCharged() {
		return CrossbowShooting.isCharged(linkle, InteractionHand.MAIN_HAND) || CrossbowShooting.isCharged(linkle, InteractionHand.OFF_HAND);
	}

	@Override
	public void tick() {
		LivingEntity target = linkle.getTarget();
		if (target == null) {
			return;
		}
		boolean canSee = linkle.getSensing().hasLineOfSight(target);
		if (canSee != seeTime > 0) {
			seeTime = 0;
		}
		seeTime += canSee ? 1 : -1;

		double distanceSqr = linkle.distanceToSqr(target);
		move(target, distanceSqr);
		linkle.getLookControl().setLookAt(target, 30.0F, 30.0F);
		useWeapons(target, canSee);
	}

	private void move(LivingEntity target, double distanceSqr) {
		if (linkle.getMode() == LinkleMode.STAY) {
			linkle.getNavigation().stop();
			return;
		}
		double speed = linkle.isUsingItem() ? 0.7 : 1.0;
		if (distanceSqr < RETREAT_DISTANCE_SQR) {
			// Too close: back off to a spot away from the target.
			if (--pathDelay <= 0) {
				pathDelay = 10;
				Vec3 away = DefaultRandomPos.getPosAway(linkle, 8, 4, target.position());
				if (away != null) {
					linkle.getNavigation().moveTo(away.x, away.y, away.z, 1.25);
				}
			}
		} else if (distanceSqr > APPROACH_DISTANCE_SQR || seeTime < -20) {
			// Too far or lost sight: close in.
			if (--pathDelay <= 0) {
				pathDelay = 15;
				linkle.getNavigation().moveTo(target, speed);
			}
		} else {
			// In range: circle-strafe like a skilled archer.
			linkle.getNavigation().stop();
			pathDelay = 0;
			if (++strafeTicks >= 25) {
				if (linkle.getRandom().nextFloat() < 0.35F) {
					strafeRight = !strafeRight;
				}
				strafeTicks = 0;
			}
			float forward = distanceSqr > PREFERRED_MAX_SQR ? 0.5F : distanceSqr < PREFERRED_MIN_SQR ? -0.5F : 0.0F;
			linkle.getMoveControl().strafe(forward, strafeRight ? 0.5F : -0.5F);
			linkle.lookAt(target, 30.0F, 30.0F);
		}
	}

	private void useWeapons(LivingEntity target, boolean canSee) {
		switch (phase) {
			case IDLE -> {
				if (cooldown > 0) {
					cooldown--;
					return;
				}
				InteractionHand toLoad = handToLoad();
				if (toLoad != null && linkle.hasAmmo() && seeTime > -60) {
					linkle.startUsingItem(toLoad);
					linkle.setChargingCrossbow(true);
					chargeTicks = 0;
					phase = Phase.CHARGING;
				} else if (anyCharged()) {
					startAiming();
				}
			}
			case CHARGING -> {
				chargeTicks++;
				if (!linkle.isUsingItem()) {
					linkle.setChargingCrossbow(false);
					phase = Phase.IDLE;
					return;
				}
				ItemStack using = linkle.getUseItem();
				if (CrossbowItem.isCharged(using)) {
					linkle.releaseUsingItem();
					linkle.setChargingCrossbow(false);
					phase = Phase.IDLE;
				} else if (chargeTicks > CrossbowItem.getChargeDuration(using, linkle) + 20) {
					// Could not load (ran out of arrows mid-load): give up this attempt.
					linkle.stopUsingItem();
					linkle.setChargingCrossbow(false);
					cooldown = 20;
					phase = Phase.IDLE;
				}
			}
			case AIMING -> {
				if (--aimTicks > 0 || !canSee) {
					return;
				}
				if (!linkle.isLineOfFireClear(target)) {
					// A friend is in the way: wait and sidestep.
					if (blockedTicks++ % 20 == 0) {
						strafeRight = !strafeRight;
					}
					return;
				}
				blockedTicks = 0;
				InteractionHand first = CrossbowShooting.isCharged(linkle, InteractionHand.MAIN_HAND) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
				CrossbowShooting.fire(linkle, first, target);
				fireDelay = 5;
				phase = Phase.FIRING;
			}
			case FIRING -> {
				if (--fireDelay > 0) {
					return;
				}
				InteractionHand second = CrossbowShooting.isCharged(linkle, InteractionHand.OFF_HAND) ? InteractionHand.OFF_HAND
					: CrossbowShooting.isCharged(linkle, InteractionHand.MAIN_HAND) ? InteractionHand.MAIN_HAND : null;
				if (second != null && canSee && linkle.isLineOfFireClear(target)) {
					CrossbowShooting.fire(linkle, second, target);
				}
				linkle.setAiming(false);
				cooldown = 6 + linkle.getRandom().nextInt(8);
				phase = Phase.IDLE;
			}
		}
	}

	private void startAiming() {
		linkle.setAiming(true);
		aimTicks = 6;
		phase = Phase.AIMING;
	}

	/** Main hand first, then off hand; null when both are loaded. */
	private InteractionHand handToLoad() {
		if (!CrossbowShooting.isCharged(linkle, InteractionHand.MAIN_HAND)) {
			return InteractionHand.MAIN_HAND;
		}
		if (!CrossbowShooting.isCharged(linkle, InteractionHand.OFF_HAND)) {
			return InteractionHand.OFF_HAND;
		}
		return null;
	}
}
