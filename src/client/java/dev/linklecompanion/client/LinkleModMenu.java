package dev.linklecompanion.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.linklecompanion.client.screen.LinkleSettingsScreen;

/**
 * Optional Mod Menu integration: adds a "Configure" button for Linkle Companion.
 * Mod Menu is NOT required; without it this class is simply never loaded.
 */
public class LinkleModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return LinkleSettingsScreen::new;
	}
}
