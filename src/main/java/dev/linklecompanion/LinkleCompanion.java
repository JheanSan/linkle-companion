package dev.linklecompanion;

import dev.linklecompanion.config.LinkleConfig;
import dev.linklecompanion.network.DialoguePayload;
import dev.linklecompanion.registry.ModAttachments;
import dev.linklecompanion.registry.ModEntities;
import dev.linklecompanion.registry.ModItems;
import dev.linklecompanion.registry.ModMenus;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common entry point (runs on both the client and the dedicated server).
 * Client-only code (rendering, screens, HUD) lives in src/client.
 */
public class LinkleCompanion implements ModInitializer {
	public static final String MOD_ID = "linkle_companion";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LinkleConfig.load();
		ModEntities.register();
		ModItems.register();
		ModMenus.register();
		ModAttachments.register();
		PayloadTypeRegistry.clientboundPlay().register(DialoguePayload.TYPE, DialoguePayload.CODEC);
		LinkleEvents.register();
		LOGGER.info("Linkle Companion ready");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
