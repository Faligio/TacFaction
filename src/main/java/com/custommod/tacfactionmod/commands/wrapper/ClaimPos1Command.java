package com.custommod.tacfactionmod.commands.wrapper;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import com.custommod.tacfactionmod.TacFactionClaim;

public class ClaimPos1Command {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tacfaction")
                .then(Commands.literal("claimpos1")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            ServerPlayer player = source.getPlayerOrException();
                            Vec3 pos = player.position();

                            TacFactionClaim.LOGGER.info("Setting position 1 for player: " + player.getName().getString() + " at " + pos.toString());

                            for (TacFactionClaim.ClaimData claim : TacFactionClaim.activeClaims.values()) {
                                if (claim.owner.equals(player.getUUID()) && claim.pos1 == null) {
                                    claim.pos1 = new Vec3(pos.x, -64, pos.z);
                                    source.sendSuccess(() -> Component.literal("First position set at " + pos.toString()), false);
                                    source.sendSuccess(() -> Component.literal("Now set the second corner using /tacfaction claimpos2"), false);
                                    return 1;
                                }
                            }
                            source.sendFailure(Component.literal("No active claim found or you are not the owner! Please initialize a claim first using /tacfaction claim <name>."));
                            return 0;
                        })
                )
        );
    }
}
