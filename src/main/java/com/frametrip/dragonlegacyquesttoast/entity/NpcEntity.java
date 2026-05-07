package com.frametrip.dragonlegacyquesttoast.entity;

import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.NpcDialoguePacket;
import com.frametrip.dragonlegacyquesttoast.network.NpcStartScenePacket;
import com.frametrip.dragonlegacyquesttoast.network.OpenCompanionScreenPacket;
import com.frametrip.dragonlegacyquesttoast.network.OpenTraderShopPacket;
import com.frametrip.dragonlegacyquesttoast.profession.NpcProfessionType;
import com.frametrip.dragonlegacyquesttoast.server.companion.CompanionGoal;
import com.frametrip.dragonlegacyquesttoast.server.DialogueDefinition;
import com.frametrip.dragonlegacyquesttoast.server.DialogueManager;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcScene;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcSceneManager;
import com.google.gson.Gson;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;

import java.util.Objects;

public class NpcEntity extends PathfinderMob {

    private static final Gson GSON = new Gson();

    public static final EntityDataAccessor<String> DATA_NPC_JSON =
            SynchedEntityData.defineId(NpcEntity.class, EntityDataSerializers.STRING);

    public NpcEntity(EntityType<? extends NpcEntity> type, Level level) {
        super(type, level);
        setCustomNameVisible(true);
        setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_NPC_JSON, GSON.toJson(new NpcEntityData()));
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new CompanionGoal(this));
        goalSelector.addGoal(3, new NpcLookAtPlayerGoal());
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.4));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.15)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    public NpcEntityData getNpcData() {
        NpcEntityData d = GSON.fromJson(entityData.get(DATA_NPC_JSON), NpcEntityData.class);
        return d != null ? d : new NpcEntityData();
    }

    public void setNpcData(NpcEntityData data) {
        entityData.set(DATA_NPC_JSON, GSON.toJson(data));
        applyDataEffects(data);
    }

    private void applyDataEffects(NpcEntityData data) {
        setCustomName(Component.literal(data.displayName));
        Objects.requireNonNull(getAttribute(Attributes.MOVEMENT_SPEED))
                .setBaseValue(data.walkSpeed * 0.25);
    }

    @Override
    public void tick() {
        super.tick();
        NpcEntityData data = getNpcData();
        setPose("CROUCHING".equals(data.idlePose) ? Pose.CROUCHING : Pose.STANDING);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("NpcData", entityData.get(DATA_NPC_JSON));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("NpcData")) {
            String json = tag.getString("NpcData");
            entityData.set(DATA_NPC_JSON, json);
            NpcEntityData data = GSON.fromJson(json, NpcEntityData.class);
            if (data != null) applyDataEffects(data);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide && !player.isShiftKeyDown()) {
            NpcEntityData data = getNpcData();
        if (player instanceof ServerPlayer sp) {
                // Profession: Trader → open shop window
                if (data.professionData != null
                        && data.professionData.type == NpcProfessionType.TRADER
                        && data.professionData.traderData != null) {
                    ModNetwork.CHANNEL.send(
                            PacketDistributor.PLAYER.with(() -> sp),
                            new OpenTraderShopPacket(this.getUUID(), data)
                    );
                    return InteractionResult.CONSUME;
                }

            // Profession: Companion → open companion control screen
                if (data.professionData != null
                        && data.professionData.type == NpcProfessionType.COMPANION) {
                    ModNetwork.CHANNEL.send(
                            PacketDistributor.PLAYER.with(() -> sp),
                            new OpenCompanionScreenPacket(this.getUUID(), data)
                    );
                    return InteractionResult.CONSUME;
                }
            
                // Prefer scene-based dialogue, fall back to legacy dialogue
                if (!data.sceneId.isEmpty()) {
                    NpcScene scene = NpcSceneManager.get(data.sceneId);
                    if (scene != null) {
                        ModNetwork.CHANNEL.send(
                                PacketDistributor.PLAYER.with(() -> sp),
                                new NpcStartScenePacket(data.displayName, data.sceneId, data.playerRelation, this.getUUID())                        );
                    }
                } else if (!data.dialogueId.isEmpty()) {
                    DialogueDefinition dlg = DialogueManager.get(data.dialogueId);
                    if (dlg != null && !dlg.lines.isEmpty()) {
                        String text = String.join("\n", dlg.lines);
                        ModNetwork.CHANNEL.send(
                                PacketDistributor.PLAYER.with(() -> sp),
                                new NpcDialoguePacket(data.displayName, text)
                        );
                    }
                }
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void playStepSound(net.minecraft.core.BlockPos pos, BlockState block) {
    }

    private class NpcLookAtPlayerGoal extends LookAtPlayerGoal {
        NpcLookAtPlayerGoal() {
            super(NpcEntity.this, Player.class, 8.0f);
        }

        @Override
        public boolean canUse() {
            return NpcEntity.this.getNpcData().lookAtPlayer && super.canUse();
        }
    }
}
