package com.custommod.tacfactionmod.commands.wrapper;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import com.custommod.tacfactionmod.TacFactionClaim;

import java.util.function.Supplier;

public class RemoveClaimCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tac")
                .then(Commands.literal("removeclaim")
                        .then(Commands.argument("claimName", StringArgumentType.string())
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    String claimName = StringArgumentType.getString(context, "claimName");

                                    if (TacFactionClaim.activeClaims.containsKey(claimName)) {
                                        TacFactionClaim.activeClaims.remove(claimName);
                                        source.sendSuccess((Supplier<Component>) () -> Component.literal("Claim '" + claimName + "' has been removed."), false);
                                    } else {
                                        source.sendFailure(Component.literal("No claim found with the name '" + claimName + "'."));
                                    }

                                    return 1;
                                })
                        )
                )
        );
    }
}
