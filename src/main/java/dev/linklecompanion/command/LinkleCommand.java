package dev.linklecompanion.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.linklecompanion.LinkleCompanion;
import dev.linklecompanion.entity.LinkleEntity;
import dev.linklecompanion.entity.LinkleMode;
import dev.linklecompanion.entity.LinkleSummoning;
import dev.linklecompanion.entity.LinkleVariant;
import dev.linklecompanion.registry.ModAttachments;
import net.fabricmc.fabric.api.permission.v1.PermissionPredicates;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;

import java.util.Arrays;

/**
 * {@code /linkle ...} commands. Each one has a permission node
 * ({@code linkle_companion.command.<name>}) so permission mods can allow or deny it; without such
 * a mod, {@code summon} needs operator level 2 and the rest are open to everyone.
 */
public final class LinkleCommand {
	private LinkleCommand() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("linkle")
			.then(Commands.literal("summon")
				.requires(PermissionPredicates.require(LinkleCompanion.id("command.summon"), PermissionLevel.GAMEMASTERS))
				.executes(ctx -> summon(ctx, false))
				.then(Commands.literal("new").executes(ctx -> summon(ctx, true))))
			.then(Commands.literal("recall")
				.requires(PermissionPredicates.require(LinkleCompanion.id("command.recall"), PermissionLevel.ALL))
				.executes(LinkleCommand::recall))
			.then(Commands.literal("mode")
				.requires(PermissionPredicates.require(LinkleCompanion.id("command.mode"), PermissionLevel.ALL))
				.then(Commands.argument("mode", StringArgumentType.word())
					.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(LinkleMode.values()).map(LinkleMode::id), builder))
					.executes(LinkleCommand::mode)))
			.then(Commands.literal("skin")
				.requires(PermissionPredicates.require(LinkleCompanion.id("command.skin"), PermissionLevel.ALL))
				.then(Commands.argument("skin", StringArgumentType.word())
					.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(LinkleVariant.values()).map(LinkleVariant::id), builder))
					.executes(LinkleCommand::skin)))
			.then(Commands.literal("info")
				.requires(PermissionPredicates.require(LinkleCompanion.id("command.info"), PermissionLevel.ALL))
				.executes(LinkleCommand::info))
			.then(Commands.literal("dismiss")
				.requires(PermissionPredicates.require(LinkleCompanion.id("command.dismiss"), PermissionLevel.ALL))
				.then(Commands.literal("confirm").executes(LinkleCommand::dismiss))));
	}

	private static int summon(CommandContext<CommandSourceStack> ctx, boolean forceNew) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		LinkleSummoning.Result result = LinkleSummoning.summonOrRecall(player, forceNew);
		String key = switch (result) {
			case SUMMONED -> "message.linkle_companion.summoned";
			case RECALLED -> "message.linkle_companion.recalled";
			case ELSEWHERE -> null;
			case FAILED -> "message.linkle_companion.failed";
		};
		if (key != null) {
			ctx.getSource().sendSuccess(() -> Component.translatable(key), false);
		}
		return result == LinkleSummoning.Result.SUMMONED || result == LinkleSummoning.Result.RECALLED ? 1 : 0;
	}

	private static LinkleEntity requireLinkle(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		LinkleEntity linkle = LinkleSummoning.findLoaded(player);
		if (linkle == null || !linkle.isOwnedByPlayer(player)) {
			ctx.getSource().sendFailure(Component.translatable(player.hasAttached(ModAttachments.COMPANION)
				? "message.linkle_companion.elsewhere" : "message.linkle_companion.none"));
			return null;
		}
		return linkle;
	}

	private static int recall(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		LinkleEntity linkle = requireLinkle(ctx);
		if (linkle == null) {
			return 0;
		}
		LinkleSummoning.teleportToOwner(linkle, ctx.getSource().getPlayerOrException());
		ctx.getSource().sendSuccess(() -> Component.translatable("message.linkle_companion.recalled"), false);
		return 1;
	}

	private static int mode(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		LinkleMode mode = LinkleMode.byId(StringArgumentType.getString(ctx, "mode"));
		if (mode == null) {
			ctx.getSource().sendFailure(Component.translatable("message.linkle_companion.unknown_mode"));
			return 0;
		}
		LinkleEntity linkle = requireLinkle(ctx);
		if (linkle == null) {
			return 0;
		}
		linkle.setMode(mode);
		ctx.getSource().sendSuccess(() -> Component.translatable("message.linkle_companion.mode." + mode.id()), false);
		return 1;
	}

	private static int skin(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		LinkleVariant variant = LinkleVariant.byIdOrNull(StringArgumentType.getString(ctx, "skin"));
		if (variant == null) {
			ctx.getSource().sendFailure(Component.translatable("message.linkle_companion.unknown_skin"));
			return 0;
		}
		LinkleEntity linkle = requireLinkle(ctx);
		if (linkle == null) {
			return 0;
		}
		linkle.setVariant(variant);
		ctx.getSource().sendSuccess(() -> Component.translatable("message.linkle_companion.skin_set", variant.id()), false);
		return 1;
	}

	private static int info(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		LinkleEntity linkle = requireLinkle(ctx);
		if (linkle == null) {
			return 0;
		}
		int arrows = 0;
		for (int slot = 0; slot < linkle.getInventory().getContainerSize(); slot++) {
			var stack = linkle.getInventory().getItem(slot);
			if (stack.is(net.minecraft.tags.ItemTags.ARROWS)) {
				arrows += stack.getCount();
			}
		}
		int finalArrows = arrows;
		ctx.getSource().sendSuccess(() -> Component.translatable("message.linkle_companion.info",
			Component.translatable("mode.linkle_companion." + linkle.getMode().id()),
			Math.round(linkle.getHealth()), Math.round(linkle.getMaxHealth()),
			finalArrows,
			linkle.getBlockX(), linkle.getBlockY(), linkle.getBlockZ(),
			linkle.level().dimension().identifier().toString(),
			linkle.getVariant().id()), false);
		if (linkle.isKnockedOut()) {
			ctx.getSource().sendSuccess(() -> Component.translatable("message.linkle_companion.info_knocked_out", linkle.getKnockoutTicks() / 20), false);
		}
		return 1;
	}

	private static int dismiss(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		LinkleEntity linkle = requireLinkle(ctx);
		if (linkle == null) {
			return 0;
		}
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		linkle.leaveWorld((net.minecraft.server.level.ServerLevel) linkle.level());
		player.removeAttached(ModAttachments.COMPANION);
		ctx.getSource().sendSuccess(() -> Component.translatable("message.linkle_companion.dismissed"), false);
		return 1;
	}
}
