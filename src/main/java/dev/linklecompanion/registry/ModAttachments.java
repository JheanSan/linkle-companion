package dev.linklecompanion.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.linklecompanion.LinkleCompanion;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

/**
 * Data stored on each player (saved in the player file through Fabric's attachment API).
 * It remembers which Linkle belongs to the player so there is only one per player.
 */
public final class ModAttachments {
	/**
	 * @param linkleId   UUID of the player's current Linkle
	 * @param generation goes up every time a brand-new Linkle is summoned; an older copy that
	 *                   still carries a smaller number knows it has been replaced and leaves
	 */
	public record CompanionData(UUID linkleId, int generation) {
		public static final Codec<CompanionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			UUIDUtil.CODEC.fieldOf("linkle").forGetter(CompanionData::linkleId),
			Codec.INT.fieldOf("generation").forGetter(CompanionData::generation)
		).apply(instance, CompanionData::new));
	}

	public static final AttachmentType<CompanionData> COMPANION = AttachmentRegistry.create(
		LinkleCompanion.id("companion"),
		builder -> builder.persistent(CompanionData.CODEC).copyOnDeath()
	);

	private ModAttachments() {
	}

	public static void register() {
		// Static initializer does the work; this method just makes sure the class is loaded.
	}
}
