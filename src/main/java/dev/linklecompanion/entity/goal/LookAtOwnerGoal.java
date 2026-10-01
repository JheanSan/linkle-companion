package dev.linklecompanion.entity.goal;

import dev.linklecompanion.entity.LinkleEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** Idle flavor: now and then she turns her head to look at her owner for a couple of seconds. */
public class LookAtOwnerGoal extends Goal {
	private static final double RANGE_SQR = 10.0 * 10.0;

	private final LinkleEntity linkle;
	private LivingEntity owner;
	private int ticksLeft;

	public LookAtOwnerGoal(LinkleEntity linkle) {
		this.linkle = linkle;
		this.setFlags(EnumSet.of(Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (linkle.getRandom().nextFloat() >= 0.03F || linkle.getTarget() != null) {
			return false;
		}
		LivingEntity candidate = linkle.getOwner();
		if (candidate == null || candidate.level() != linkle.level() || linkle.distanceToSqr(candidate) > RANGE_SQR) {
			return false;
		}
		owner = candidate;
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		return ticksLeft > 0 && owner.isAlive() && linkle.distanceToSqr(owner) <= RANGE_SQR;
	}

	@Override
	public void start() {
		ticksLeft = adjustedTickDelay(40 + linkle.getRandom().nextInt(40));
	}

	@Override
	public void stop() {
		owner = null;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		ticksLeft--;
		linkle.getLookControl().setLookAt(owner.getX(), owner.getEyeY(), owner.getZ());
	}
}
