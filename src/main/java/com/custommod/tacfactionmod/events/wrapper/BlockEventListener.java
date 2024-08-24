package com.custommod.tacfactionmod.events.wrapper;

import com.custommod.tacfactionmod.TacFactionClaim;
/*import com.jaquadro.minecraft.storagedrawers.block.BlockDrawers;*/
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

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
                ItemStack itemInHand = player.getMainHandItem();

                if (itemInHand.getItem() == Items.WATER_BUCKET || itemInHand.getItem() == Items.LAVA_BUCKET) {
                    handleBlockInteraction(player, pos, world, event);
                }
            }
        } catch (Exception e) {
            System.err.println("Error handling block place event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide) {
            if (event.getEntity() instanceof ServerPlayer player) {
                Level world = player.getCommandSenderWorld();
                BlockPos pos = event.getPos();
                ItemStack itemInHand = player.getMainHandItem();

                BlockState state = world.getBlockState(pos);
                ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());

                if (blockId != null && blockId.getNamespace().equals("waystones")) {
                    return;
                }

                if (itemInHand.getItem() == Items.BUCKET) {
                    if (state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.LAVA) {
                        TacFactionClaim.ClaimData claim = getClaimAtPosition(pos);

                        if (claim != null && !claim.allowedPlayers.contains(player.getUUID()) && !claim.owner.equals(player.getUUID())) {
                            player.sendSystemMessage(Component.literal("Vous n'avez pas la permission de récupérer ce liquide ici."));
                            event.setCanceled(true);
                        }
                    }
                }

                if (itemInHand.getItem() == Items.WATER_BUCKET || itemInHand.getItem() == Items.LAVA_BUCKET) {
                    handleBlockInteraction(player, pos, world, event);
                }

                handleBlockInteraction(player, pos, world, event);
            }
        }
    }

    @SubscribeEvent
    public static void onItemFrameInteract(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!event.getLevel().isClientSide) {
            if (event.getEntity() instanceof ServerPlayer player) {
                if (event.getTarget() instanceof ItemFrame) {
                    BlockPos pos = event.getTarget().blockPosition();
                    TacFactionClaim.ClaimData claim = getClaimAtPosition(pos);

                    if (claim != null && !claim.allowedPlayers.contains(player.getUUID()) && !claim.owner.equals(player.getUUID())) {
                        player.sendSystemMessage(Component.literal("Vous n'avez pas la permission d'interagir avec cet item frame ici."));
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

   /* public static void onStorageDrawerInteract(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide) {
            if (event.getEntity() instanceof ServerPlayer player) {
                Level world = player.getCommandSenderWorld();
                BlockPos pos = event.getPos();
                BlockState state = world.getBlockState(pos);

                // Check if the block is a Storage Drawer
                if (state.getBlock() instanceof BlockDrawers) {
                    TacFactionClaim.ClaimData claim = getClaimAtPosition(pos);

                    // If the Storage Drawer is in a claimed area and the player is not allowed, cancel the event
                    if (claim != null && !claim.allowedPlayers.contains(player.getUUID()) && !claim.owner.equals(player.getUUID())) {
                        player.sendSystemMessage(Component.literal("Vous n'avez pas la permission d'interagir avec ce Storage Drawer ici."));
                        event.setCanceled(true);  // Cancel the event to prevent interaction with the Storage Drawer
                    }
                }
            }
        }
    }*/



    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        try {
            if (event.getSource().getEntity() instanceof ServerPlayer player) {
                Level world = player.getCommandSenderWorld();
                BlockPos pos = event.getEntity().blockPosition();

                if (event.getEntity() instanceof Animal) {
                    TacFactionClaim.ClaimData claim = getClaimAtPosition(pos);

                    if (claim != null && !claim.allowedPlayers.contains(player.getUUID()) && !claim.owner.equals(player.getUUID()) && !player.hasPermissions(4)) {
                        player.sendSystemMessage(Component.literal("Vous n'avez pas la permission de tuer des animaux ici."));
                        event.setCanceled(true); // Properly cancel the event
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error handling animal death event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        List<BlockPos> affectedBlocks = event.getAffectedBlocks();
        affectedBlocks.removeIf(blockPos -> {
            TacFactionClaim.ClaimData claim = getClaimAtPosition(blockPos);
            return claim != null;
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttackEntity(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            BlockPos pos = event.getTarget().blockPosition();
            TacFactionClaim.ClaimData claim = getClaimAtPosition(pos);

            if (claim != null && event.getTarget() instanceof Animal) {
                if (!claim.allowedPlayers.contains(player.getUUID()) && !claim.owner.equals(player.getUUID())) {
                    player.sendSystemMessage(Component.literal("Vous n'avez pas la permission de frapper des animaux ici."));
                    event.setCanceled(true);
                }
            }
        }
    }

    private static void handleBlockInteraction(ServerPlayer player, BlockPos pos, Level world, net.minecraftforge.eventbus.api.Event event) {
        try {
            TacFactionClaim.ClaimData claim = getClaimAtPosition(pos);

            if (player.hasPermissions(4)) {
                return;
            }

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

    @SubscribeEvent
    public static void onPlayerUseItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ItemStack itemInHand = event.getItemStack();

            if (itemInHand.getItem() == Items.WATER_BUCKET || itemInHand.getItem() == Items.LAVA_BUCKET) {
                BlockPos pos = player.blockPosition();
                TacFactionClaim.ClaimData claim = getClaimAtPosition(pos);

                if (claim != null && !claim.allowedPlayers.contains(player.getUUID()) && !claim.owner.equals(player.getUUID())) {
                    player.sendSystemMessage(Component.literal("Vous n'avez pas la permission d'utiliser ce seau ici."));
                    event.setCanceled(true);
                }
            }
        }
    }
}
