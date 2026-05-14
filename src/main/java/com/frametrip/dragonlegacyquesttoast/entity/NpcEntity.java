package com.frametrip.dragonlegacyquesttoast.entity;

import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.NpcDialoguePacket;
import com.frametrip.dragonlegacyquesttoast.network.NpcStartScenePacket;
import com.frametrip.dragonlegacyquesttoast.network.OpenCompanionScreenPacket;
import com.frametrip.dragonlegacyquesttoast.network.OpenTraderShopPacket;
import com.frametrip.dragonlegacyquesttoast.profession.NpcProfessionType;
import com.frametrip.dragonlegacyquesttoast.server.animation.NpcAnimationData;
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

import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.util.GeckoLibUtil;
import java.util.Objects;

public class NpcEntity extends PathfinderMob implements GeoEntity {

    // instance-level cache — must NOT be static
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    // Parsed NpcEntityData cache — invalidated whenever DATA_NPC_JSON changes.
    // Avoids repeated GSON.fromJson() in tick(), movementPredicate(), and render methods.
    private NpcEntityData cachedNpcData;

    private static final Gson GSON = new Gson();

    public static final EntityDataAccessor<String> DATA_NPC_JSON =
            SynchedEntityData.defineId(NpcEntity.class, EntityDataSerializers.STRING);

