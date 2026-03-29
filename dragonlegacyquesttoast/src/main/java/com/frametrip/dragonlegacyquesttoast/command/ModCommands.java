package com.frametrip.dragonlegacyquesttoast.command;

import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.QuestToastPacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("dlquesttoast")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .then(Commands.argument("title", StringArgumentType.greedyString())
                                                .executes(ctx -> {
                                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                                    String type = StringArgumentType.getString(ctx, "type");
                                                    String title = StringArgumentType.getString(ctx, "title");

                                                    if (!"accepted".equals(type) && !"completed".equals(type)) {
                                                        ctx.getSource().sendFailure(net.minecraft.network.chat.Component.literal("Type must be accepted or completed"));
                                                        return 0;
                                                    }

                                                    ModNetwork.CHANNEL.send(
                                                            PacketDistributor.PLAYER.with(() -> player),
                                                            new QuestToastPacket(type, title)
                                                    );

                                                    ctx.getSource().sendSuccess(() ->
                                                            net.minecraft.network.chat.Component.literal("Quest toast sent to " + player.getGameProfile().getName()),
                                                            true
                                                    );
                                                    return 1;
                                                }))))
        );
    }
}