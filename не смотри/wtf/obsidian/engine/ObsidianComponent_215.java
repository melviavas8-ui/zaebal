package wtf.obsidian.engine;

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
























import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ObsidianComponent_215 implements IInstance {
   private static final Map<UUID, ObsidianComponent_158> positionCache = new ConcurrentHashMap<>();

   public static void updateEntity(LivingEntity entity) {
      ObsidianComponent_158 data = positionCache.computeIfAbsent(entity.getUuid(), k -> new ObsidianComponent_158());
      data.update(entity.getX(), entity.getY(), entity.getZ());
   }

   public static ObsidianComponent_158 getData(LivingEntity entity) {
      return positionCache.get(entity.getUuid());
   }

   public static Vec3d predict(LivingEntity entity, int ticks, float extraForward, boolean isMeFlying) {
      ObsidianComponent_158 data = getData(entity);
      Vec3d pos = new Vec3d(
         entity.getX(), entity.getY() + (double)(entity.getStandingEyeHeight() / 2.0f), entity.getZ()
      );
      if (data == null) {
         return predictElytraPhysics(entity, pos, ticks);
      } else {
         Vec3d forward = data.getResolvedForward();
         double speed = data.getLastSpeed();
         boolean isHighSpeed = data.isSpeedChanged();
         if (entity.isGliding()) {
            double horizontalSpeed = Math.hypot(forward.x, forward.z)
               * 20.0;
            double verticalSpeed = Math.abs(forward.y) * 20.0;
            if (horizontalSpeed <= 5.0
               && verticalSpeed <= 5.0) {
               return pos;
            } else {
               boolean shouldPredict = (isMeFlying && entity.isGliding() && isHighSpeed);
               float predictMultiplier = shouldPredict ? (float)(ticks + 2) + extraForward : (float)ticks;
               Vec3d linearPredict = pos.add(forward.multiply((double)predictMultiplier, (double)predictMultiplier, (double)predictMultiplier));
               Vec3d physicsPredict = predictElytraPhysics(entity, pos, ticks);
               double weight = MathHelper.clamp(
                  speed / 50.0,
                  0.3,
                  0.9
               );
               return new Vec3d(
                  MathHelper.lerp(weight, physicsPredict.x, linearPredict.x),
                  MathHelper.lerp(weight, physicsPredict.y, linearPredict.y),
                  MathHelper.lerp(weight, physicsPredict.z, linearPredict.z)
               );
            }
         } else {
            return speed > 1.0
               ? pos.add(forward.multiply((double)ticks, (double)ticks, (double)ticks))
               : pos;
         }
      }
   }

   public static Vec3d predict(LivingEntity entity, Vec3d pos, int ticks) {
      ObsidianComponent_158 data = getData(entity);
      if (data != null && entity.isGliding()) {
         Vec3d forward = data.getResolvedForward();
         double horizontalSpeed = Math.hypot(forward.x, forward.z)
            * 20.0;
         double verticalSpeed = Math.abs(forward.y) * 20.0;
         return horizontalSpeed <= 5.0
               && verticalSpeed <= 5.0
            ? pos
            : pos.add(forward.multiply((double)ticks, (double)ticks, (double)ticks));
      } else {
         return predictElytraPhysics(entity, pos, ticks);
      }
   }

   public static Vec3d predictElytraPhysics(LivingEntity entity, Vec3d pos, int ticks) {
      Vec3d velocity = entity.getVelocity();
      if (!entity.isGliding()) {
         return pos.add(velocity.multiply((double)ticks, (double)ticks, (double)ticks));
      } else {
         double horizontalDelta = Math.hypot(entity.prevX - entity.getX(), entity.prevZ - entity.getZ())
            * 20.0;
         double verticalDelta = Math.abs(entity.getY() - entity.prevY)
            * 20.0;
         if (horizontalDelta <= 5.0
            && verticalDelta <= 5.0) {
            return pos;
         } else {
            for (int i = 0; i < ticks; i++) {
               Vec3d rotation = entity.getRotationVector();
               float pitchRad = (float)Math.toRadians((double)entity.getPitch());
               double horizontalSpeed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
               double velocityLength = velocity.length();
               float cos = MathHelper.cos(pitchRad);
               cos = (float)(
                  (double)(cos * cos)
                     * Math.min(
                        1.0,
                        rotation.length() / 0.4
                     )
               );
               velocity = velocity.add(
                  0.0,
                  Double.longBitsToDouble(-4632943008668576645L)
                     * (
                        Double.longBitsToDouble(-4616189618054758400L)
                           + (double)cos * 0.75
                     ),
                  0.0
               );
               if (velocity.y < 0.0
                  && horizontalSpeed > 0.0) {
                  double d5 = velocity.y * Double.longBitsToDouble(-4631501856787818086L) * (double)cos;
                  velocity = velocity.add(rotation.x * d5 / horizontalSpeed, d5, rotation.z * d5 / horizontalSpeed);
               }

               if (pitchRad < 0.0f
                  && horizontalSpeed > 0.0) {
                  double lift = velocityLength
                     * (double)(-MathHelper.sin(pitchRad))
                     * 0.04;
                  velocity = velocity.add(
                     -rotation.x * lift / horizontalSpeed,
                     lift * 3.2,
                     -rotation.z * lift / horizontalSpeed
                  );
               }

               if (horizontalSpeed > 0.0) {
                  velocity = velocity.add(
                     (rotation.x / horizontalSpeed * velocityLength - velocity.x)
                        * 0.1,
                     0.0,
                     (rotation.z / horizontalSpeed * velocityLength - velocity.z)
                        * 0.1
                  );
               }

               velocity = velocity.multiply(
                  0.99,
                  0.98,
                  0.99
               );
               pos = pos.add(velocity);
            }

            return pos;
         }
      }
   }

   public static Vec3d bypasselytrahacking(LivingEntity target) {
      Vec3d interpolatedRotation = Vec3d.fromPolar(target.getLerpTargetPitch(), target.getLerpTargetYaw());
      Vec3d rotationVector = target.getRotationVector();
      Vec3d relativePos = target.getPos()
         .add(
            0.0,
            (double)(target.getHeight() * 0.6F),
            0.0
         )
         .subtract(mc.player.getEyePos());
      Vec3d blendedDirection = interpolatedRotation.normalize().lerp(rotationVector, interpolatedRotation.length());
      return relativePos.add(blendedDirection.normalize().multiply((double)ModuleManager.elytraTarget.forward.getValue().floatValue()));
   }

   public static void cleanup() {
      long now = System.currentTimeMillis();
      positionCache.entrySet()
         .removeIf(
            e -> (now - e.getValue().getLastUpdate() > (10000L))
         );
   }

   public static void clear() {
      positionCache.clear();
   }
}
