package com.custommod.tacfactionmod.commands.wrapper;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import com.custommod.tacfactionmod.TacFactionClaim;

public class ClaimPos2Command {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tac")
                .then(Commands.literal("claimpos2")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            ServerPlayer player = source.getPlayerOrException();
                            Vec3 pos = player.position();

                            TacFactionClaim.LOGGER.info("Setting position 2 for player: " + player.getName().getString() + " at " + pos.toString());

                            for (TacFactionClaim.ClaimData claim : TacFactionClaim.activeClaims.values()) {
                                if (claim.owner.equals(player.getUUID()) && claim.pos1 != null && claim.pos2 == null) {
                                    claim.pos2 = new Vec3(pos.x, 320, pos.z);
                                    source.sendSuccess(() -> Component.literal("Second position set at " + pos.toString()), false);

                                    source.sendSuccess(() -> Component.literal("Claim created with the area defined by the two positions."), false);
                                    return 1;
                                }
                            }
                            source.sendFailure(Component.literal("No active claim found, first position not set, or you are not the owner!"));
                            return 0;
                        })
                )
        );
    }
}
