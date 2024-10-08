package com.custommod.tacfactionmod.commands.wrapper;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import com.custommod.tacfactionmod.TacFactionClaim;

import java.util.stream.Collectors;

public class AddPlayerCommand {

    private static final SuggestionProvider<CommandSourceStack> CLAIM_SUGGESTIONS = (context, builder) -> {
        return net.minecraft.commands.SharedSuggestionProvider.suggest(TacFactionClaim.activeClaims.keySet(), builder);
    };

    private static final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTIONS = (context, builder) -> {
        return net.minecraft.commands.SharedSuggestionProvider.suggest(
                context.getSource().getServer().getPlayerList().getPlayers().stream()
                        .map(player -> player.getName().getString())
                        .collect(Collectors.toList()), builder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tac")
                .then(Commands.literal("addplayer")
                        .then(Commands.argument("claimName", StringArgumentType.string())
                                .suggests(CLAIM_SUGGESTIONS)
                                .then(Commands.argument("playerName", StringArgumentType.string())
                                        .suggests(PLAYER_SUGGESTIONS)
                                        .executes(context -> {
                                            String claimName = StringArgumentType.getString(context, "claimName");
                                            String playerName = StringArgumentType.getString(context, "playerName");
                                            CommandSourceStack source = context.getSource();
                                            ServerPlayer player = source.getPlayerOrException();

                                            TacFactionClaim.ClaimData claim = TacFactionClaim.activeClaims.get(claimName);
                                            if (claim != null && claim.owner.equals(player.getUUID())) {
                                                ServerPlayer targetPlayer = source.getServer().getPlayerList().getPlayerByName(playerName);
                                                if (targetPlayer != null) {
                                                    if (claim.allowedPlayers.add(targetPlayer.getUUID())) {
                                                        source.sendSuccess(() -> Component.literal("Player '" + playerName + "' added to claim '" + claimName + "'"), false);
                                                        return 1;
                                                    } else {
                                                        source.sendFailure(Component.literal("Player '" + playerName + "' is already allowed in this claim!"));
                                                    }
                                                } else {
                                                    source.sendFailure(Component.literal("Player '" + playerName + "' not found! Make sure they are online."));
                                                }
                                            } else {
                                                source.sendFailure(Component.literal("Claim not found or you are not the owner!"));
                                            }
                                            return 0;
                                        })
                                )
                        )
                )
        );
    }
}
