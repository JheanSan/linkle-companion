package dev.linklecompanion.gametest;

import dev.linklecompanion.client.compat.ClientCompat;
import dev.linklecompanion.LinkleCompanion;
import dev.linklecompanion.client.lang.ModLanguage;
import dev.linklecompanion.client.screen.LinkleLanguageScreen;
import dev.linklecompanion.client.screen.LinkleSettingsScreen;
import dev.linklecompanion.config.LinkleConfig;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.client.resources.language.I18n;

import java.util.concurrent.CompletableFuture;

/**
 * Mod language test ({@code ./gradlew runClientGameTest}): the "Mod language" setting changes
 * Linkle's text without touching the rest of the game, "Same as game" follows the game, and a game
 * language without a Linkle file borrows a close one. Screenshots of the settings and the picker.
 */
public class LinkleLanguageClientTest implements FabricClientGameTest {
	private static final String ENTITY = "entity.linkle_companion.linkle";
	private static final String COMPASS = "item.linkle_companion.wanderers_compass";
	private static final String VANILLA = "gui.done";

	@Override
	public void runTest(ClientGameTestContext context) {
		String vanillaEnglish = text(context, VANILLA);
		expect(context, ENTITY, "Linkle");
		expect(context, COMPASS, "Wanderer's Compass");

		// Mod language override: Linkle in Japanese, Minecraft stays English.
		setModLanguage(context, "ja_jp");
		expect(context, ENTITY, "リンクル");
		expect(context, COMPASS, "旅人のコンパス");
		expect(context, VANILLA, vanillaEnglish);
		context.runOnClient(client -> ClientCompat.setScreen(new LinkleSettingsScreen(null)));
		context.waitTicks(5);
		shot(context, "language_01_settings_ja");
		context.runOnClient(client -> ClientCompat.setScreen(new LinkleLanguageScreen(new LinkleSettingsScreen(null), null)));
		context.waitTicks(5);
		shot(context, "language_02_picker");
		context.runOnClient(client -> ClientCompat.setScreen(null));

		// Back to "Same as game": English again.
		setModLanguage(context, ModLanguage.AUTO);
		expect(context, ENTITY, "Linkle");
		expect(context, VANILLA, vanillaEnglish);

		// Game in Mexican Spanish (no Linkle file): borrows es_es instead of falling back to English.
		setGameLanguage(context, "es_mx");
		expect(context, COMPASS, "Brújula del viajero");

		// Game in German, Linkle in Korean: each keeps its own language.
		setGameLanguage(context, "de_de");
		String vanillaGerman = text(context, VANILLA);
		if (vanillaGerman.equals(vanillaEnglish)) {
			throw new AssertionError("Game language did not switch to German");
		}
		expect(context, COMPASS, "Kompass des Wanderers");
		setModLanguage(context, "ko_kr");
		expect(context, COMPASS, "방랑자의 나침반");
		expect(context, VANILLA, vanillaGerman);

		// A language survives a resource reload (F3+T, resource pack changes).
		reload(context);
		expect(context, COMPASS, "방랑자의 나침반");

		// Leave the test client as we found it.
		setModLanguage(context, ModLanguage.AUTO);
		setGameLanguage(context, "en_us");
		expect(context, COMPASS, "Wanderer's Compass");
		int languages = context.computeOnClient(client -> ModLanguage.available().size());
		LinkleCompanion.LOGGER.info("Language test passed: {} shipped languages", languages);
	}

	private static void setModLanguage(ClientGameTestContext context, String code) {
		context.runOnClient(client -> {
			LinkleConfig.get().language = code;
			ModLanguage.apply();
		});
	}

	private static void setGameLanguage(ClientGameTestContext context, String code) {
		context.runOnClient(client -> {
			client.getLanguageManager().setSelected(code);
			client.options.languageCode = code;
		});
		reload(context);
	}

	private static void reload(ClientGameTestContext context) {
		CompletableFuture<Void> done = context.computeOnClient(client -> client.reloadResourcePacks());
		context.waitFor(client -> done.isDone() && !ClientCompat.isLoading(), 1200);
	}

	private static String text(ClientGameTestContext context, String key) {
		return context.computeOnClient(client -> I18n.get(key));
	}

	private static void expect(ClientGameTestContext context, String key, String expected) {
		String actual = text(context, key);
		if (!expected.equals(actual)) {
			throw new AssertionError(key + ": expected '" + expected + "' but was '" + actual + "'");
		}
		LinkleCompanion.LOGGER.info("Language check OK: {} = {}", key, actual);
	}

	private static void shot(ClientGameTestContext context, String name) {
		LinkleCompanion.LOGGER.info("Language screenshot: {}", context.takeScreenshot(TestScreenshotOptions.of("linkle_" + name)));
	}
}
