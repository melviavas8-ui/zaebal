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
























import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class ObsidianComponent_166 extends CombatPredictionHelper implements IInstance {
   public float smoothedSpeed = 0.0f;

   public void reset() {
      this.smoothedSpeed = 0.0f;
   }
   public boolean isYawOnTarget(LivingEntity target, float currentYaw) {
      Vec3d eyePos = mc.player.getEyePos();
      Box box = target.getBoundingBox();
      double minDiff = Double.MAX_VALUE;
      double maxDiff = Double.longBitsToDouble(-4503599627370497L);
      double[] var10000 = new double[2];
      var10000[0] = box.minX;
      var10000[1] = box.maxX;
      double[] xs = var10000;
      var10000 = new double[2];
      var10000[0] = box.minZ;
      var10000[1] = box.maxZ;
      double[] zs = var10000;
      double[] var11 = xs;
      int var12 = xs.length;

      for (int var13 = 0; var13 < var12; var13++) {
         double x = var11[var13];
         double[] var16 = zs;
         int var17 = zs.length;

         for (int var18 = 0; var18 < var17; var18++) {
            double z = var16[var18];
            double dx = x - eyePos.x;
            double dz = z - eyePos.z;
            float cornerYaw = (float)(
               MathHelper.atan2(dz, dx) * 57.29577951308232
                  - 90.0
            );
            float diff = MathHelper.wrapDegrees(cornerYaw - currentYaw);
            if ((double)diff < minDiff) {
               minDiff = (double)diff;
            }

            if ((double)diff > maxDiff) {
               maxDiff = (double)diff;
            }
         }
      }

      return (minDiff <= 0.0
            && maxDiff >= 0.0);
   }

   @Override
   public void updateRotations(LivingEntity target) {
      if (mc.player != null && target != null) {
         float currentYaw = mc.player.getYaw();
         boolean isOnTarget = this.isYawOnTarget(target, currentYaw);
         Vec3d targetPoint = ObsidianComponent_311.getMultipoint(target, 128.0);
         if (targetPoint == null) {
            targetPoint = target.getBoundingBox().getCenter();
         }

         Vec2f targetRot = ObsidianComponent_374.getRotations(this.getPredictedPoint(target, targetPoint));
         float yawDelta = MathHelper.wrapDegrees(targetRot.x - currentYaw);
         float absDelta = Math.abs(yawDelta);
         float targetSpeed = 0.0f;
         if (!isOnTarget) {
            float randomFactor = ThreadLocalRandom.current()
               .nextFloat(0.07000000029802322f, 0.11999999731779099f);
            float randomMaxSpeed = ThreadLocalRandom.current()
               .nextFloat(0.8999999761581421f, 1.5f);
            targetSpeed = MathHelper.clamp(absDelta * randomFactor, 0.009999999776482582f, randomMaxSpeed);
         }

         this.smoothedSpeed = MathHelper.lerp(0.11999999731779099f, this.smoothedSpeed, targetSpeed);
         if (this.smoothedSpeed < 0.0005000000237487257f) {
            this.smoothedSpeed = 0.0f;
         }

         float clampedDelta = MathHelper.clamp(yawDelta, -this.smoothedSpeed, this.smoothedSpeed);
         float targetPitch = mc.player.getPitch();
         this.rotate = new Vec2f(currentYaw + clampedDelta, targetPitch);
         RotationManager.update(
            new ObsidianComponent_olorSetting(targetRot.x, targetPitch),
            this.smoothedSpeed,
            0.0f,
            this.smoothedSpeed,
            0.0f,
            1,
            1,
            false
         );
      }
   }
}
