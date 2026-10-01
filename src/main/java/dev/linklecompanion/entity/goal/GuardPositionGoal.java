package dev.linklecompanion.entity.goal;

import dev.linklecompanion.entity.LinkleEntity;
import dev.linklecompanion.entity.LinkleMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** Guard mode: walks back to her guard point whenever she drifted away (for example after a fight). */
public class GuardPositionGoal extends Goal {
	private final LinkleEntity linkle;
	private int recalcDelay;

	public GuardPositionGoal(LinkleEntity linkle) {
		this.linkle = linkle;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		BlockPos guard = linkle.getGuardPos();
		return linkle.getMode() == LinkleMode.GUARD && guard != null && linkle.blockPosition().distSqr(guard) > 9.0;
	}

	@Override
	public boolean canContinueToUse() {
		BlockPos guard = linkle.getGuardPos();
		return linkle.getMode() == LinkleMode.GUARD && guard != null && linkle.blockPosition().distSqr(guard) > 2.0;
	}

	@Override
	public void start() {
		recalcDelay = 0;
	}

	@Override
	public void stop() {
		linkle.getNavigation().stop();
	}

	@Override
	public void tick() {
		BlockPos guard = linkle.getGuardPos();
		if (guard != null && --recalcDelay <= 0) {
			recalcDelay = adjustedTickDelay(20);
			linkle.getNavigation().moveTo(guard.getX() + 0.5, guard.getY(), guard.getZ() + 0.5, 1.0);
		}
	}
}
