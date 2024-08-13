package com.custommod.tacfactionmod;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class PermissionManager {

    public enum PermissionLevel {
        OWNER,
        OPERATOR,
        USER
    }

    public static boolean hasPermission(CommandSourceStack source, PermissionLevel level, String claimName) {
        try {
            ServerPlayer player = source.getPlayerOrException();

            switch (level) {
                case OWNER:
                    TacFactionClaim.ClaimData claim = TacFactionClaim.activeClaims.get(claimName);
                    return claim != null && claim.owner.equals(player.getUUID());
                case OPERATOR:
                    return source.hasPermission(2);
                case USER:
                    return true;
                default:
                    return false;
            }
        } catch (CommandSyntaxException e) {
            return false;  // Handle the exception appropriately
        }
    }
}
