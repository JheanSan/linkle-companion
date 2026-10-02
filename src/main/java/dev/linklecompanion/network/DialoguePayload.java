package dev.linklecompanion.network;

import dev.linklecompanion.LinkleCompanion;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server -> nearby clients: "Linkle says this line". Only the lang key travels; the text itself
 * lives in each client's language file, so it is translated on each player's machine.
 *
 * @param entityId which Linkle is talking (for the speech bubble above her head)
 * @param variant  skin id, so the corner box can show the right face
 * @param key      translation key of the line
 */
public record DialoguePayload(int entityId, String variant, String key) implements CustomPacketPayload {
	public static final Type<DialoguePayload> TYPE = new Type<>(LinkleCompanion.id("dialogue"));
	public static final StreamCodec<RegistryFriendlyByteBuf, DialoguePayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, DialoguePayload::entityId,
		ByteBufCodecs.stringUtf8(32), DialoguePayload::variant,
		ByteBufCodecs.stringUtf8(128), DialoguePayload::key,
		DialoguePayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
