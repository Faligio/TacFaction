package com.custommod.tacfactionmod.commands.wrapper;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import com.custommod.tacfactionmod.TacFactionClaim;

import static com.mojang.text2speech.Narrator.LOGGER;

public class AddPlayerCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tacfaction")
                .then(Commands.literal("addplayer")
                        .then(Commands.argument("claimName", StringArgumentType.string())
                                .then(Commands.argument("playerName", StringArgumentType.string())
                                        .executes(context -> {
                                            LOGGER.info("Executing addplayer command...");
                                            String claimName = StringArgumentType.getString(context, "claimName");
                                            String playerName = StringArgumentType.getString(context, "playerName");
                                            CommandSourceStack source = context.getSource();
                                            ServerPlayer player = source.getPlayerOrException();

                                            TacFactionClaim.ClaimData claim = TacFactionClaim.activeClaims.get(claimName);
                                            if (claim != null && claim.owner.equals(player.getUUID())) {
                                                ServerPlayer targetPlayer = source.getServer().getPlayerList().getPlayerByName(playerName);
                                                if (targetPlayer != null) {
                                                    claim.allowedPlayers.add(targetPlayer.getUUID());
                                                    source.sendSuccess(() -> Component.literal("Player '" + playerName + "' added to claim '" + claimName + "'"), false);
                                                    LOGGER.info("Player added successfully.");
                                                    return 1;
                                                } else {
                                                    source.sendFailure(Component.literal("Player not found!"));
                                                    LOGGER.warn("Player not found.");
                                                }
                                            } else {
                                                source.sendFailure(Component.literal("Claim not found or you are not the owner!"));
                                                LOGGER.warn("Claim not found or not owner.");
                                            }
                                            return 0;
                                        })
                                )
                        )
                )
        );
    }
}

