package com.frametrip.dragonlegacyquesttoast.command;

import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.NpcDialogueConfigPacket;
import com.frametrip.dragonlegacyquesttoast.network.NpcDialoguePacket;
import com.frametrip.dragonlegacyquesttoast.network.QuestToastConfigPacket;
import com.frametrip.dragonlegacyquesttoast.network.QuestToastPacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        registerQuestToastCommand(dispatcher);
        registerQuestToastConfigCommand(dispatcher);
        registerNpcSayCommand(dispatcher);
        registerNpcSayConfigCommand(dispatcher);
    }

    private static void registerQuestToastCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("dlquesttoast")
                        .requires(source -> source.hasPermission(2))
                        .then(
                                Commands.argument("player", EntityArgument.player())
                                        .then(
                                                Commands.argument("type", StringArgumentType.word())
                                                        .executes(ctx -> {
                                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                                            String type = StringArgumentType.getString(ctx, "type");

                                                            if (!"accepted".equals(type) && !"completed".equals(type) && !"updated".equals(type)) {
                                                                ctx.getSource().sendFailure(Component.literal("Type must be accepted, completed or updated"));
                                                                return 0;
                                                            }

                                                            ModNetwork.CHANNEL.send(
                                                                    PacketDistributor.PLAYER.with(() -> player),
                                                                    new QuestToastPacket(type, "")
                                                            );

                                                            return 1;
                                                        })
                                        )
                        )
        );
    }

    private static void registerQuestToastConfigCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("dlquesttoastconfig")
                        .requires(source -> source.hasPermission(2))
                        .then(
                                Commands.argument("player", EntityArgument.player())
                                        .then(
                                                Commands.literal("reset")
                                                        .executes(ctx -> {
                                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");

                                                            ModNetwork.CHANNEL.send(
                                                                    PacketDistributor.PLAYER.with(() -> player),
                                                                    new QuestToastConfigPacket(true, 0, 0, 0, 0, 0, 0, 0, 0)
                                                            );

                                                            return 1;
                                                        })
                                        )
                                        .then(
                                                Commands.literal("set")
                                                        .then(
                                                                Commands.argument("x", IntegerArgumentType.integer())
                                                                        .then(
                                                                                Commands.argument("y", IntegerArgumentType.integer())
                                                                                        .then(
                                                                                                Commands.argument("width", IntegerArgumentType.integer(1))
                                                                                                        .then(
                                                                                                                Commands.argument("height", IntegerArgumentType.integer(1))
                                                                                                                        .then(
                                                                                                                                Commands.argument("fadeIn", IntegerArgumentType.integer(1))
                                                                                                                                        .then(
                                                                                                                                                Commands.argument("stay", IntegerArgumentType.integer(1))
                                                                                                                                                        .then(
                                                                                                                                                                Commands.argument("fadeOut", IntegerArgumentType.integer(1))
                                                                                                                                                                        .then(
                                                                                                                                                                                Commands.argument("startOffsetX", IntegerArgumentType.integer())
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
                                                                                                                                                                                        })
                                                                                                                                                                        )
                                                                                                                                                        )
                                                                                                                                        )
                                                                                                                        )
                                                                                                        )
                                                                                        )
                                                                        )
                                                        )
                                        )
                        )
        );
    }

    private static void registerNpcSayCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("dlnpcsay")
                        .requires(source -> source.hasPermission(2))
                        .then(
                                Commands.argument("player", EntityArgument.player())
                                        .then(
                                                Commands.argument("payload", StringArgumentType.greedyString())
                                                        .executes(ctx -> {
                                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                                            String payload = StringArgumentType.getString(ctx, "payload");

                                                            String npcName = "[NPC]";
                                                            String text = payload;

                                                            int sep = payload.indexOf("||");
                                                            if (sep >= 0) {
                                                                npcName = payload.substring(0, sep).trim();
                                                                text = payload.substring(sep + 2).trim();
                                                            }

                                                            ModNetwork.CHANNEL.send(
                                                                    PacketDistributor.PLAYER.with(() -> player),
                                                                    new NpcDialoguePacket(npcName, text)
                                                            );

                                                            return 1;
                                                        })
                                        )
                        )
        );
    }

    private static void registerNpcSayConfigCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("dlnpcsayconfig")
                        .requires(source -> source.hasPermission(2))
                        .then(
                                Commands.argument("player", EntityArgument.player())
                                        .then(
                                                Commands.literal("reset")
                                                        .executes(ctx -> {
                                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");

                                                            ModNetwork.CHANNEL.send(
                                                                    PacketDistributor.PLAYER.with(() -> player),
                                                                    new NpcDialogueConfigPacket(true, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
                                                            );

                                                            return 1;
                                                        })
                                        )
                                        .then(
                                                Commands.literal("set")
                                                        .then(
                                                                Commands.argument("x", IntegerArgumentType.integer())
                                                                        .then(
                                                                                Commands.argument("yOffsetFromBottom", IntegerArgumentType.integer())
                                                                                        .then(
                                                                                                Commands.argument("width", IntegerArgumentType.integer(1))
                                                                                                        .then(
                                                                                                                Commands.argument("height", IntegerArgumentType.integer(1))
                                                                                                                        .then(
                                                                                                                                Commands.argument("fadeIn", IntegerArgumentType.integer(1))
                                                                                                                                        .then(
                                                                                                                                                Commands.argument("stay", IntegerArgumentType.integer(1))
                                                                                                                                                        .then(
                                                                                                                                                                Commands.argument("fadeOut", IntegerArgumentType.integer(1))
                                                                                                                                                                        .then(
                                                                                                                                                                                Commands.argument("textMaxChars", IntegerArgumentType.integer(1))
                                                                                                                                                                                        .then(
                                                                                                                                                                                                Commands.argument("textMaxLines", IntegerArgumentType.integer(1))
                                                                                                                                                                                                        .then(
                                                                                                                                                                                                                Commands.argument("nameXOffset", IntegerArgumentType.integer())
                                                                                                                                                                                                                        .then(
                                                                                                                                                                                                                                Commands.argument("nameYOffset", IntegerArgumentType.integer())
                                                                                                                                                                                                                                        .then(
                                                                                                                                                                                                                                                Commands.argument("textXOffset", IntegerArgumentType.integer())
                                                                                                                                                                                                                                                        .then(
                                                                                                                                                                                                                                                                Commands.argument("textYOffset", IntegerArgumentType.integer())
                                                                                                                                                                                                                                                                        .then(
                                                                                                                                                                                                                                                                                Commands.argument("textLineHeight", IntegerArgumentType.integer(1))
                                                                                                                                                                                                                                                                                        .executes(ctx -> {
                                                                                                                                                                                                                                                                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");

                                                                                                                                                                                                                                                                                            ModNetwork.CHANNEL.send(
                                                                                                                                                                                                                                                                                                    PacketDistributor.PLAYER.with(() -> player),
                                                                                                                                                                                                                                                                                                    new NpcDialogueConfigPacket(
                                                                                                                                                                                                                                                                                                            false,
                                                                                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "x"),
                                                                                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "yOffsetFromBottom"),
                                                                                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "width"),
                                                                                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "height"),
                                                                                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "fadeIn"),
                                                                                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "stay"),
                                                                                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "fadeOut"),
                                                                                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "textMaxChars"),
                                                                                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "textMaxLines"),
                                                                                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "nameXOffset"),
                                                                                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "nameYOffset"),
                                                                                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "textXOffset"),
                                                                                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "textYOffset"),
                                                                                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "textLineHeight")
                                                                                                                                                                                                                                                                                                    )
                                                                                                                                                                                                                                                                                            );

                                                                                                                                                                                                                                                                                            return 1;
                                                                                                                                                                                                                                                                                        })
                                                                                                                                                                                                                                                                        )
                                                                                                                                                                                                                                                        )
                                                                                                                                                                                                                                        )
                                                                                                                                                                                                                        )
                                                                                                                                                                                                        )
                                                                                                                                                                                        )
                                                                                                                                                                        )
                                                                                                                                                        )
                                                                                                                                        )
                                                                                                                        )
                                                                                                        )
                                                                                        )
                                                                        )
                                                        )
                                        )
                        )
        );
    }
}
