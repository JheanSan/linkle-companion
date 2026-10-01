package dev.linklecompanion.entity.goal;

import dev.linklecompanion.entity.LinkleEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Idle flavor: every now and then, when nothing is happening, she lifts a crossbow and looks it
 * over for a few seconds. The pose itself is drawn by the client model.
 */
public class InspectCrossbowsGoal extends Goal {
	private final LinkleEntity linkle;
	private int ticksLeft;

	public InspectCrossbowsGoal(LinkleEntity linkle) {
		this.linkle = linkle;
		this.setFlags(EnumSet.of(Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		return linkle.getRandom().nextInt(reducedTickDelay(900)) == 0
			&& linkle.getTarget() == null
			&& !linkle.isKnockedOut()
			&& linkle.getNavigation().isDone()
			&& !linkle.isVolleying();
	}

	@Override
	public boolean canContinueToUse() {
		return ticksLeft > 0 && linkle.getTarget() == null && !linkle.isKnockedOut();
	}

	@Override
	public void start() {
		ticksLeft = adjustedTickDelay(50 + linkle.getRandom().nextInt(30));
		linkle.setInspecting(true);
	}

	@Override
	public void stop() {
		linkle.setInspecting(false);
	}

	@Override
	public void tick() {
		ticksLeft--;
	}
}
