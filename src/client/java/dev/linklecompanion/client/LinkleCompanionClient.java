package dev.linklecompanion.client;

import dev.linklecompanion.client.hud.DialogueHud;
import dev.linklecompanion.client.hud.SpeechBubbles;
import dev.linklecompanion.client.lang.ModLanguage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
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
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

/** Client entry point: rendering, the inventory screen, the dialogue box and the mod language. */
public class LinkleCompanionClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLayerRegistry.registerModelLayer(LinkleRenderer.LAYER, LinkleModel::createLayer);
		EntityRendererRegistry.register(ModEntities.LINKLE, LinkleRenderer::new);
		MenuScreens.register(ModMenus.LINKLE, LinkleScreen::new);
		HudElementRegistry.addLast(DialogueHud.ID, new DialogueHud());
		ClientPlayNetworking.registerGlobalReceiver(DialoguePayload.TYPE, (payload, context) -> DialogueHud.show(payload));
		LinkleKeys.register();
		// Mod language setting: re-applied after every language (resource) reload.
		ResourceLoader resources = ResourceLoader.get(PackType.CLIENT_RESOURCES);
		resources.registerReloadListener(ModLanguage.RELOAD_ID, (ResourceManagerReloadListener) ModLanguage::onReload);
		resources.addListenerOrdering(ResourceReloaderKeys.Client.LANGUAGES, ModLanguage.RELOAD_ID);
		// Entity ids are reused between worlds, so forget bubbles when leaving one.
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> SpeechBubbles.clear());
	}
}
