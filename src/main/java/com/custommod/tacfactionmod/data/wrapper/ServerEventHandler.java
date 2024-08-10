package com.custommod.tacfactionmod.data.wrapper;

import com.custommod.tacfactionmod.TacFactionClaim;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.File;

@Mod.EventBusSubscriber
public class ServerEventHandler {

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        File file = new File(event.getServer().getServerDirectory(), "claims.json");
        ClaimStorage.loadClaimsFromFile(file);
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        File file = new File(event.getServer().getServerDirectory(), "claims.json");
        ClaimStorage.saveClaimsToFile(TacFactionClaim.activeClaims, file);
    }
}
