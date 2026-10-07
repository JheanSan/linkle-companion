package dev.linklecompanion.client.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Client calls that differ between Minecraft versions. This copy is for 26.2 and newer; each
 * version family has its own copy in src/compat/&lt;family&gt;/client with the same methods.
 */
public final class ClientCompat {
	private ClientCompat() {
	}

	/** Makes this the language all text lookups use. */
	public static void installLanguage(Language language) {
		Language.inject(language);
	}

	public static void setScreen(@Nullable Screen screen) {
		Minecraft.getInstance().gui.setScreen(screen);
	}

	public static @Nullable Screen currentScreen() {
		return Minecraft.getInstance().gui.screen();
	}

	/** True while the resource loading screen is up. */
	public static boolean isLoading() {
		return Minecraft.getInstance().gui.overlay() != null;
	}

	/** Shows text on the action bar above the hotbar. */
	public static void showActionBar(Component text) {
		Minecraft.getInstance().gui.hud.setOverlayMessage(text, false);
	}

	/** Hides or shows the HUD (F1). */
	public static void setHudHidden(boolean hidden) {
		var hud = Minecraft.getInstance().gui.hud;
		if (hud.isHidden() != hidden) {
			hud.toggle();
		}
	}

	/** Adds a full-width widget (a button) to an options list. */
	public static void addWide(OptionsList list, AbstractWidget widget) {
		list.addBig(widget);
	}

	/** One name-tag style line of text above an entity. */
	public static void submitNameTag(SubmitNodeCollector collector, PoseStack poseStack, Vec3 attachment, int offset,
			Component text, int light, EntityRenderState state, CameraRenderState camera) {
		collector.submitNameTag(poseStack, attachment, offset, text, true, light, camera);
	}
}
