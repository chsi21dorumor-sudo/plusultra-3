package com.plusultra.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.plusultra.agency.AgencyData;
import com.plusultra.core.ModDimensions;
import com.plusultra.mission.Ranks;
import com.plusultra.quirk.AbilityManager;
import com.plusultra.quirk.PlayerData;
import com.plusultra.quirk.Quirk;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class ModCommands {
    private ModCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        // ---- /plusultra ----
        d.register(Commands.literal("plusultra")
                .then(Commands.literal("info").executes(c -> info(c)))
                .then(Commands.literal("quirk").requires(s -> s.hasPermission(2))
                        .then(Commands.literal("set")
                                .then(Commands.argument("target", EntityArgument.player())
                                        .then(Commands.argument("quirk", StringArgumentType.word())
                                                .suggests((c, b) -> SharedSuggestionProvider.suggest(Quirk.ids(), b))
                                                .executes(c -> setQuirk(c)))))
                        .then(Commands.literal("clear")
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(c -> clearQuirk(c)))))
                .then(Commands.literal("level").requires(s -> s.hasPermission(2))
                        .then(Commands.literal("set")
                                .then(Commands.argument("target", EntityArgument.player())
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1, 50))
                                                .executes(c -> setLevel(c)))))));

        // ---- /agency ----
        d.register(Commands.literal("agency")
                .then(Commands.literal("create").then(Commands.argument("name", StringArgumentType.word())
                        .executes(c -> agencyCreate(c))))
                .then(Commands.literal("join").then(Commands.argument("name", StringArgumentType.word())
                        .executes(c -> agencyJoin(c))))
                .then(Commands.literal("leave").executes(c -> agencyLeave(c)))
                .then(Commands.literal("info").executes(c -> agencyInfo(c)))
                .then(Commands.literal("list").executes(c -> agencyList(c))));

        // ---- /training ----
        d.register(Commands.literal("training")
                .executes(c -> training(c, true))
                .then(Commands.literal("leave").executes(c -> training(c, false))));
    }

    private static void ok(CommandContext<CommandSourceStack> c, String msg) {
        c.getSource().sendSuccess(() -> Component.literal(msg), false);
    }

    private static int fail(CommandContext<CommandSourceStack> c, String msg) {
        c.getSource().sendFailure(Component.literal(msg));
        return 0;
    }

    // ------------------------------------------------------------ /plusultra

    private static int info(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        PlayerData d = PlayerData.get(p);
        Ranks.Rank rank = Ranks.of(d.points);
        ok(c, "Quirk: " + (d.hasQuirk() ? d.quirk + " Lv." + d.level + " (" + d.xp + "/" + d.xpNeeded() + " xp)" : "none"));
        ok(c, "Rank: " + rank.name() + " (" + d.points + " pts)");
        return 1;
    }

    private static int setQuirk(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer t = EntityArgument.getPlayer(c, "target");
        String id = StringArgumentType.getString(c, "quirk");
        if (Quirk.byId(id) == null) return fail(c, "Unknown quirk: " + id);
        PlayerData.get(t).setQuirk(id);
        AbilityManager.send(t);
        ok(c, "Set " + t.getName().getString() + "'s quirk to " + id);
        return 1;
    }

    private static int clearQuirk(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer t = EntityArgument.getPlayer(c, "target");
        PlayerData.get(t).clearQuirk();
        AbilityManager.send(t);
        ok(c, "Cleared " + t.getName().getString() + "'s quirk");
        return 1;
    }

    private static int setLevel(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer t = EntityArgument.getPlayer(c, "target");
        PlayerData d = PlayerData.get(t);
        if (!d.hasQuirk()) return fail(c, t.getName().getString() + " has no quirk.");
        d.level = IntegerArgumentType.getInteger(c, "level");
        d.xp = 0;
        d.stamina = d.maxStamina();
        AbilityManager.send(t);
        ok(c, "Set level to " + d.level);
        return 1;
    }

    // ------------------------------------------------------------ /agency

    private static int agencyCreate(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        String err = AgencyData.get(c.getSource().getServer()).create(StringArgumentType.getString(c, "name"), p.getUUID());
        if (err != null) return fail(c, err);
        ok(c, "Agency founded!");
        return 1;
    }

    private static int agencyJoin(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        String err = AgencyData.get(c.getSource().getServer()).join(StringArgumentType.getString(c, "name"), p.getUUID());
        if (err != null) return fail(c, err);
        ok(c, "You joined the agency!");
        return 1;
    }

    private static int agencyLeave(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        String err = AgencyData.get(c.getSource().getServer()).leave(p.getUUID());
        if (err != null) return fail(c, err);
        ok(c, "You left your agency.");
        return 1;
    }

    private static int agencyInfo(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        AgencyData.Agency a = AgencyData.get(c.getSource().getServer()).agencyOf(p.getUUID());
        if (a == null) return fail(c, "You are not in an agency.");
        StringBuilder names = new StringBuilder();
        for (java.util.UUID id : a.members) {
            ServerPlayer m = c.getSource().getServer().getPlayerList().getPlayer(id);
            if (names.length() > 0) names.append(", ");
            names.append(m != null ? m.getName().getString() : id.toString().substring(0, 8));
        }
        ok(c, a.name + " (" + a.members.size() + "/8): " + names);
        ok(c, "Stay within 16 blocks of members for +20% stamina regen each (max +60%).");
        return 1;
    }

    private static int agencyList(CommandContext<CommandSourceStack> c) {
        var all = AgencyData.get(c.getSource().getServer()).all();
        if (all.isEmpty()) return fail(c, "No agencies yet. Use /agency create <name>.");
        for (AgencyData.Agency a : all) ok(c, a.name + " - " + a.members.size() + " member(s)");
        return all.size();
    }

    // ------------------------------------------------------------ /training

    private static int training(CommandContext<CommandSourceStack> c, boolean enter) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        var server = c.getSource().getServer();
        if (enter) {
            ServerLevel tl = server.getLevel(ModDimensions.TRAINING);
            if (tl == null) return fail(c, "The Training Ground dimension is not loaded.");
            p.teleportTo(tl, 0.5, -59, 0.5, p.getYRot(), 0f);
            ok(c, "Welcome to the Training Ground. Abilities cost no stamina here. /training leave to exit.");
        } else {
            ServerLevel dest = server.getLevel(p.getRespawnDimension());
            if (dest == null) dest = server.overworld();
            BlockPos pos = p.getRespawnPosition() != null ? p.getRespawnPosition() : dest.getSharedSpawnPos();
            p.teleportTo(dest, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, p.getYRot(), 0f);
            ok(c, "Back to the real world.");
        }
        return 1;
    }
}
