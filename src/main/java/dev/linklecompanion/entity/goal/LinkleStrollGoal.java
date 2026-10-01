package dev.linklecompanion.entity.goal;

import dev.linklecompanion.entity.LinkleEntity;
import dev.linklecompanion.entity.LinkleMode;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;

/**
 * Small wanders: a wild Linkle strolls around, and in Follow mode she may wander a few steps while
 * her owner is standing still nearby. Never in Stay or Guard mode.
 */
public class LinkleStrollGoal extends WaterAvoidingRandomStrollGoal {
	private final LinkleEntity linkle;

	public LinkleStrollGoal(LinkleEntity linkle) {
		super(linkle, 0.8);
		this.linkle = linkle;
	}

	@Override
	public boolean canUse() {
		if (linkle.isTame()) {
			if (linkle.getMode() != LinkleMode.FOLLOW) {
				return false;
			}
			LivingEntity owner = linkle.getOwner();
			if (owner == null || owner.level() != linkle.level() || linkle.distanceToSqr(owner) > 36.0) {
				return false;
			}
		}
		return super.canUse();
	}
}
