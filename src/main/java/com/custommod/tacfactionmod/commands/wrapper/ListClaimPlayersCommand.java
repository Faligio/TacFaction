package com.custommod.tacfactionmod.commands.wrapper;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import com.custommod.tacfactionmod.TacFactionClaim;

public class ListClaimPlayersCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tac")
                .then(Commands.literal("listclaimplayers")
                        .then(Commands.argument("claimName", StringArgumentType.string())
                                .executes(context -> {
                                    String claimName = StringArgumentType.getString(context, "claimName");
                                    CommandSourceStack source = context.getSource();
                                    ServerPlayer player = source.getPlayerOrException();

                                    TacFactionClaim.ClaimData claim = TacFactionClaim.activeClaims.get(claimName);
                                    if (claim != null) {
                                        source.sendSuccess(() -> Component.literal("Players added to claim '" + claimName + "':"), false);

                                        boolean foundAny = false;
                                        for (java.util.UUID playerUUID : claim.allowedPlayers) {
                                            ServerPlayer allowedPlayer = source.getServer().getPlayerList().getPlayer(playerUUID);
                                            if (allowedPlayer != null) {
                                                foundAny = true;
                                                source.sendSuccess(() -> Component.literal("- " + allowedPlayer.getName().getString()), false);
                                            }
                                        }

                                        if (!foundAny) {
                                            source.sendFailure(Component.literal("No players added to this claim."));
                                        }
                                    } else {
                                        source.sendFailure(Component.literal("Claim '" + claimName + "' not found."));
                                    }

                                    return 1;
                                })
                        )
                )
        );
    }
}
