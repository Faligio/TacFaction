package com.custommod.tacfactionmod.events.wrapper;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.custommod.tacfactionmod.TacFactionClaim;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = TacFactionClaim.MODID)
public class ClaimEnterListener {

    private static final Map<UUID, String> playerClaimStates = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) event.player;
            Vec3 pos = player.position();
            UUID playerId = player.getUUID();
            boolean isInsideClaim = false;

            for (Map.Entry<String, TacFactionClaim.ClaimData> entry : TacFactionClaim.activeClaims.entrySet()) {
                TacFactionClaim.ClaimData claim = entry.getValue();

                if (TacFactionClaim.isInsideClaim(claim, pos)) {
                    isInsideClaim = true;
                    String claimName = entry.getKey();

                    if (!claimName.equals(playerClaimStates.get(playerId))) {
                        player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("You entered: " + claimName)));
                        player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("Claimed Area")));
                        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));

                        player.getCommandSenderWorld().playSound(null, player.blockPosition(), TacFactionClaim.ED_NEW_LOCATION.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                        playerClaimStates.put(playerId, claimName);
                    }
                    break;
                }
            }

            if (!isInsideClaim && playerClaimStates.containsKey(playerId)) {
                // Le joueur a quitté le claim
                playerClaimStates.remove(playerId);
            }
        }
    }
}
