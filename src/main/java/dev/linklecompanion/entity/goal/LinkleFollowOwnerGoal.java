package dev.linklecompanion.entity.goal;

import dev.linklecompanion.config.LinkleConfig;
import dev.linklecompanion.entity.LinkleEntity;
import dev.linklecompanion.entity.LinkleMode;
import dev.linklecompanion.entity.LinkleSummoning;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.PathType;

import java.util.EnumSet;

/**
 * Follow mode: walks after the owner, jogs when far behind, and teleports when she falls too far
 * back. Distances come from the config. Based on vanilla FollowOwnerGoal.
 */
public class LinkleFollowOwnerGoal extends Goal {
	private final LinkleEntity linkle;
	private LivingEntity owner;
	private int timeToRecalcPath;
	private float oldWaterCost;

	public LinkleFollowOwnerGoal(LinkleEntity linkle) {
		this.linkle = linkle;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (!linkle.isTame() || linkle.getMode() != LinkleMode.FOLLOW || linkle.unableToMoveToOwner()) {
			return false;
		}
		LivingEntity candidate = linkle.getOwner();
		if (candidate == null || candidate.level() != linkle.level()) {
			return false;
		}
		double start = LinkleConfig.get().followStartDistance;
		if (linkle.distanceToSqr(candidate) < start * start) {
			return false;
		}
		this.owner = candidate;
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		if (linkle.getNavigation().isDone() || linkle.getMode() != LinkleMode.FOLLOW || linkle.unableToMoveToOwner()) {
			return false;
		}
		double stop = LinkleConfig.get().followStopDistance;
		return owner.isAlive() && owner.level() == linkle.level() && linkle.distanceToSqr(owner) > stop * stop;
	}

	@Override
	public void start() {
		this.timeToRecalcPath = 0;
		this.oldWaterCost = linkle.getPathfindingMalus(PathType.WATER);
		linkle.setPathfindingMalus(PathType.WATER, 0.0F);
	}

	@Override
	public void stop() {
		this.owner = null;
		linkle.getNavigation().stop();
		linkle.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
	}

	@Override
	public void tick() {
		double distanceSqr = linkle.distanceToSqr(owner);
		double teleport = LinkleConfig.get().teleportDistance;
		boolean tooFar = LinkleConfig.get().teleportToOwner && distanceSqr >= teleport * teleport;
		if (!tooFar) {
			linkle.getLookControl().setLookAt(owner, 10.0F, linkle.getMaxHeadXRot());
		}
		if (--this.timeToRecalcPath > 0) {
			return;
		}
		this.timeToRecalcPath = this.adjustedTickDelay(10);
		if (tooFar && owner instanceof ServerPlayer player && !player.isSpectator()) {
			LinkleSummoning.teleportToOwner(linkle, player);
		} else {
			// Jog to catch up when far behind, walk when close.
			double speed = distanceSqr > 100.0 ? 1.35 : 1.0;
			linkle.getNavigation().moveTo(owner, speed);
		}
	}
}
