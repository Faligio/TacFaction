package com.custommod.tacfactionmod;

import com.custommod.tacfactionmod.commands.wrapper.ClaimCommand;
import com.custommod.tacfactionmod.commands.wrapper.ClaimPos1Command;
import com.custommod.tacfactionmod.commands.wrapper.ClaimPos2Command;
import com.custommod.tacfactionmod.commands.wrapper.ListClaimPlayersCommand;
import com.custommod.tacfactionmod.commands.wrapper.ListNearbyClaimsCommand;
import com.custommod.tacfactionmod.commands.wrapper.RemovePlayerCommand;
import com.custommod.tacfactionmod.commands.wrapper.AddPlayerCommand;
import com.custommod.tacfactionmod.commands.wrapper.CheckClaimCommand;
import com.mojang.logging.LogUtils;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mod(TacFactionClaim.MODID)
public class TacFactionClaim {
    public static final String MODID = "tacfactionmod";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Map<String, ClaimData> activeClaims = new HashMap<>();

    public TacFactionClaim() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("TacFaction Mod Setup Complete");
    }

    @SubscribeEvent
    public void onServerStarting(RegisterCommandsEvent event) {
        LOGGER.info("onServerStarting called!");
        // Commande test
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("testcommand")
                .executes(context -> {
                    context.getSource().sendSuccess(() -> Component.literal("Test command executed!"), false);
                    return 1;
                }));
    }

    public static class ClaimData {
        public Vec3 pos1;
        public Vec3 pos2;
        public Set<java.util.UUID> allowedPlayers = new HashSet<>();
        public java.util.UUID owner;

        public ClaimData(java.util.UUID owner) {
            this.owner = owner;
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("TacFaction Mod Client Setup Complete");
        }
    }

    public static boolean isInsideRadius(ClaimData claim, Vec3 pos, int radius) {
        if (claim.pos1 != null && claim.pos2 != null) {
            double minX = Math.min(claim.pos1.x, claim.pos2.x);
            double minZ = Math.min(claim.pos1.z, claim.pos2.z);
            double maxX = Math.max(claim.pos1.x, claim.pos2.x);
            double maxZ = Math.max(claim.pos1.z, claim.pos2.z);

            double distX = Math.max(Math.abs(pos.x - minX), Math.abs(pos.x - maxX));
            double distZ = Math.max(Math.abs(pos.z - minZ), Math.abs(pos.z - maxZ));

            return distX <= radius && distZ <= radius;
        }
        return false;
    }

    public static boolean isInsideClaim(ClaimData claim, Vec3 pos) {
        if (claim.pos1 != null && claim.pos2 != null) {
            double minX = Math.min(claim.pos1.x, claim.pos2.x);
            double minY = Math.min(claim.pos1.y, claim.pos2.y);
            double minZ = Math.min(claim.pos1.z, claim.pos2.z);
            double maxX = Math.max(claim.pos1.x, claim.pos2.x);
            double maxY = Math.max(claim.pos1.y, claim.pos2.y);
            double maxZ = Math.max(claim.pos1.z, claim.pos2.z);

            return pos.x >= minX && pos.x <= maxX && pos.y >= minY && pos.y <= maxY && pos.z >= minZ && pos.z <= maxZ;
        }
        return false;
    }
}
