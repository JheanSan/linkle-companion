package dev.linklecompanion.compat;

import dev.linklecompanion.LinkleCompanion;
import net.fabricmc.fabric.api.permission.v1.PermissionPredicates;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.Entity;

import java.util.function.Predicate;

/**
 * The few calls that differ between Minecraft versions. This copy is for 26.2 and newer; each
 * version family has its own copy in src/compat/&lt;family&gt; with the same methods (see
 * docs/VERSIONS.md). Keep them in sync.
 */
public final class VersionCompat {
	private VersionCompat() {
	}

	/** Who may run a /linkle subcommand: permission node {@code linkle_companion.<node>}, vanilla level as fallback. */
	public static Predicate<CommandSourceStack> permission(String node, boolean operatorsOnly) {
		return PermissionPredicates.require(LinkleCompanion.id(node), operatorsOnly ? PermissionLevel.GAMEMASTERS : PermissionLevel.ALL);
	}

	/** Fires the vanilla "summoned an entity" advancement trigger. */
	public static void triggerSummoned(ServerPlayer player, Entity entity) {
		CriteriaTriggers.SUMMONED_ENTITY.trigger(player, entity);
	}
}
