package dev.linklecompanion.item;

import dev.linklecompanion.entity.LinkleSummoning;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * The Wanderer's Compass, Linkle's summon item.
 * <ul>
 *   <li>Use it: summons Linkle, or calls her back to you if she already exists and is loaded.</li>
 *   <li>Sneak + use when she is lost somewhere unloaded: summons a fresh Linkle; the old one
 *       leaves (dropping her items) the next time her area is loaded.</li>
 * </ul>
 * It is not used up, so it doubles as a recall whistle.
 */
public class WanderersCompassItem extends Item {
	public WanderersCompassItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.SUCCESS;
		}
		LinkleSummoning.Result result = LinkleSummoning.summonOrRecall(serverPlayer, player.isSecondaryUseActive());
		switch (result) {
			case SUMMONED -> serverPlayer.sendOverlayMessage(Component.translatable("message.linkle_companion.summoned"));
			case RECALLED -> serverPlayer.sendOverlayMessage(Component.translatable("message.linkle_companion.recalled"));
			case FAILED -> serverPlayer.sendOverlayMessage(Component.translatable("message.linkle_companion.failed"));
			case ELSEWHERE -> {
			}
		}
		player.getCooldowns().addCooldown(player.getItemInHand(hand), 40);
		return result == LinkleSummoning.Result.FAILED || result == LinkleSummoning.Result.ELSEWHERE
			? InteractionResult.FAIL
			: InteractionResult.SUCCESS_SERVER;
	}
}
