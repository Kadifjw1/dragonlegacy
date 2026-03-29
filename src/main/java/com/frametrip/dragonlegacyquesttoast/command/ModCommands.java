package com.frametrip.dragonlegacyquesttoast.command;

import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.QuestToastConfigPacket;
import com.frametrip.dragonlegacyquesttoast.network.QuestToastPacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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
                                        .executes(ctx -> {
                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                            String type = StringArgumentType.getString(ctx, "type");

                                            if (!"accepted".equals(type) && !"completed".equals(type) && !"updated".equals(type)) {
                                                ctx.getSource().sendFailure(net.minecraft.network.chat.Component.literal("Type must be accepted, completed or updated"));
                                                return 0;
                                            }

                                            ModNetwork.CHANNEL.send(
                                                    PacketDistributor.PLAYER.with(() -> player),
                                                    new QuestToastPacket(type, "")
                                            );

                                            return 1;
                                        })))
        );

        dispatcher.register(
                Commands.literal("dlquesttoastconfig")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.literal("reset")
                                        .executes(ctx -> {
                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");

                                            ModNetwork.CHANNEL.send(
                                                    PacketDistributor.PLAYER.with(() -> player),
                                                    new QuestToastConfigPacket(true, 0, 0, 0, 0, 0, 0, 0, 0)
                                            );

                                            return 1;
                                        }))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                                        .then(Commands.argument("width", IntegerArgumentType.integer(1))
                                                                .then(Commands.argument("height", IntegerArgumentType.integer(1))
                                                                        .then(Commands.argument("fadeIn", IntegerArgumentType.integer(1))
                                                                                .then(Commands.argument("stay", IntegerArgumentType.integer(1))
                                                                                        .then(Commands.argument("fadeOut", IntegerArgumentType.integer(1))
                                                                                                .then(Commands.argument("startOffsetX", IntegerArgumentType.integer())
                                                                                                        .executes(ctx -> {
                                                                                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");

                                                                                                            int x = IntegerArgumentType.getInteger(ctx, "x");
                                                                                                            int y = IntegerArgumentType.getInteger(ctx, "y");
                                                                                                            int width = IntegerArgumentType.getInteger(ctx, "width");
                                                                                                            int height = IntegerArgumentType.getInteger(ctx, "height");
                                                                                                            int fadeIn = IntegerArgumentType.getInteger(ctx, "fadeIn");
                                                                                                            int stay = IntegerArgumentType.getInteger(ctx, "stay");
                                                                                                            int fadeOut = IntegerArgumentType.getInteger(ctx, "fadeOut");
                                                                                                            int startOffsetX = IntegerArgumentType.getInteger(ctx, "startOffsetX");

                                                                                                            ModNetwork.CHANNEL.send(
                                                                                                                    PacketDistributor.PLAYER.with(() -> player),
                                                                                                                    new QuestToastConfigPacket(
                                                                                                                            false,
                                                                                                                            x,
                                                                                                                            y,
                                                                                                                            width,
                                                                                                                            height,
                                                                                                                            fadeIn,
                                                                                                                            stay,
                                                                                                                            fadeOut,
                                                                                                                            startOffsetX
                                                                                                                    )
                                                                                                            );

                                                                                                            return 1;
                                                                                                        })))))))))))
        );
    }
}
