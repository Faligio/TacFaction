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

public class RemovePlayerCommand {

    private static final SuggestionProvider<CommandSourceStack> CLAIM_SUGGESTIONS = (context, builder) -> {
        return net.minecraft.commands.SharedSuggestionProvider.suggest(TacFactionClaim.activeClaims.keySet(), builder);
    };

    private static final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTIONS = (context, builder) -> {
        String claimName = StringArgumentType.getString(context, "claimName");
        TacFactionClaim.ClaimData claim = TacFactionClaim.activeClaims.get(claimName);

        if (claim != null) {
            return net.minecraft.commands.SharedSuggestionProvider.suggest(
                    claim.allowedPlayers.stream()
                            .map(uuid -> context.getSource().getServer().getPlayerList().getPlayer(uuid))
                            .filter(player -> player != null)
                            .map(ServerPlayer::getName)
                            .map(Object::toString)
                            .collect(Collectors.toList()), builder);
        }
        return net.minecraft.commands.SharedSuggestionProvider.suggest(new String[0], builder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tac")
                .then(Commands.literal("removeplayer")
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
                                                if (targetPlayer != null && claim.allowedPlayers.contains(targetPlayer.getUUID())) {
                                                    claim.allowedPlayers.remove(targetPlayer.getUUID());
                                                    source.sendSuccess(() -> Component.literal("Player '" + playerName + "' removed from claim '" + claimName + "'"), false);
                                                    return 1;
                                                } else {
                                                    source.sendFailure(Component.literal("Player not found or not in the claim!"));
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