    /** "AUTO" = derive from movement; any other value = forced animation name. */
    public static final EntityDataAccessor<String> DATA_ANIM_STATE =
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
        entityData.define(DATA_ANIM_STATE, "AUTO");
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new CompanionGoal(this));
        goalSelector.addGoal(2, new com.frametrip.dragonlegacyquesttoast.server.companion.CompanionGuardGoal(this));
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
        if (cachedNpcData == null) {
            NpcEntityData d = GSON.fromJson(entityData.get(DATA_NPC_JSON), NpcEntityData.class);
            cachedNpcData = d != null ? d : new NpcEntityData();
        }
        return cachedNpcData;
    }

    public void setNpcData(NpcEntityData data) {
        cachedNpcData = data;
        entityData.set(DATA_NPC_JSON, GSON.toJson(data));
        applyDataEffects(data);
    }

    /** Force a specific animation state, synced to all clients. */
    public void setAnimState(com.frametrip.dragonlegacyquesttoast.server.animation.AnimationState state) {
        NpcEntityData data = getNpcData();
        NpcAnimationData anim = findAnimationForState(data, state);
        String animName = anim != null
                ? "animation.npc." + anim.name.toLowerCase().replace(' ', '_')
                : "animation.npc." + state.name().toLowerCase();
        entityData.set(DATA_ANIM_STATE, animName);
    }

    /** Return to automatic movement-based animation selection. */
    public void clearAnimState() {
        entityData.set(DATA_ANIM_STATE, "AUTO");
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
            cachedNpcData = null;
            NpcEntityData data = GSON.fromJson(json, NpcEntityData.class);
            if (data != null) {
                cachedNpcData = data;
                applyDataEffects(data);
            }
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
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            net.minecraft.world.DifficultyInstance difficulty,
            MobSpawnType reason,
            net.minecraft.world.entity.SpawnGroupData spawnData,
            net.minecraft.nbt.CompoundTag dataTag) {
        net.minecraft.world.entity.SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        this.setXRot(0f);
        this.setYRot(0f);
        this.yHeadRot = 0f;
        this.yBodyRot = 0f;
        return result;
    }

    @Override
    public Component getDisplayName() {
        String name = getNpcData().displayName;
        if (name != null && !name.isEmpty()) {
            return Component.literal(name);
        }
        return super.getDisplayName();
    }

    // ── GeckoLib ──────────────────────────────────────────────────────────────

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, this::movementPredicate));
    }

    private PlayState movementPredicate(AnimationState<NpcEntity> state) {
        String forced = entityData.get(DATA_ANIM_STATE);
        if (!"AUTO".equals(forced)) {
            state.getController().setAnimation(
                    RawAnimation.begin().then(forced, Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }

        NpcEntityData data = getNpcData();
        // Use navigation activity as well — slow NPCs (speed ≈0.06 blocks/tick) may
        // fall below GeckoLib's isMoving() velocity threshold while the pathfinder is active.
        boolean moving = state.isMoving() || this.getNavigation().isInProgress();
        if (moving) {
            // 1. custom WALK binding  2. profession walk binding  3. default name
            NpcAnimationData anim = findAnimationForState(data,
                    com.frametrip.dragonlegacyquesttoast.server.animation.AnimationState.WALK);
            if (anim == null) anim = findAnimationForState(data, professionWalkState(data));
            String name = anim != null
                    ? "animation.npc." + anim.name.toLowerCase().replace(' ', '_')
                    : "animation.npc.walk";
            state.getController().setAnimation(
                    RawAnimation.begin().then(name, findAnimLooping(anim)));
        } else {
            // 1. custom IDLE binding  2. profession idle binding  3. default name
            NpcAnimationData anim = findAnimationForState(data,
                    com.frametrip.dragonlegacyquesttoast.server.animation.AnimationState.IDLE);
            if (anim == null) anim = findAnimationForState(data, professionIdleState(data));
            String name = anim != null
                    ? "animation.npc." + anim.name.toLowerCase().replace(' ', '_')
                    : "animation.npc.idle";
            state.getController().setAnimation(
                    RawAnimation.begin().then(name, findAnimLooping(anim)));
        }
        return PlayState.CONTINUE;
    }

    /** Maps profession to a preferred idle AnimationState for the fallback chain. */
    private com.frametrip.dragonlegacyquesttoast.server.animation.AnimationState professionIdleState(
            NpcEntityData data) {
        if (data.professionData == null) return com.frametrip.dragonlegacyquesttoast.server.animation.AnimationState.IDLE;
        return switch (data.professionData.type) {
            case GUARD    -> com.frametrip.dragonlegacyquesttoast.server.animation.AnimationState.GUARD;
            case TRADER   -> com.frametrip.dragonlegacyquesttoast.server.animation.AnimationState.WORK;
            case FARMER   -> com.frametrip.dragonlegacyquesttoast.server.animation.AnimationState.WORK;
            case MINER    -> com.frametrip.dragonlegacyquesttoast.server.animation.AnimationState.WORK;
            case BUILDER  -> com.frametrip.dragonlegacyquesttoast.server.animation.AnimationState.WORK;
            default       -> com.frametrip.dragonlegacyquesttoast.server.animation.AnimationState.IDLE;
        };
    }

    /** Maps profession to a preferred walk AnimationState for the fallback chain. */
    private com.frametrip.dragonlegacyquesttoast.server.animation.AnimationState professionWalkState(
            NpcEntityData data) {
        if (data.professionData == null) return com.frametrip.dragonlegacyquesttoast.server.animation.AnimationState.WALK;
        return switch (data.professionData.type) {
            case COMPANION, FOLLOWER ->
                    com.frametrip.dragonlegacyquesttoast.server.animation.AnimationState.FOLLOW;
            default ->
                    com.frametrip.dragonlegacyquesttoast.server.animation.AnimationState.WALK;
        };
    }

    private NpcAnimationData findAnimationForState(
            NpcEntityData data,
            com.frametrip.dragonlegacyquesttoast.server.animation.AnimationState target) {
        if (data.animations == null) return null;
        for (NpcAnimationData a : data.animations) {
            if (a.stateBinding == target) return a;
        }
        return null;
    }

    private Animation.LoopType findAnimLooping(NpcAnimationData anim) {
        return (anim == null || anim.loop) ? Animation.LoopType.LOOP : Animation.LoopType.HOLD_ON_LAST_FRAME;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
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
