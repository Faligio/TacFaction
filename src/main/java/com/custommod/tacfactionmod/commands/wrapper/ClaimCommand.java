package com.custommod.tacfactionmod.commands.wrapper;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import com.custommod.tacfactionmod.TacFactionClaim;

public class ClaimCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tacfaction")
                .then(Commands.literal("claim")
                        .then(Commands.argument("claimName", StringArgumentType.string())
                                .executes(context -> {
                                    String claimName = StringArgumentType.getString(context, "claimName");
                                    CommandSourceStack source = context.getSource();
                                    ServerPlayer player = source.getPlayerOrException();

                                    TacFactionClaim.LOGGER.info("Initializing claim with name: " + claimName);
                                    TacFactionClaim.activeClaims.put(claimName, new TacFactionClaim.ClaimData(player.getUUID()));
                                    source.sendSuccess(() -> Component.literal("Claim '" + claimName + "' initialized! Now set the first corner using /tacfaction claimpos1"), false);
                                    return 1;
                                })
                        )
                )
        );
    }
}
