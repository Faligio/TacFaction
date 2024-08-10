package com.custommod.tacfactionmod.commands.wrapper;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import com.custommod.tacfactionmod.TacFactionClaim;

import java.util.Set;
import java.util.stream.Collectors;

public class ListClaimsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tac")
                .then(Commands.literal("listclaims")
                        .executes(context -> {
                            Set<String> claimNames = TacFactionClaim.activeClaims.keySet();

                            if (!claimNames.isEmpty()) {
                                String claimsList = claimNames.stream().collect(Collectors.joining(", "));
                                context.getSource().sendSuccess(() -> Component.literal("Existing Claims: " + claimsList), false);
                            } else {
                                context.getSource().sendFailure(Component.literal("No claims found on the server."));
                            }

                            return 1;
                        })
                )
        );
    }
}
