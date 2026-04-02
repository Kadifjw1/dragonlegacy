package com.frametrip.dragonlegacyquesttoast.command;

import com.frametrip.dragonlegacyquesttoast.network.AwakeningBackgroundConfigPacket;
import com.frametrip.dragonlegacyquesttoast.network.AwakeningCenterConfigPacket;
import com.frametrip.dragonlegacyquesttoast.network.AwakeningPathsConfigPacket;
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.NpcDialogueConfigPacket;
import com.frametrip.dragonlegacyquesttoast.network.NpcDialoguePacket;
import com.frametrip.dragonlegacyquesttoast.network.OpenAwakeningScreenPacket;
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
        registerAwakeningOpenCommand(dispatcher);
        registerAwakeningBackgroundCommand(dispatcher);
        registerAwakeningCenterCommand(dispatcher);
        registerAwakeningPathsCommand(dispatcher);
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

                                                                                                                                                                                            ModNetwork.CHANNEL.send(
                                                                                                                                                                                                    PacketDistributor.PLAYER.with(() -> player),
                                                                                                                                                                                                    new QuestToastConfigPacket(
                                                                                                                                                                                                            false,
                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "x"),
                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "y"),
                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "width"),
                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "height"),
                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "fadeIn"),
                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "stay"),
                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "fadeOut"),
                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "startOffsetX")
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
                                                                    new NpcDialogueConfigPacket(
                                                                            true,
                                                                            0, 0, 0, 0, 0, 0, 0, 0,
                                                                            0, 0, 0, 0, 0, 0, 0, 0
                                                                    )
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
                                                                                                Commands.argument("minWidth", IntegerArgumentType.integer(1))
                                                                                                        .then(
                                                                                                                Commands.argument("maxWidth", IntegerArgumentType.integer(1))
                                                                                                                        .then(
                                                                                                                                Commands.argument("minHeight", IntegerArgumentType.integer(1))
                                                                                                                                        .then(
                                                                                                                                                Commands.argument("fadeIn", IntegerArgumentType.integer(1))
                                                                                                                                                        .then(
                                                                                                                                                                Commands.argument("stay", IntegerArgumentType.integer(1))
                                                                                                                                                                        .then(
                                                                                                                                                                                Commands.argument("fadeOut", IntegerArgumentType.integer(1))
                                                                                                                                                                                        .then(
                                                                                                                                                                                                Commands.argument("textMaxLines", IntegerArgumentType.integer(1))
                                                                                                                                                                                                        .then(
                                                                                                                                                                                                                Commands.argument("leftPadding", IntegerArgumentType.integer(0))
                                                                                                                                                                                                                        .then(
                                                                                                                                                                                                                                Commands.argument("rightPadding", IntegerArgumentType.integer(0))
                                                                                                                                                                                                                                        .then(
                                                                                                                                                                                                                                                Commands.argument("topPadding", IntegerArgumentType.integer(0))
                                                                                                                                                                                                                                                        .then(
                                                                                                                                                                                                                                                                Commands.argument("bottomPadding", IntegerArgumentType.integer(0))
                                                                                                                                                                                                                                                                        .then(
                                                                                                                                                                                                                                                                                Commands.argument("nameYOffset", IntegerArgumentType.integer())
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
                                                                                                                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "minWidth"),
                                                                                                                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "maxWidth"),
                                                                                                                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "minHeight"),
                                                                                                                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "fadeIn"),
                                                                                                                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "stay"),
                                                                                                                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "fadeOut"),
                                                                                                                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "textMaxLines"),
                                                                                                                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "leftPadding"),
                                                                                                                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "rightPadding"),
                                                                                                                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "topPadding"),
                                                                                                                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "bottomPadding"),
                                                                                                                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "nameYOffset"),
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
                                                        )
                                        )
                        )
        );
    }

    private static void registerAwakeningOpenCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("dlawakening")
                        .requires(source -> source.hasPermission(2))
                        .then(
                                Commands.literal("open")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(ctx -> {
                                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");

                                                            ModNetwork.CHANNEL.send(
                                                                    PacketDistributor.PLAYER.with(() -> player),
                                                                    new OpenAwakeningScreenPacket()
                                                            );
                                                            return 1;
                                                        })
                                        )
                        )
        );
    }

    private static void registerAwakeningBackgroundCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("dlawakeningbg")
                        .requires(source -> source.hasPermission(2))
                        .then(
                                Commands.literal("reset")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(ctx -> {
                                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");

                                                            ModNetwork.CHANNEL.send(
                                                                    PacketDistributor.PLAYER.with(() -> player),
                                                                    new AwakeningBackgroundConfigPacket(true, 0, 0, 0, 0)
                                                            );
                                                            return 1;
                                                        })
                                        )
                        )
                        .then(
                                Commands.literal("set")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .then(
                                                                Commands.argument("x", IntegerArgumentType.integer())
                                                                        .then(
                                                                                Commands.argument("y", IntegerArgumentType.integer())
                                                                                        .then(
                                                                                                Commands.argument("width", IntegerArgumentType.integer(1))
                                                                                                        .then(
                                                                                                                Commands.argument("height", IntegerArgumentType.integer(1))
                                                                                                                        .executes(ctx -> {
                                                                                                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");

                                                                                                                            ModNetwork.CHANNEL.send(
                                                                                                                                    PacketDistributor.PLAYER.with(() -> player),
                                                                                                                                    new AwakeningBackgroundConfigPacket(
                                                                                                                                            false,
                                                                                                                                            IntegerArgumentType.getInteger(ctx, "x"),
                                                                                                                                            IntegerArgumentType.getInteger(ctx, "y"),
                                                                                                                                            IntegerArgumentType.getInteger(ctx, "width"),
                                                                                                                                            IntegerArgumentType.getInteger(ctx, "height")
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
        );
    }

    private static void registerAwakeningCenterCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("dlawakeningcenter")
                        .requires(source -> source.hasPermission(2))
                        .then(
                                Commands.literal("reset")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(ctx -> {
                                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");

                                                            ModNetwork.CHANNEL.send(
                                                                    PacketDistributor.PLAYER.with(() -> player),
                                                                    new AwakeningCenterConfigPacket(true, 0, 0, 0, 0, 0, 0, 0.0F)
                                                            );
                                                            return 1;
                                                        })
                                        )
                        )
                        .then(
                                Commands.literal("set")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .then(
                                                                Commands.argument("frameX", IntegerArgumentType.integer())
                                                                        .then(
                                                                                Commands.argument("frameY", IntegerArgumentType.integer())
                                                                                        .then(
                                                                                                Commands.argument("frameWidth", IntegerArgumentType.integer(1))
                                                                                                        .then(
                                                                                                                Commands.argument("frameHeight", IntegerArgumentType.integer(1))
                                                                                                                        .then(
                                                                                                                                Commands.argument("playerOffsetX", IntegerArgumentType.integer())
                                                                                                                                        .then(
                                                                                                                                                Commands.argument("playerOffsetY", IntegerArgumentType.integer())
                                                                                                                                                        .then(
                                                                                                                                                                Commands.argument("playerScale", IntegerArgumentType.integer(1))
                                                                                                                                                                        .executes(ctx -> {
                                                                                                                                                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");

                                                                                                                                                                            ModNetwork.CHANNEL.send(
                                                                                                                                                                                    PacketDistributor.PLAYER.with(() -> player),
                                                                                                                                                                                    new AwakeningCenterConfigPacket(
                                                                                                                                                                                            false,
                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "frameX"),
                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "frameY"),
                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "frameWidth"),
                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "frameHeight"),
                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "playerOffsetX"),
                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "playerOffsetY"),
                                                                                                                                                                                            (float) IntegerArgumentType.getInteger(ctx, "playerScale")
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
        );
    }

    private static void registerAwakeningPathsCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("dlawakeningpaths")
                        .requires(source -> source.hasPermission(2))
                        .then(
                                Commands.literal("reset")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(ctx -> {
                                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");

                                                            ModNetwork.CHANNEL.send(
                                                                    PacketDistributor.PLAYER.with(() -> player),
                                                                    new AwakeningPathsConfigPacket(
                                                                            true,
                                                                            0, 0,
                                                                            0, 0,
                                                                            0, 0,
                                                                            0, 0,
                                                                            0, 0
                                                                    )
                                                            );
                                                            return 1;
                                                        })
                                        )
                        )
                        .then(
                                Commands.literal("set")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .then(
                                                                Commands.argument("pathFrameSize", IntegerArgumentType.integer(1))
                                                                        .then(
                                                                                Commands.argument("pathIconSize", IntegerArgumentType.integer(1))
                                                                                        .then(
                                                                                                Commands.argument("fireX", IntegerArgumentType.integer())
                                                                                                        .then(
                                                                                                                Commands.argument("fireY", IntegerArgumentType.integer())
                                                                                                                        .then(
                                                                                                                                Commands.argument("iceX", IntegerArgumentType.integer())
                                                                                                                                        .then(
                                                                                                                                                Commands.argument("iceY", IntegerArgumentType.integer())
                                                                                                                                                        .then(
                                                                                                                                                                Commands.argument("stormX", IntegerArgumentType.integer())
                                                                                                                                                                        .then(
                                                                                                                                                                                Commands.argument("stormY", IntegerArgumentType.integer())
                                                                                                                                                                                        .then(
                                                                                                                                                                                                Commands.argument("voidX", IntegerArgumentType.integer())
                                                                                                                                                                                                        .then(
                                                                                                                                                                                                                Commands.argument("voidY", IntegerArgumentType.integer())
                                                                                                                                                                                                                        .executes(ctx -> {
                                                                                                                                                                                                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");

                                                                                                                                                                                                                            ModNetwork.CHANNEL.send(
                                                                                                                                                                                                                                    PacketDistributor.PLAYER.with(() -> player),
                                                                                                                                                                                                                                    new AwakeningPathsConfigPacket(
                                                                                                                                                                                                                                            false,
                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "pathFrameSize"),
                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "pathIconSize"),
                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "fireX"),
                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "fireY"),
                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "iceX"),
                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "iceY"),
                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "stormX"),
                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "stormY"),
                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "voidX"),
                                                                                                                                                                                                                                            IntegerArgumentType.getInteger(ctx, "voidY")
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
        );
    }
}
