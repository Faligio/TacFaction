package com.custommod.tacfactionmod.commands.wrapper;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import com.custommod.tacfactionmod.TacFactionClaim;

public class ModifyClaimTitleCommand {

    private static final SuggestionProvider<CommandSourceStack> CLAIM_SUGGESTIONS = (context, builder) -> {
        return net.minecraft.commands.SharedSuggestionProvider.suggest(TacFactionClaim.activeClaims.keySet().stream(), builder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tac")
                .then(Commands.literal("modifyclaimtitle")
                        .then(Commands.argument("claimName", StringArgumentType.string())
                                .suggests(CLAIM_SUGGESTIONS)
                                .then(Commands.argument("title", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            String claimName = StringArgumentType.getString(context, "claimName");
                                            String title = StringArgumentType.getString(context, "title");
                                            CommandSourceStack source = context.getSource();
                                            ServerPlayer player = source.getPlayerOrException();

                                            TacFactionClaim.ClaimData claimData = TacFactionClaim.activeClaims.get(claimName);

                                            if (claimData != null) {
                                                if (claimData.owner.equals(player.getUUID())) {
                                                    claimData.title = title;
                                                    source.sendSuccess(() -> Component.literal("Title for claim '" + claimName + "' has been updated to: " + title), false);
                                                } else {
                                                    source.sendFailure(Component.literal("You are not the owner of this claim!"));
                                                }
                                            } else {
                                                source.sendFailure(Component.literal("No claim found with the name '" + claimName + "'."));
                                            }

                                            return 1;
                                        })
                                )
                        )
                )
        );
    }
}
