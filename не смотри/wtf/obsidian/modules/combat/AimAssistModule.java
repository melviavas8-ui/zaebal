package wtf.obsidian.modules.combat;

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

























import java.util.Random;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AimAssistModule extends Module {
   public static AimAssistModule INSTANCE = new AimAssistModule();
   public static boolean active = false;
   public static float startYaw;
   public static float startPitch;
   public static float targetYaw;
   public static float targetPitch;
   public static float tickStepYaw;
   public static float tickStepPitch;
   public static float lastPlayerYaw;
   public static float lastPlayerPitch;
   private static final Random RANDOM = new Random();
   public static float lastNoiseYaw = 0.0f;
   public static float lastNoisePitch = 0.0f;
   public final SliderSetting maxDegreesPerTick = new SliderSetting(
      "Макс. скорость",
      12.0f,
      1.0f,
      30.0f,
      0.5f
   );
   public final SliderSetting range = new SliderSetting(
      "Дистанция",
      4.5f,
      1.0f,
      10.0f,
      0.5f
   );
   public final SliderSetting fov = new SliderSetting(
      "Угол обзора",
      90.0f,
      15.0f,
      180.0f,
      5.0f
   );
   public final SliderSetting humanization = new SliderSetting(
      "Шум (Гуманизация)",
      0.15000000596046448f,
      0.0f,
      1.0f,
      0.05000000074505806f
   );

   public AimAssistModule() {
      super("AimAssist", "Стабильная легитимная доводка без багов", Category.COMBAT);
      Setting[] var10001 = new Setting[4];
      var10001[0] = this.maxDegreesPerTick;
      var10001[1] = this.range;
      var10001[2] = this.fov;
      var10001[3] = this.humanization;
      this.addSettings(var10001);
   }

   public static void setTarget(float yaw, float pitch) {
      targetYaw = yaw;
      targetPitch = pitch;
      active = true;
   }

   public static void stop() {
      active = false;
   }

   public static boolean isActive() {
      return active;
   }

   @EventTarget
   public static void onUpdate(EventTick event) {
      ClientPlayerEntity player = mc.player;
      if (player != null && mc.world != null) {
         LivingEntity bestTarget = null;
         double maxDist = INSTANCE.range.getValue().doubleValue();
         double closestDist = maxDist * maxDist;
         float maxFov = INSTANCE.fov.getValue().floatValue();

         for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof LivingEntity) {
               LivingEntity living = (LivingEntity)entity;
               if (living != player
                  && !living.isDead()
                  && !(living.getHealth() <= 0.0f)
                  && (living instanceof MobEntity || living instanceof AnimalEntity || living instanceof PlayerEntity)) {
                  float targetYawAngle = getYawTo(player.getEyePos(), living.getPos());
                  float yawDiffToTarget = Math.abs(MathHelper.wrapDegrees(targetYawAngle - player.getYaw()));
                  if (!(yawDiffToTarget > maxFov / 2.0f)) {
                     double distSq = player.squaredDistanceTo(living);
                     if (distSq < closestDist) {
                        closestDist = distSq;
                        bestTarget = living;
                     }
                  }
               }
            }
         }

         if (bestTarget != null) {
            Vec3d eyesPos = player.getEyePos();
            double randomOffsetX = (RANDOM.nextDouble() - 0.5)
               * 0.1;
            double randomOffsetY = (RANDOM.nextDouble() - 0.5)
               * 0.2;
            double randomOffsetZ = (RANDOM.nextDouble() - 0.5)
               * 0.1;
            Vec3d targetPos = bestTarget.getPos()
               .add(
                  randomOffsetX,
                  (double)bestTarget.getHeight() * 0.5 + randomOffsetY,
                  randomOffsetZ
               );
            double diffX = targetPos.x - eyesPos.x;
            double diffY = targetPos.y - eyesPos.y;
            double diffZ = targetPos.z - eyesPos.z;
            double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
            float yaw = (float)(
               Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0
            );
            float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));
            setTarget(yaw, pitch);
         } else {
            stop();
         }

         if (!active) {
            lastPlayerYaw = player.getYaw();
            lastPlayerPitch = player.getPitch();
         } else {
            startYaw = player.getYaw();
            startPitch = player.getPitch();
            float maxStep = INSTANCE.maxDegreesPerTick.getValue().floatValue();
            float noiseFactor = INSTANCE.humanization.getValue().floatValue();
            lastNoiseYaw = lastNoiseYaw * 0.5f
               + (float)(RANDOM.nextGaussian() * 0.5) * noiseFactor;
            lastNoisePitch = lastNoisePitch * 0.5f
               + (float)(RANDOM.nextGaussian() * 0.5) * noiseFactor;
            float yawDiff = Math.abs(MathHelper.wrapDegrees(targetYaw - startYaw));
            float dynamicStep = maxStep
               * Math.min(
                  1.0f, yawDiff / 10.0f
               );
            dynamicStep = Math.max(1.0f, dynamicStep);
            float clampedYaw = clampStep(startYaw, targetYaw, dynamicStep) + lastNoiseYaw;
            float clampedPitch = clampStep(startPitch, targetPitch, dynamicStep) + lastNoisePitch;
            player.setYaw(startYaw);
            tickStepYaw = clampedYaw;
            tickStepPitch = clampedPitch;
            lastPlayerYaw = player.getYaw();
            lastPlayerPitch = player.getPitch();
         }
      } else {
         stop();
      }
   }
   public static float getYawTo(Vec3d from, Vec3d to) {
      double diffX = to.x - from.x;
      double diffZ = to.z - from.z;
      return (float)(Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0);
   }

   public static void onRenderFrame(float tickDelta) {
      if (active) {
         ClientPlayerEntity player = mc.player;
         if (player != null) {
            float yaw = MathHelper.lerpAngleDegrees(tickDelta, startYaw, tickStepYaw);
            float pitch = MathHelper.lerp(tickDelta, startPitch, tickStepPitch);
            pitch = MathHelper.clamp(
               pitch, -90.0f, 90.0f
            );
            player.setYaw(yaw);
            player.setPitch(pitch);
            player.headYaw = yaw;
            player.bodyYaw = yaw;
            lastPlayerYaw = yaw;
            lastPlayerPitch = pitch;
         }
      }
   }
   public static float clampStep(float from, float to, float maxStep) {
      float diff = MathHelper.wrapDegrees(to - from);
      diff = MathHelper.clamp(diff, -maxStep, maxStep);
      return from + diff;
   }

   @Override
   public void onDisable() {
      stop();
      super.onDisable();
   }
}
