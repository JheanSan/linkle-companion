package dev.linklecompanion.compat;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.function.Predicate;

/**
 * The few calls that differ between Minecraft versions. This copy is for 26.1.x; each version
 * family has its own copy in src/compat/&lt;family&gt; with the same methods (see docs/VERSIONS.md).
 */
public final class VersionCompat {
	private VersionCompat() {
	}

	/**
	 * Who may run a /linkle subcommand. Fabric API for 26.1 has no permission API, so this uses the
	 * vanilla operator levels only (permission mods can't change it on 26.1).
	 */
	public static Predicate<CommandSourceStack> permission(String node, boolean operatorsOnly) {
		return Commands.hasPermission(operatorsOnly ? Commands.LEVEL_GAMEMASTERS : Commands.LEVEL_ALL);
	}

	/** Fires the vanilla "summoned an entity" advancement trigger. */
	public static void triggerSummoned(ServerPlayer player, Entity entity) {
		CriteriaTriggers.SUMMONED_ENTITY.trigger(player, entity);
	}
}
