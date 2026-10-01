package dev.linklecompanion.entity.goal;

import dev.linklecompanion.entity.LinkleEntity;
import dev.linklecompanion.entity.LinkleMode;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Keeps Linkle from boxing her owner in: when the owner walks right into her (or stands on top of
 * her and looks at her), she steps out of the way. Checked twice a second, not every tick.
 */
public class GiveWayGoal extends Goal {
	private static final double CROWDED_DISTANCE_SQR = 1.7 * 1.7;

	private final LinkleEntity linkle;
	private double lastOwnerX;
	private double lastOwnerZ;
	private int checkDelay;
	private int cooldown;

	public GiveWayGoal(LinkleEntity linkle) {
		this.linkle = linkle;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (cooldown > 0) {
			cooldown--;
			return false;
		}
		if (--checkDelay > 0) {
			return false;
		}
		checkDelay = 10;

		if (linkle.getMode() != LinkleMode.FOLLOW || !(linkle.getOwner() instanceof LivingEntity owner) || owner.level() != linkle.level()) {
			return false;
		}
		double movedX = owner.getX() - lastOwnerX;
		double movedZ = owner.getZ() - lastOwnerZ;
		lastOwnerX = owner.getX();
		lastOwnerZ = owner.getZ();

		if (linkle.distanceToSqr(owner) > CROWDED_DISTANCE_SQR) {
			return false;
		}
		// Is the owner heading toward her (moved toward her, or looking straight at her)?
		double toLinkleX = linkle.getX() - owner.getX();
		double toLinkleZ = linkle.getZ() - owner.getZ();
		boolean walkingIntoHer = movedX * toLinkleX + movedZ * toLinkleZ > 0.05;
		Vec3 look = owner.getLookAngle();
		double length = Math.sqrt(toLinkleX * toLinkleX + toLinkleZ * toLinkleZ);
		boolean lookingAtHer = length > 0.01 && (look.x * toLinkleX + look.z * toLinkleZ) / length > 0.8;
		return walkingIntoHer || lookingAtHer;
	}

	@Override
	public void start() {
		LivingEntity owner = linkle.getOwner();
		if (owner == null) {
			return;
		}
		Vec3 away = DefaultRandomPos.getPosAway(linkle, 4, 2, owner.position());
		if (away != null) {
			linkle.getNavigation().moveTo(away.x, away.y, away.z, 1.15);
		}
		cooldown = 40;
	}

	@Override
	public boolean canContinueToUse() {
		return !linkle.getNavigation().isDone();
	}
}
