package com.custommod.tacfactionmod.commands.wrapper;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import com.custommod.tacfactionmod.TacFactionClaim;
import java.util.Map;  // Import du type Map

public class ListNearbyClaimsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tac")
                .then(Commands.literal("listnearbyclaims")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            ServerPlayer player = source.getPlayerOrException();
                            Vec3 pos = player.position();
                            int radius = 100;

                            source.sendSuccess(() -> Component.literal("Nearby Claims:"), false);

                            boolean foundAny = false;
                            for (Map.Entry<String, TacFactionClaim.ClaimData> entry : TacFactionClaim.activeClaims.entrySet()) {
                                TacFactionClaim.ClaimData claim = entry.getValue();
                                if (TacFactionClaim.isInsideRadius(claim, pos, radius)) {
                                    foundAny = true;
                                    source.sendSuccess(() -> Component.literal("- " + entry.getKey()), false);
                                }
                            }

                            if (!foundAny) {
                                source.sendFailure(Component.literal("No claims found within a " + radius + " block radius."));
                            }

                            return 1;
                        })
                )
        );
    }
}
