package dev.linklecompanion.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.linklecompanion.LinkleCompanion;
import dev.linklecompanion.client.screen.LinkleSettingsScreen;
import dev.linklecompanion.network.LinkleActionPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

/**
 * Hotkeys, all rebindable in Options > Controls > Key Binds > "Linkle Companion":
 * G calls Linkle to you, H switches her mode, J opens her inventory (within 16 blocks), and
 * "Linkle settings" (unbound by default) opens the settings screen.
 */
public final class LinkleKeys {
	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(LinkleCompanion.id("keys"));

	private static KeyMapping recall;
	private static KeyMapping cycleMode;
	private static KeyMapping settings;
	private static KeyMapping inventory;

	private LinkleKeys() {
	}

	public static void register() {
		recall = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.linkle_companion.recall", InputConstants.KEY_G, CATEGORY));
		cycleMode = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.linkle_companion.mode", InputConstants.KEY_H, CATEGORY));
		inventory = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.linkle_companion.inventory", InputConstants.KEY_J, CATEGORY));
		settings = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.linkle_companion.settings", InputConstants.UNKNOWN.getValue(), CATEGORY));
		ClientTickEvents.END_CLIENT_TICK.register(LinkleKeys::tick);
	}

	private static void tick(Minecraft minecraft) {
		while (recall.consumeClick()) {
			send(LinkleActionPayload.RECALL);
		}
		while (cycleMode.consumeClick()) {
			send(LinkleActionPayload.CYCLE_MODE);
		}
		while (inventory.consumeClick()) {
			send(LinkleActionPayload.OPEN_INVENTORY);
		}
		while (settings.consumeClick()) {
			if (minecraft.gui.screen() == null) {
				minecraft.gui.setScreen(new LinkleSettingsScreen(null));
			}
		}
	}

	private static void send(int action) {
		// Only when connected to a server that has the mod (it always does when Linkle exists).
		if (ClientPlayNetworking.canSend(LinkleActionPayload.TYPE)) {
			ClientPlayNetworking.send(new LinkleActionPayload(action));
		}
	}
}
