package com.custommod.tacfactionmod.commands.wrapper;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import com.custommod.tacfactionmod.TacFactionClaim;

import java.util.Map;
import java.util.stream.Collectors;

public class TransferClaimOwnershipCommand {

    private static final SuggestionProvider<CommandSourceStack> OWN_CLAIM_SUGGESTIONS = (context, builder) -> {
        ServerPlayer player = context.getSource().getPlayerOrException();
        return net.minecraft.commands.SharedSuggestionProvider.suggest(
                TacFactionClaim.activeClaims.entrySet().stream()
                        .filter(entry -> entry.getValue().owner.equals(player.getUUID()))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList()), builder);
    };

    private static final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTIONS = (context, builder) -> {
        return net.minecraft.commands.SharedSuggestionProvider.suggest(
                context.getSource().getServer().getPlayerList().getPlayers().stream()
                        .map(player -> player.getName().getString())
                        .collect(Collectors.toList()), builder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tac")
                .then(Commands.literal("transferownership")
                        .then(Commands.argument("claimName", StringArgumentType.string())
                                .suggests(OWN_CLAIM_SUGGESTIONS)
                                .then(Commands.argument("newOwnerName", StringArgumentType.string())
                                        .suggests(PLAYER_SUGGESTIONS)
                                        .executes(context -> {
                                            String claimName = StringArgumentType.getString(context, "claimName");
                                            String newOwnerName = StringArgumentType.getString(context, "newOwnerName");
                                            CommandSourceStack source = context.getSource();
                                            ServerPlayer player = source.getPlayerOrException();

                                            TacFactionClaim.ClaimData claim = TacFactionClaim.activeClaims.get(claimName);
                                            if (claim != null && claim.owner.equals(player.getUUID())) {  // Verify ownership
                                                ServerPlayer newOwner = source.getServer().getPlayerList().getPlayerByName(newOwnerName);
                                                if (newOwner != null) {
                                                    claim.owner = newOwner.getUUID();
                                                    source.sendSuccess(() -> Component.literal("Ownership of claim '" + claimName + "' has been transferred to '" + newOwnerName + "'"), false);
                                                    return 1;
                                                } else {
                                                    source.sendFailure(Component.literal("Player '" + newOwnerName + "' not found! Make sure they are online."));
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
