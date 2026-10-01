package dev.linklecompanion.entity.goal;

import dev.linklecompanion.entity.LinkleEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** While knocked out, this goal holds every movement flag so no other goal can move her. */
public class KnockedOutGoal extends Goal {
	private final LinkleEntity linkle;

	public KnockedOutGoal(LinkleEntity linkle) {
		this.linkle = linkle;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		return linkle.isKnockedOut();
	}

	@Override
	public void start() {
		linkle.getNavigation().stop();
	}
}
