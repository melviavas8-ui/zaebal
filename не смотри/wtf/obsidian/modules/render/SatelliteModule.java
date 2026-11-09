package wtf.obsidian.modules.render;

import wtf.obsidian.*;
import wtf.obsidian.manager.*;
import wtf.obsidian.utils.*;
import wtf.obsidian.engine.*;
import wtf.obsidian.event.*;
import wtf.obsidian.commands.*;
import wtf.obsidian.settings.*;
import wtf.obsidian.hud.*;
import wtf.obsidian.modules.combat.*;
import wtf.obsidian.modules.movement.*;
import wtf.obsidian.modules.render.*;
import wtf.obsidian.modules.player.*;
import wtf.obsidian.modules.misc.*;
import wtf.obsidian.mixin.*;
import wtf.obsidian.Module;
import wtf.obsidian.event.Event;

























import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.entity.model.AllayEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.AllayEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class SatelliteModule extends Module {
   private static final Identifier ALLAY_TEXTURE = Identifier.ofVanilla("textures/entity/allay/allay.png");
   public static final long ATTACK_FOLLOW_TIMEOUT_MS = 3600L;
   public static final long ATTACK_LAUNCH_DURATION_MS = 560L;
   public static final long ATTACK_RETURN_DURATION_MS = 920L;
   public static SatelliteModule INSTANCE = new SatelliteModule();
   public final ModeSetting shoulder;
   public final SliderSetting scale;
   public final SliderSetting offsetX;
   public final SliderSetting offsetY;
   public final SliderSetting offsetZ;
   public final SliderSetting rotateX;
   public final SliderSetting rotateY;
   public final SliderSetting rotateZ;
   public final BooleanSetting showSelf;
   public final BooleanSetting showOthers;
   public final BooleanSetting showFriends;
   public final BooleanSetting attackEnemies;
   public final BooleanSetting idleAnimation;
   public final SliderSetting idleSpeed;
   public final SliderSetting idleStrength;
   public final AllayEntityRenderState attackState;
   public AllayEntityModel attackModel;
   public int attackTargetId;
   public long attackStartedAt;
   public long lastAttackAt;
   public long attackReturnStartedAt;
   public Vec3d attackReturnStartPos;
   public float attackOrbitSeed;
   public float attackCurveSide;
   public float attackCurveLift;
   public float attackCurveDepth;
   public float attackRadiusJitter;
   public float attackHeightJitter;
   public float attackBobSeed;
   public float attackOrbitSpeed;
   public float attackOrbitDirection;
   public float attackLookYaw;
   public float attackLookPitch;
   public boolean attackLookInitialized;

   public SatelliteModule() {
      super("Satellite", "Питомец-аллей на плече", Category.RENDER);
      String[] var10005 = new String[2];
      var10005[0] = "Правое";
      var10005[1] = "Левое";
      this.shoulder = new ModeSetting("Плечо", "Правое", var10005);
      this.scale = new SliderSetting(
         "Размер",
         0.3799999952316284f,
         0.15000000596046448f,
         1.25f,
         0.009999999776482582f
      );
      this.offsetX = new SliderSetting(
         "Смещение X",
         0.0f,
         -1.0f,
         1.0f,
         0.009999999776482582f
      );
      this.offsetY = new SliderSetting(
         "Смещение Y",
         0.18000000715255737f,
         -1.0f,
         1.0F,
         0.009999999776482582f
      );
      this.offsetZ = new SliderSetting(
         "Смещение Z",
         0.0f,
         -1.0f,
         1.0f,
         0.009999999776482582f
      );
      this.rotateX = new SliderSetting(
         "Поворот X",
         0.0f,
         -180.0f,
         180.0F,
         1.0f
      );
      this.rotateY = new SliderSetting(
         "Поворот Y",
         0.0f,
         -180.0f,
         180.0f,
         1.0f
      );
      this.rotateZ = new SliderSetting(
         "Поворот Z",
         0.0f,
         -180.0f,
         180.0f,
         1.0f
      );
      this.showSelf = new BooleanSetting("Показывать на себе", true);
      this.showOthers = new BooleanSetting("Показывать на других", true);
      this.showFriends = new BooleanSetting("Показывать на друзьях", true);
      this.attackEnemies = new BooleanSetting("Атаковать врагов", true);
      this.idleAnimation = new BooleanSetting("Idle-анимация", true);
      this.idleSpeed = new SliderSetting(
            "Скорость idle",
            1.0f,
            0.10000000149011612f,
            3.0f,
            0.05000000074505806f
         )
         .visible(() -> this.idleAnimation.isState());
      this.idleStrength = new SliderSetting(
            "Сила idle",
            0.3499999940395355f,
            0.0f,
            1.5f,
            0.05000000074505806f
         )
         .visible(() -> this.idleAnimation.isState());
      this.attackState = new AllayEntityRenderState();
      this.attackTargetId = -2147483648;
      this.attackReturnStartPos = new Vec3d(
         0.0,
         0.0,
         0.0
      );
      Setting[] var10001 = new Setting[15];
      var10001[0] = this.shoulder;
      var10001[1] = this.scale;
      var10001[2] = this.offsetX;
      var10001[3] = this.offsetY;
      var10001[4] = this.offsetZ;
      var10001[5] = this.rotateX;
      var10001[6] = this.rotateY;
      var10001[7] = this.rotateZ;
      var10001[8] = this.showSelf;
      var10001[9] = this.showOthers;
      var10001[10] = this.showFriends;
      var10001[11] = this.attackEnemies;
      var10001[12] = this.idleAnimation;
      var10001[13] = this.idleSpeed;
      var10001[14] = this.idleStrength;
      this.addSettings(var10001);
   }

   @Override
   public void onDisable() {
      this.clearAttackTarget();
      super.onDisable();
   }

   @EventTarget
   public void onAttack(Event_358 event) {
      if (this.attackEnemies.isState() && event != null && event.getPlayer() != null && event.getTarget() != null && mc.player != null) {
         if (event.getPlayer().getId() == mc.player.getId() && event.getTarget() != mc.player) {
            long now = System.currentTimeMillis();
            if (this.attackTargetId != event.getTarget().getId()) {
               this.attackStartedAt = now;
               this.randomizeAttackPath(now);
            }

            this.attackTargetId = event.getTarget().getId();
            this.lastAttackAt = now;
            this.attackReturnStartedAt = 0L;
         }
      }
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (!this.attackEnemies.isState()) {
         this.clearAttackTarget();
      } else {
         this.updateAttackLifecycle();
      }
   }

   @EventTarget
   public void onRender3D(RenderEvent event) {
      if (mc.player != null && mc.world != null && event != null) {
         float tickDelta = event.getTickDelta();
         long now = System.currentTimeMillis();
         Entity target = this.updateAttackLifecycle();
         if (target != null) {
            this.ensureAttackModel();
            if (this.attackModel != null) {
               this.renderAttackSatellite(event, target, this.getAttackRenderPosition(target, tickDelta, now), tickDelta, now);
            }
         }
      }
   }
   public void renderAttackSatellite(RenderEvent event, Entity target, Vec3d renderPos, float tickDelta, long now) {
      Vec3d cameraPos = event.getCamera().getPos();
      Vec3d targetPos = this.getInterpolatedEntityPos(target, tickDelta);
      float elapsed = (float)(now - this.attackStartedAt) / 1000.0f;
      Vec3d focusPos = targetPos.add(
         0.0,
         (double)target.getHeight() * 0.56,
         0.0
      );
      float desiredYaw = this.getLookYaw(renderPos, focusPos);
      float desiredPitch = this.getLookPitch(renderPos, focusPos);
      if (!this.attackLookInitialized) {
         this.attackLookYaw = desiredYaw;
         this.attackLookPitch = desiredPitch;
         this.attackLookInitialized = true;
      } else {
         this.attackLookYaw = MathHelper.lerpAngleDegrees(0.3199999928474426f, this.attackLookYaw, desiredYaw);
         this.attackLookPitch = MathHelper.lerp(0.23999999463558197f, this.attackLookPitch, desiredPitch);
      }

      float headYaw = MathHelper.clamp(
         MathHelper.wrapDegrees(desiredYaw - this.attackLookYaw),
         -85.0f,
         85.0f
      );
      MatrixStack matrices = event.getMatrices();
      matrices.push();
      matrices.translate(renderPos.x - cameraPos.x, renderPos.y - cameraPos.y, renderPos.z - cameraPos.z);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - this.attackLookYaw));
      matrices.scale(this.scale.get(), this.scale.get(), this.scale.get());
      matrices.scale(
         -1.0f,
         -1.0f,
         1.0f
      );
      matrices.translate(
         0.0f,
         -1.5010000467300415f,
         0.0f
      );
      this.attackState.age = (float)mc.player.age + tickDelta + elapsed * 20.0f;
      this.attackState.limbFrequency = elapsed * 6.400000095367432f;
      this.attackState.limbAmplitudeMultiplier = 0.7200000286102295f
         + MathHelper.sin(elapsed * 7.0f + this.attackBobSeed)
            * 0.11999999731779099f;
      this.attackState.yawDegrees = headYaw;
      this.attackState.pitch = this.attackLookPitch;
      this.attackState.invisible = false;
      this.attackState.invisibleToPlayer = false;
      this.attackState.hasOutline = false;
      this.attackState.shaking = false;
      this.attackState.baby = false;
      this.attackState.touchingWater = target.isTouchingWater();
      this.attackState.bodyYaw = this.attackLookYaw;
      this.attackState.baseScale = 1.0f;
      this.attackState.ageScale = 1.0f;
      this.attackState.pose = target instanceof LivingEntity living ? living.getPose() : EntityPose.STANDING;
      this.attackState.deathTime = 0.0f;
      this.attackState.hurt = false;
      this.attackState.dancing = false;
      this.attackState.spinning = false;
      this.attackState.spinningAnimationTicks = 0.0f;
      this.attackState.itemHoldAnimationTicks = 0.6499999761581421f;
      this.attackModel.setAngles(this.attackState);
      Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
      VertexConsumer vertexConsumer = immediate.getBuffer(this.attackModel.getLayer(ALLAY_TEXTURE));
      this.attackModel.render(matrices, vertexConsumer, 15728880, OverlayTexture.DEFAULT_UV);
      immediate.draw();
      matrices.pop();
   }

   public boolean shouldRender(PlayerEntityRenderState playerState) {
      if (this.isEnable() && mc.player != null && mc.world != null && playerState != null && !playerState.spectator) {
         boolean self = (playerState.id == mc.player.getId());
         if (!self) {
            if (mc.world.getEntityById(playerState.id) instanceof PlayerEntity player
               && Obsidian.INSTANCE != null
               && Obsidian.INSTANCE.friendStorage != null
               && Obsidian.INSTANCE.friendStorage.isFriend(player.getName().getString())) {
               return this.showFriends.isState();
            }

            return this.showOthers.isState();
         } else {
            return (!this.hasActiveAttackTarget() && this.shouldRenderOwnShoulderPet());
         }
      } else {
         return false;
      }
   }

   public boolean isLeftShoulder() {
      return this.shoulder.is("Левое");
   }

   public boolean hasActiveAttackTarget() {
      return (this.updateAttackLifecycle() != null);
   }
   public boolean shouldRenderOwnShoulderPet() {
      return (this.showSelf.isState() && !mc.options.getPerspective().isFirstPerson());
   }
   public Entity updateAttackLifecycle() {
      if (this.attackEnemies.isState() && mc.world != null && mc.player != null && this.attackTargetId != -2147483648) {
         Entity target = mc.world.getEntityById(this.attackTargetId);
         if (target != null && !target.isRemoved() && target != mc.player) {
            if (target instanceof LivingEntity living && !living.isAlive()) {
               this.clearAttackTarget();
               return null;
            }

            if (mc.player.squaredDistanceTo(target) > 4096.0) {
               this.clearAttackTarget();
               return null;
            } else {
               long now = System.currentTimeMillis();
               if (this.attackReturnStartedAt == (0L)
                  && now - this.lastAttackAt > (3600L)) {
                  float elapsed = (float)(now - this.attackStartedAt) / 1000.0f;
                  this.attackReturnStartPos = this.getOrbitPosition(
                     target, this.getInterpolatedEntityPos(target, 1.0f), elapsed
                  );
                  this.attackReturnStartedAt = now;
               }

               if (this.attackReturnStartedAt != (0L)
                  && now - this.attackReturnStartedAt > (920L)) {
                  this.clearAttackTarget();
                  return null;
               } else {
                  return target;
               }
            }
         } else {
            this.clearAttackTarget();
            return null;
         }
      } else {
         return null;
      }
   }
   public Vec3d getAttackRenderPosition(Entity target, float tickDelta, long now) {
      Vec3d shoulderPos = this.getShoulderWorldPosition(tickDelta);
      Vec3d targetPos = this.getInterpolatedEntityPos(target, tickDelta);
      float elapsed = (float)(now - this.attackStartedAt) / 1000.0f;
      Vec3d orbitPos = this.getOrbitPosition(target, targetPos, elapsed);
      if (this.attackReturnStartedAt == (0L)) {
         float launchProgress = MathHelper.clamp(
            (float)(now - this.attackStartedAt) / 560.0f,
            0.0f,
            1.0f
         );
         return launchProgress < 1.0f
            ? this.buildLaunchCurve(shoulderPos, orbitPos, launchProgress)
            : orbitPos;
      } else {
         float returnProgress = MathHelper.clamp(
            (float)(now - this.attackReturnStartedAt) / 920.0f,
            0.0f,
            1.0f
         );
         return this.buildReturnCurve(this.attackReturnStartPos, shoulderPos, returnProgress);
      }
   }
   public Vec3d getOrbitPosition(Entity target, Vec3d targetPos, float elapsed) {
      double baseRadius = Math.max(
            0.86,
            (double)target.getWidth() * 1.05
               + 0.46
         )
         * (double)this.attackRadiusJitter;
      double angle = (double)(
         this.attackOrbitSeed * 0.01745329238474369f + elapsed * this.attackOrbitSpeed * this.attackOrbitDirection
      );
      double radiusPulse = Math.sin(
            (double)(
               elapsed * 1.25f
                  + this.attackBobSeed * 0.44999998807907104f
            )
         )
         * 0.07;
      double orbitRadius = baseRadius + radiusPulse;
      double orbitX = Math.cos(angle) * orbitRadius;
      double orbitZ = Math.sin(angle) * orbitRadius;
      double orbitY = targetPos.y
         + (double)target.getHeight()
            * (0.78 + (double)this.attackHeightJitter)
         + Math.sin((double)(elapsed * 2.9000000953674316f + this.attackBobSeed))
            * 0.2
         + Math.cos(
               (double)(
                  elapsed * 1.7999999523162842f
                     + this.attackBobSeed * 0.800000011920929f
               )
            )
            * 0.08;
      return new Vec3d(targetPos.x + orbitX, orbitY, targetPos.z + orbitZ);
   }
   public Vec3d buildLaunchCurve(Vec3d start, Vec3d end, float progress) {
      float eased = this.easeInOut(progress);
      Vec3d direction = end.subtract(start);
      Vec3d horizontal = new Vec3d(direction.x, 0.0, direction.z);
      if (horizontal.lengthSquared() < 0.0001) {
         horizontal = new Vec3d(
            0.0,
            0.0,
            1.0
         );
      } else {
         horizontal = horizontal.normalize();
      }

      Vec3d sideways = new Vec3d(horizontal.z, 0.0, -horizontal.x)
         .normalize();
      Vec3d lift = new Vec3d(
         0.0,
         (double)this.attackCurveLift,
         0.0
      );
      Vec3d control1 = start.add(
            sideways.multiply((double)this.attackCurveSide * 0.52)
         )
         .add(lift.multiply(0.82));
      Vec3d control2 = end.add(
            sideways.multiply((double)(-this.attackCurveSide) * 0.28)
         )
         .add(horizontal.multiply((double)this.attackCurveDepth * 0.18))
         .add(lift.multiply(0.58));
      return this.cubicBezier(start, control1, control2, end, eased);
   }
   public Vec3d buildReturnCurve(Vec3d start, Vec3d end, float progress) {
      float eased = this.easeInOut(progress);
      Vec3d direction = end.subtract(start);
      Vec3d horizontal = new Vec3d(direction.x, 0.0, direction.z);
      if (horizontal.lengthSquared() < 0.0001) {
         horizontal = new Vec3d(
            0.0,
            0.0,
            1.0
         );
      } else {
         horizontal = horizontal.normalize();
      }

      Vec3d sideways = new Vec3d(horizontal.z, 0.0, -horizontal.x)
         .normalize();
      Vec3d lift = new Vec3d(
         0.0,
         (double)this.attackCurveLift * 0.72,
         0.0
      );
      Vec3d control1 = start.add(
            sideways.multiply((double)(-this.attackCurveSide) * 0.24)
         )
         .add(lift.multiply(0.62));
      Vec3d control2 = end.add(
            sideways.multiply((double)this.attackCurveSide * 0.3)
         )
         .add(
            horizontal.multiply((double)(-this.attackCurveDepth) * 0.1)
         )
         .add(lift.multiply(0.22));
      Vec3d bezier = this.cubicBezier(start, control1, control2, end, eased);
      return eased > 0.985F ? end : bezier;
   }
   public Vec3d getShoulderWorldPosition(float tickDelta) {
      Vec3d playerPos = this.getInterpolatedEntityPos(mc.player, tickDelta);
      float bodyYaw = MathHelper.lerpAngleDegrees(tickDelta, mc.player.prevBodyYaw, mc.player.bodyYaw);
      float yawRad = bodyYaw * 0.01745329238474369f;
      Vec3d forward = new Vec3d(
         (double)(-MathHelper.sin(yawRad)),
         0.0,
         (double)MathHelper.cos(yawRad)
      );
      Vec3d right = new Vec3d(forward.z, 0.0, -forward.x);
      double side = (
            this.isLeftShoulder()
               ? 1.0
               : Double.longBitsToDouble(-4616189618054758400L)
         )
         * (double)mc.player.getWidth()
         * 0.42;
      double height = (double)mc.player.getHeight()
         - (
            mc.player.isSneaking()
               ? 0.38
               : 0.24
         );
      double back = 0.0;
      Vec3d shoulderPos = playerPos.add(
            0.0,
            height,
            0.0
         )
         .add(right.multiply(side))
         .add(forward.multiply(back))
         .add(right.multiply((double)this.offsetX.get() * 0.65))
         .add(
            0.0,
            (double)this.offsetY.get() * 0.45,
            0.0
         )
         .add(forward.multiply((double)this.offsetZ.get() * 0.35));
      if (this.idleAnimation.isState()) {
         float time = ((float)mc.player.age + tickDelta)
            * (0.699999988079071f + this.idleSpeed.get() * 0.6499999761581421f);
         float bob = MathHelper.sin(time * 0.41999998688697815f)
            * 0.029999999329447746f
            * this.idleStrength.get();
         shoulderPos = shoulderPos.add(
            0.0,
            (double)bob,
            0.0
         );
      }

      return shoulderPos;
   }
   public Vec3d getInterpolatedEntityPos(Entity entity, float tickDelta) {
      return new Vec3d(
         MathHelper.lerp((double)tickDelta, entity.prevX, entity.getX()),
         MathHelper.lerp((double)tickDelta, entity.prevY, entity.getY()),
         MathHelper.lerp((double)tickDelta, entity.prevZ, entity.getZ())
      );
   }
   public Vec3d cubicBezier(Vec3d p0, Vec3d p1, Vec3d p2, Vec3d p3, float t) {
      float inv = 1.0f - t;
      double w0 = (double)(inv * inv * inv);
      double w1 = 3.0 * (double)inv * (double)inv * (double)t;
      double w2 = 3.0 * (double)inv * (double)t * (double)t;
      double w3 = (double)(t * t * t);
      return new Vec3d(
         p0.x * w0 + p1.x * w1 + p2.x * w2 + p3.x * w3, p0.y * w0 + p1.y * w1 + p2.y * w2 + p3.y * w3, p0.z * w0 + p1.z * w1 + p2.z * w2 + p3.z * w3
      );
   }
   public float easeInOut(float value) {
      float clamped = MathHelper.clamp(
         value, 0.0f, 1.0f
      );
      return clamped
         * clamped
         * clamped
         * (
            clamped * (clamped * 6.0f - 15.0f)
               + 10.0f
         );
   }
   public void ensureAttackModel() {
      if (this.attackModel == null && mc != null) {
         this.attackModel = new AllayEntityModel(mc.getLoadedEntityModels().getModelPart(EntityModelLayers.ALLAY));
      }
   }
   public void randomizeAttackPath(long now) {
      this.attackOrbitSeed = this.randomRange(
         0.0f, 360.0f
      );
      this.attackCurveSide = this.randomRange(
         -1.100000023841858f, 1.100000023841858f
      );
      this.attackCurveLift = this.randomRange(0.47999998927116394f, 0.9599999785423279f);
      this.attackCurveDepth = this.randomRange(
         -0.41999998688697815f, 0.41999998688697815f
      );
      this.attackRadiusJitter = this.randomRange(
         0.9200000166893005f, 1.2400000095367432f
      );
      this.attackHeightJitter = this.randomRange(
         -0.05999999865889549f, 0.14000000059604645f
      );
      this.attackBobSeed = this.randomRange(0.0f, 6.2831854820251465f);
      this.attackOrbitSpeed = this.randomRange(
         1.7000000476837158f, 2.450000047683716f
      );
      this.attackOrbitDirection = Math.random() > 0.5
         ? 1.0f
         : -1.0f;
   }
   public float randomRange(float min, float max) {
      return min + (float)Math.random() * (max - min);
   }
   public float getLookYaw(Vec3d from, Vec3d to) {
      double dx = to.x - from.x;
      double dz = to.z - from.z;
      return (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
   }
   public float getLookPitch(Vec3d from, Vec3d to) {
      double dx = to.x - from.x;
      double dy = to.y - from.y;
      double dz = to.z - from.z;
      double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
      return MathHelper.clamp(
         (float)(-Math.toDegrees(Math.atan2(dy, horizontalDistance))),
         -35.0f,
         35.0f
      );
   }
   public void clearAttackTarget() {
      this.attackTargetId = -2147483648;
      this.attackStartedAt = 0L;
      this.lastAttackAt = 0L;
      this.attackReturnStartedAt = 0L;
      this.attackReturnStartPos = new Vec3d(
         0.0,
         0.0,
         0.0
      );
      this.attackLookYaw = 0.0f;
      this.attackLookPitch = 0.0f;
      this.attackLookInitialized = false;
   }
}
