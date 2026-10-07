package dev.linklecompanion.client.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * Client calls that differ between Minecraft versions. This copy is for 26.1.x; each version
 * family has its own copy in src/compat/&lt;family&gt;/client with the same methods.
 */
public final class ClientCompat {
	private ClientCompat() {
	}

	private static @Nullable Method i18nSetLanguage;
	private static boolean i18nFailed;

	/**
	 * Makes this the language all text lookups use. 26.1 keeps a second copy in {@link I18n} whose setter
	 * isn't public; it is set by reflection (no mixin). If that ever fails, text built with components
	 * still follows the setting and one warning is logged.
	 */
	public static void installLanguage(Language language) {
		Language.inject(language);
		if (i18nFailed) {
			return;
		}
		try {
			if (i18nSetLanguage == null) {
				i18nSetLanguage = I18n.class.getDeclaredMethod("setLanguage", Language.class);
				i18nSetLanguage.setAccessible(true);
			}
			i18nSetLanguage.invoke(null, language);
		} catch (ReflectiveOperationException | RuntimeException e) {
			i18nFailed = true;
			dev.linklecompanion.LinkleCompanion.LOGGER.warn("Mod language: could not update I18n ({}); some texts may stay in the game language.", e.toString());
		}
	}

	public static void setScreen(@Nullable Screen screen) {
		Minecraft.getInstance().setScreen(screen);
	}

	public static @Nullable Screen currentScreen() {
		return Minecraft.getInstance().screen;
	}

	/** True while the resource loading screen is up. */
	public static boolean isLoading() {
		return Minecraft.getInstance().getOverlay() != null;
	}

	/** Shows text on the action bar above the hotbar. */
	public static void showActionBar(Component text) {
		Minecraft.getInstance().gui.setOverlayMessage(text, false);
	}

	/** Hides or shows the HUD (F1). */
	public static void setHudHidden(boolean hidden) {
		Minecraft.getInstance().options.hideGui = hidden;
	}

	/** Adds a wide widget (a button) to an options list; 26.1 lists only take it as a single small cell. */
	public static void addWide(OptionsList list, AbstractWidget widget) {
		list.addSmall(java.util.List.of(widget));
	}

	/** One name-tag style line of text above an entity. */
	public static void submitNameTag(SubmitNodeCollector collector, PoseStack poseStack, Vec3 attachment, int offset,
			Component text, int light, EntityRenderState state, CameraRenderState camera) {
		collector.submitNameTag(poseStack, attachment, offset, text, true, light, state.distanceToCameraSq, camera);
	}
}
