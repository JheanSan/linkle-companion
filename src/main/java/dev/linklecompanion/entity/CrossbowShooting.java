package dev.linklecompanion.entity;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.phys.Vec3;

/**
 * Fires Linkle's crossbows. Loading uses the real vanilla crossbow (so the item shows its pull and
 * loaded states); firing is done here so we can apply the damage config, the pickup rule and fire
 * from the correct hand.
 */
public final class CrossbowShooting {
	private static final float BOLT_SPEED = 3.15F;
	private static final float INACCURACY = 1.5F;

	private CrossbowShooting() {
	}

	public static boolean isCharged(LivingEntity shooter, InteractionHand hand) {
		ItemStack stack = shooter.getItemInHand(hand);
		return stack.is(Items.CROSSBOW) && CrossbowItem.isCharged(stack);
	}

	/** Fires whatever is loaded in this hand's crossbow at the target. */
	public static void fire(LinkleEntity linkle, InteractionHand hand, LivingEntity target) {
		if (!(linkle.level() instanceof ServerLevel level)) {
			return;
		}
		ItemStack weapon = linkle.getItemInHand(hand);
		ChargedProjectiles charged = weapon.get(DataComponents.CHARGED_PROJECTILES);
		if (charged == null || charged.isEmpty()) {
			return;
		}
		weapon.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);

		// Shift the bolt toward the hand that fires it, so left and right shots look right.
		boolean rightHand = (hand == InteractionHand.MAIN_HAND) == (linkle.getMainArm() == HumanoidArm.RIGHT);
		float yaw = linkle.yBodyRot * Mth.DEG_TO_RAD;
		double side = rightHand ? 0.32 : -0.32;
		double offsetX = -Mth.cos(yaw) * side;
		double offsetZ = -Mth.sin(yaw) * side;

		charged.itemCopies().forEach(ammo -> {
			ArrowItem arrowItem = ammo.getItem() instanceof ArrowItem item ? item : (ArrowItem) Items.ARROW;
			AbstractArrow arrow = arrowItem.createArrow(level, ammo, linkle, weapon);
			arrow.setPos(arrow.getX() + offsetX, arrow.getY(), arrow.getZ() + offsetZ);
			linkle.prepareBolt(arrow, !ammo.has(DataComponents.INTANGIBLE_PROJECTILE));
			double dx = target.getX() - arrow.getX();
			double dz = target.getZ() - arrow.getZ();
			double horizontal = Math.sqrt(dx * dx + dz * dz);
			double dy = target.getY(0.3333333333333333) - arrow.getY() + horizontal * 0.2F;
			arrow.shoot(dx, dy, dz, BOLT_SPEED, INACCURACY);
			level.addFreshEntity(arrow);
		});

		level.playSound(null, linkle.getX(), linkle.getY(), linkle.getZ(), SoundEvents.CROSSBOW_SHOOT, linkle.getSoundSource(),
			1.0F, 0.95F + linkle.getRandom().nextFloat() * 0.15F);
		linkle.onCrossbowAttackPerformed();
	}

	/** One bolt of the spinning volley, flying flat in the given horizontal direction. */
	public static void fireVolleyBolt(LinkleEntity linkle, double dirX, double dirZ) {
		if (!(linkle.level() instanceof ServerLevel level)) {
			return;
		}
		Arrow arrow = new Arrow(level, linkle, new ItemStack(Items.ARROW), linkle.getMainHandItem());
		linkle.prepareBolt(arrow, false);
		arrow.setCritArrow(true);
		Vec3 start = linkle.position().add(dirX * 0.6, linkle.getBbHeight() * 0.6, dirZ * 0.6);
		arrow.setPos(start.x, start.y, start.z);
		arrow.shoot(dirX, 0.04, dirZ, 2.2F, 1.0F);
		level.addFreshEntity(arrow);
	}
}
