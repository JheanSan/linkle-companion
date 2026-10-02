package dev.linklecompanion.network;

import dev.linklecompanion.LinkleCompanion;
import dev.linklecompanion.config.LinkleConfig;
import dev.linklecompanion.dialogue.Topic;
import dev.linklecompanion.entity.LinkleEntity;
import dev.linklecompanion.entity.LinkleMode;
import dev.linklecompanion.entity.LinkleSummoning;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * Client -> server: a hotkey was pressed ("call Linkle" or "switch her mode").
 * The server checks everything itself; the client only says which key.
 */
public record LinkleActionPayload(int action) implements CustomPacketPayload {
	public static final int RECALL = 0;
	public static final int CYCLE_MODE = 1;
	public static final int OPEN_INVENTORY = 2;
	/** The inventory hotkey works within this distance (blocks). */
	public static final double INVENTORY_RANGE = 16.0;

	public static final Type<LinkleActionPayload> TYPE = new Type<>(LinkleCompanion.id("action"));
	public static final StreamCodec<RegistryFriendlyByteBuf, LinkleActionPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, LinkleActionPayload::action,
		LinkleActionPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	/** Server side: runs on the server thread. */
	public static void handle(LinkleActionPayload payload, ServerPlayNetworking.Context context) {
		ServerPlayer player = context.player();
		if (!LinkleConfig.get().enabled) {
			player.sendOverlayMessage(Component.translatable("message.linkle_companion.disabled"));
			return;
		}
		LinkleEntity linkle = LinkleSummoning.findLoaded(player);
		if (linkle == null || !linkle.isOwnedByPlayer(player)) {
			player.sendOverlayMessage(Component.translatable("message.linkle_companion.no_loaded"));
			return;
		}
		switch (payload.action) {
			case RECALL -> {
				LinkleEntity moved = LinkleSummoning.teleportToOwner(linkle, player);
				moved.dialogue().say(Topic.RECALL);
				player.sendOverlayMessage(Component.translatable("message.linkle_companion.recalled"));
			}
			case CYCLE_MODE -> {
				if (linkle.isKnockedOut()) {
					player.sendOverlayMessage(Component.translatable("message.linkle_companion.knocked_out_hint"));
					return;
				}
				LinkleMode next = linkle.getMode().next();
				linkle.setMode(next);
				player.sendOverlayMessage(Component.translatable("message.linkle_companion.mode." + next.id()));
			}
			case OPEN_INVENTORY -> {
				if (linkle.level() != player.level() || linkle.distanceToSqr(player) > INVENTORY_RANGE * INVENTORY_RANGE) {
					player.sendOverlayMessage(Component.translatable("message.linkle_companion.too_far"));
					return;
				}
				linkle.openInventory(player);
			}
			default -> {
			}
		}
	}
}
