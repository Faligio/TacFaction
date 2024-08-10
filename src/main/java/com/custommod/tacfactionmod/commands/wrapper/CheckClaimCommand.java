package com.custommod.tacfactionmod.commands.wrapper;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import com.custommod.tacfactionmod.TacFactionClaim;
import java.util.Map;  // Import du type Map

public class CheckClaimCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tacfaction")
                .then(Commands.literal("checkclaim")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            ServerPlayer player = source.getPlayerOrException();
                            Vec3 pos = player.position();

                            for (Map.Entry<String, TacFactionClaim.ClaimData> entry : TacFactionClaim.activeClaims.entrySet()) {
                                TacFactionClaim.ClaimData claim = entry.getValue();
                                if (claim.pos1 != null && claim.pos2 != null) {
                                    double minX = Math.min(claim.pos1.x, claim.pos2.x);
                                    double minY = Math.min(claim.pos1.y, claim.pos2.y);
                                    double minZ = Math.min(claim.pos1.z, claim.pos2.z);
                                    double maxX = Math.max(claim.pos1.x, claim.pos2.x);
                                    double maxY = Math.max(claim.pos1.y, claim.pos2.y);
                                    double maxZ = Math.max(claim.pos1.z, claim.pos2.z);

                                    if (pos.x >= minX && pos.x <= maxX && pos.y >= minY && pos.y <= maxY && pos.z >= minZ && pos.z <= maxZ) {
                                        if (claim.allowedPlayers.contains(player.getUUID()) || claim.owner.equals(player.getUUID())) {
                                            source.sendSuccess(() -> Component.literal("You are within the claim: " + entry.getKey()), false);
                                        } else {
                                            source.sendFailure(Component.literal("You are within the claim: " + entry.getKey() + " but you don't have permission to build."));
                                        }
                                        return 1;
                                    }
                                }
                            }
                            source.sendFailure(Component.literal("You are not within any claimed area."));
                            return 0;
                        })
                )
        );
    }
}
