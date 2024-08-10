package com.custommod.tacfactionmod;

import com.custommod.tacfactionmod.commands.wrapper.*;
import com.custommod.tacfactionmod.events.wrapper.ClaimEnterListener;
import com.mojang.logging.LogUtils;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mod(TacFactionClaim.MODID)
public class TacFactionClaim {
    public static final String MODID = "tacfactionmod";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Map<String, ClaimData> activeClaims = new HashMap<>();

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);

    public static final RegistryObject<SoundEvent> ED_NEW_LOCATION = SOUND_EVENTS.register("ed_new_location",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "ed_new_location")));

    public TacFactionClaim() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        SOUND_EVENTS.register(modEventBus);  // Enregistrement du son
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("TacFaction Mod Setup Complete");
        MinecraftForge.EVENT_BUS.register(new ClaimEnterListener());
    }

    @SubscribeEvent
    public void onServerStarting(RegisterCommandsEvent event) {
        LOGGER.info("onServerStarting called!");
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        CheckClaimCommand.register(dispatcher);
        ClaimCommand.register(dispatcher);
        ClaimPos1Command.register(dispatcher);
        ClaimPos2Command.register(dispatcher);
        AddPlayerCommand.register(dispatcher);
        RemovePlayerCommand.register(dispatcher);
        ListClaimPlayersCommand.register(dispatcher);
        ListNearbyClaimsCommand.register(dispatcher);
        RemoveClaimCommand.register(dispatcher); // Enregistrement de la commande RemoveClaimCommand
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
