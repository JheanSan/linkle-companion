package dev.linklecompanion.client;

import dev.linklecompanion.client.hud.DialogueHud;
import dev.linklecompanion.client.render.LinkleModel;
import dev.linklecompanion.client.render.LinkleRenderer;
import dev.linklecompanion.client.screen.LinkleScreen;
import dev.linklecompanion.network.DialoguePayload;
import dev.linklecompanion.registry.ModEntities;
import dev.linklecompanion.registry.ModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.gui.screens.MenuScreens;

/** Client entry point: rendering, the inventory screen and the dialogue box. */
public class LinkleCompanionClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLayerRegistry.registerModelLayer(LinkleRenderer.LAYER, LinkleModel::createLayer);
		EntityRendererRegistry.register(ModEntities.LINKLE, LinkleRenderer::new);
		MenuScreens.register(ModMenus.LINKLE, LinkleScreen::new);
		HudElementRegistry.addLast(DialogueHud.ID, new DialogueHud());
		ClientPlayNetworking.registerGlobalReceiver(DialoguePayload.TYPE, (payload, context) -> DialogueHud.show(payload));
		LinkleKeys.register();
	}
}
