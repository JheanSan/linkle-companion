package dev.linklecompanion.registry;

import dev.linklecompanion.LinkleCompanion;
import dev.linklecompanion.menu.LinkleMenu;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {
	/** Opening data is Linkle's entity id, so the client knows whose inventory it is. */
	public static final MenuType<LinkleMenu> LINKLE = Registry.register(
		BuiltInRegistries.MENU,
		LinkleCompanion.id("linkle"),
		new ExtendedMenuType<>(LinkleMenu::fromNetwork, ByteBufCodecs.VAR_INT)
	);

	private ModMenus() {
	}

	public static void register() {
		// Static initializer does the work.
	}
}
