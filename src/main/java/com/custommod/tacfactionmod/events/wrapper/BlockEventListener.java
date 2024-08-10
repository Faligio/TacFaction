package com.custommod.tacfactionmod.events.wrapper;

import com.custommod.tacfactionmod.TacFactionClaim;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TacFactionClaim.MODID)
public class BlockEventListener {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        try {
            if (event.getPlayer() instanceof ServerPlayer player) {
                Level world = player.getCommandSenderWorld();
                BlockPos pos = event.getPos();

                handleBlockInteraction(player, pos, world, event);
            }
        } catch (Exception e) {
            System.err.println("Error handling block break event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        try {
            if (event.getEntity() instanceof ServerPlayer player) {
                Level world = player.getCommandSenderWorld();
                BlockPos pos = event.getPos();

                handleBlockInteraction(player, pos, world, event);
            }
        } catch (Exception e) {
            System.err.println("Error handling block place event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        try {
            if (event.getEntity() instanceof ServerPlayer player) {
                Level world = player.getCommandSenderWorld();
                BlockPos pos = event.getPos();

                handleBlockInteraction(player, pos, world, event);
            }
        } catch (Exception e) {
            System.err.println("Error handling block interact event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleBlockInteraction(ServerPlayer player, BlockPos pos, Level world, net.minecraftforge.eventbus.api.Event event) {
        try {
            TacFactionClaim.ClaimData claim = getClaimAtPosition(pos);

            if (claim != null && !claim.allowedPlayers.contains(player.getUUID()) && !claim.owner.equals(player.getUUID())) {
                player.sendSystemMessage(Component.literal("Vous n'avez pas la permission d'interagir avec les blocs ici."));
                if (event instanceof BlockEvent.BreakEvent) {
                    ((BlockEvent.BreakEvent) event).setCanceled(true);
                } else if (event instanceof BlockEvent.EntityPlaceEvent) {
                    ((BlockEvent.EntityPlaceEvent) event).setCanceled(true);
                } else if (event instanceof PlayerInteractEvent.RightClickBlock) {
                    ((PlayerInteractEvent.RightClickBlock) event).setCanceled(true);
                }
            }
        } catch (Exception e) {
            System.err.println("Error during block interaction handling: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static TacFactionClaim.ClaimData getClaimAtPosition(BlockPos pos) {
        for (TacFactionClaim.ClaimData claim : TacFactionClaim.activeClaims.values()) {
            if (TacFactionClaim.isInsideClaim(claim, new Vec3(pos.getX(), pos.getY(), pos.getZ()))) {
                return claim;
            }
        }
        return null;
    }
}
