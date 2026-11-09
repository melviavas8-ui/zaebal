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
























import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class ObsidianComponent_396 {
   private static final MinecraftClient mc = MinecraftClient.getInstance();
   public static final float BASE_HORIZONTAL = 1.61F;
   public static final float BASE_VERTICAL = 1.5F;
   public static final float[] YAW_TABLE;
   public static final float[] PITCH_TABLE;

   public static Vec3d getBoost(LivingEntity entity) {
      float speed = getRageSpeed(entity);
      Vec3d vec3d = entity.getRotationVector();
      Vec3d oldVelocity = Vec3d.fromPolar(entity.getPitch(), entity.getYaw()).multiply((double)speed);
      float f = entity.getPitch() * 0.01745329238474369f;
      double d = Math.sqrt(vec3d.x * vec3d.x + vec3d.z * vec3d.z);
      double e = oldVelocity.horizontalLength();
      boolean bl = (entity.getVelocity().y <= 0.0);
      double g = bl && entity.hasStatusEffect(StatusEffects.SLOW_FALLING)
         ? Math.min(entity.getFinalGravity(), 0.01)
         : entity.getFinalGravity();
      double h = MathHelper.square(Math.cos((double)f));
      oldVelocity = oldVelocity.add(
         0.0,
         g
            * (
               Double.longBitsToDouble(-4616189618054758400L)
                  + h * 0.75
            ),
         0.0
      );
      if (oldVelocity.y < 0.0
         && d > 0.0) {
         double i = oldVelocity.y * Double.longBitsToDouble(-4631501856787818086L) * h;
         oldVelocity = oldVelocity.add(vec3d.x * i / d, i, vec3d.z * i / d);
      }

      if (f < 0.0f
         && d > 0.0) {
         double i = e * (double)(-MathHelper.sin(f)) * 0.04;
         oldVelocity = oldVelocity.add(
            -vec3d.x * i / d, i * 3.2, -vec3d.z * i / d
         );
      }

      if (d > 0.0) {
         oldVelocity = oldVelocity.add(
            (vec3d.x / d * e - oldVelocity.x) * 0.1,
            0.0,
            (vec3d.z / d * e - oldVelocity.z) * 0.1
         );
      }

      double length = oldVelocity.length();
      return new Vec3d(length, length, length)
         .multiply(
            0.99,
            0.98,
            0.99
         );
   }
   public static float getRageSpeed(LivingEntity entity) {
      float yawAbs = Math.abs(MathHelper.wrapDegrees(entity.getYaw()));
      float yawFolded = foldYaw(yawAbs);
      float pitchAbs = Math.abs(clampPitch(entity.getPitch()));
      if (pitchAbs >= 70.0f && pitchAbs <= 90.0f) {
         return 1.6150000095367432f;
      } else {
         float yawSpeed = YAW_TABLE[Math.min((int)Math.ceil((double)yawFolded), 90)];
         int pitchIndex = Math.min((int)Math.ceil((double)pitchAbs), PITCH_TABLE.length - 1);
         float pitchSpeed = PITCH_TABLE[pitchIndex];
         float speed = pitchAbs >= 75.0f ? pitchSpeed : Math.max(yawSpeed, pitchSpeed);
         return Math.max(
            speed,
            pitchAbs >= 75.0f
               ? 1.5f
               : 1.6100000143051147f
         );
      }
   }
   public static float foldYaw(float yawAbs) {
      float folded180 = yawAbs > 180.0f
         ? 360.0f - yawAbs
         : yawAbs;
      return folded180 > 90.0f
         ? 180.0f - folded180
         : folded180;
   }
   public static float clampPitch(float pitch) {
      return Math.max(-90.0f, Math.min(90.0f, pitch));
   }

   public static Vec3d getBoostAntiTarget(LivingEntity entity, float speedSetting) {
      float yaw = Math.abs(
         (entity.getYaw() - 360.0f) % 360.0f
      );
      float pitch = entity.getPitch();
      float absPitch = Math.abs(pitch);
      float pitchBonus = 0.0f;
      if (absPitch >= 30.0f && absPitch <= 50.0f) {
         pitchBonus = 0.15000000596046448f;
      } else if (absPitch >= 25.0f
         && absPitch <= 55.0f) {
         pitchBonus = 0.10000000149011612f;
      } else if (absPitch >= 20.0f && absPitch <= 60.0f) {
         pitchBonus = 0.05000000074505806f;
      }

      float speed = speedSetting + pitchBonus;
      float[] var10000 = new float[4];
      var10000[0] = 45.0f;
      var10000[1] = 135.0f;
      var10000[2] = 225.0f;
      var10000[3] = 315.0f;
      float[] centers = var10000;
      float minDiff = 9999.0f;
      float[] var10 = centers;
      int var11 = centers.length;

      for (int var12 = 0; var12 < var11; var12++) {
         float c = var10[var12];
         float diff = Math.abs(yaw - c);
         if (diff < minDiff) {
            minDiff = diff;
         }
      }

      if (minDiff < 15.0f) {
         speed += 0.10000000149011612f;
      } else if (minDiff < 25.0f) {
         speed += 0.05000000074505806f;
      }

      speed = Math.min(speed, 2.799999952316284f);
      return new Vec3d((double)speed, (double)speed, (double)speed);
   }

   public static Vec3d getBoostAntiTargetFast(LivingEntity entity) {
      float yaw = Math.abs(
         (entity.getYaw() - 360.0f)
            % 360.0f
      );
      float pitch = entity.getPitch();
      float absPitch = Math.abs(pitch);
      float speedXZ = 2.5f;
      float speedY = 2.299999952316284f;
      if (absPitch >= 35.0f
         && absPitch <= 50.0f) {
         speedXZ = 2.700000047683716f;
         speedY = 2.5f;
      } else if (absPitch >= 30.0f && absPitch <= 55.0f) {
         speedXZ = 2.5999999046325684f;
         speedY = 2.4000000953674316f;
      }

      float[] var10000 = new float[4];
      var10000[0] = 45.0f;
      var10000[1] = 135.0f;
      var10000[2] = 225.0f;
      var10000[3] = 315.0f;
      float[] centers = var10000;
      float minDiff = 9999.0f;
      float[] var8 = centers;
      int var9 = centers.length;

      for (int var10 = 0; var10 < var9; var10++) {
         float c = var8[var10];
         float diff = Math.abs(yaw - c);
         if (diff < minDiff) {
            minDiff = diff;
         }
      }

      if (minDiff < 20.0f) {
         speedXZ += 0.15000000596046448f;
      }

      return new Vec3d((double)speedXZ, (double)speedY, (double)speedXZ);
   }

   public static Vec3d getBoostAntiTargetWithAura(LivingEntity entity, float auraRotatePitch, float auraRotateYaw, float speedSetting) {
      float absPitch = Math.abs(auraRotatePitch);
      float speedXZ = speedSetting;
      float speedY;
      if (absPitch >= 38.0f
         && absPitch <= 52.0f) {
         speedXZ = Math.min(speedSetting + 0.20000000298023224f, 2.700000047683716f);
         speedY = Math.min(
            speedSetting + 0.15000000596046448f, 2.5f
         );
      } else if (absPitch >= 30.0f && absPitch <= 60.0f
         )
       {
         speedXZ = Math.min(
            speedSetting + 0.10000000149011612f, 2.5999999046325684f
         );
         speedY = Math.min(
            speedSetting + 0.10000000149011612f,
            2.4000000953674316f
         );
      } else if (absPitch >= 25.0f
         && absPitch <= 65.0f) {
         speedY = speedSetting - 0.05000000074505806f;
      } else {
         speedXZ = speedSetting - 0.10000000149011612f;
         speedY = speedSetting - 0.15000000596046448f;
      }

      return new Vec3d((double)speedXZ, (double)speedY, (double)speedXZ);
   }

   public static Vec3d getBoostslime(LivingEntity entity) {
      return getBoostCustom(entity, 42.0f);
   }

   public static Vec3d getBoostbravo(LivingEntity entity) {
      return getBoostCustom(entity, 39.0f);
   }

   public static Vec3d getBoostrw(LivingEntity entity) {
      return getBoostCustom(entity, 33.20000076293945f);
   }

   public static Vec3d getBoostCustom(LivingEntity entity, float targetBps) {
      float maxSpeed = targetBps / 20.0f;
      float yaw = Math.abs((entity.getYaw() - 360.0f) % 360.0f);
      float pitch = entity.getPitch();
      float minSpeed = Math.min(
         maxSpeed * 0.699999988079071f, 1.6699999570846558f
      );
      float[] var10000 = new float[4];
      var10000[0] = 45.0f;
      var10000[1] = 135.0f;
      var10000[2] = 225.0f;
      var10000[3] = 315.0f;
      float[] centers = var10000;
      float minDiff = 9999.0f;
      float[] yawFactor = centers;
      int pitchFactor = centers.length;

      for (int combinedFactor = 0; combinedFactor < pitchFactor; combinedFactor++) {
         float c = yawFactor[combinedFactor];
         float diff = Math.abs(yaw - c);
         if (diff < minDiff) {
            minDiff = diff;
         }
      }

      float yawFactorx = 1.0f - minDiff / 45.0f;
      float var29 = Math.max(
         0.0f, Math.min(1.0f, yawFactorx)
      );
      float pitchFactorx = getPitchFactor(pitch);
      float combinedFactorx = var29 * pitchFactorx;
      float speed = minSpeed + (maxSpeed - minSpeed) * combinedFactorx;
      Vec3d vec3d = entity.getRotationVector();
      Vec3d oldVelocity = Vec3d.fromPolar(pitch, entity.getYaw()).multiply((double)speed);
      float f = pitch * 0.01745329238474369f;
      double d = Math.sqrt(vec3d.x * vec3d.x + vec3d.z * vec3d.z);
      double e = oldVelocity.horizontalLength();
      boolean bl = (entity.getVelocity().y <= 0.0);
      double g = bl && entity.hasStatusEffect(StatusEffects.SLOW_FALLING)
         ? Math.min(entity.getFinalGravity(), 0.01)
         : entity.getFinalGravity();
      double h = MathHelper.square(Math.cos((double)f));
      oldVelocity = oldVelocity.add(
         0.0,
         g
            * (
               Double.longBitsToDouble(-4616189618054758400L)
                  + h * 0.75
            ),
         0.0
      );
      if (oldVelocity.y < 0.0
         && d > 0.0) {
         double i = oldVelocity.y * Double.longBitsToDouble(-4631501856787818086L) * h;
         oldVelocity = oldVelocity.add(vec3d.x * i / d, i, vec3d.z * i / d);
      }

      if (f < 0.0f
         && d > 0.0) {
         double i = e * (double)(-MathHelper.sin(f)) * 0.04;
         oldVelocity = oldVelocity.add(
            -vec3d.x * i / d, i * 3.2, -vec3d.z * i / d
         );
      }

      if (d > 0.0) {
         oldVelocity = oldVelocity.add(
            (vec3d.x / d * e - oldVelocity.x) * 0.1,
            0.0,
            (vec3d.z / d * e - oldVelocity.z) * 0.1
         );
      }

      double length = oldVelocity.length();
      return new Vec3d(length, length, length)
         .multiply(
            0.99,
            0.98,
            0.99
         );
   }

   public static Vec3d getBoostFixedBps(LivingEntity entity, float targetBps) {
      float speed = targetBps / 20.0f;
      return new Vec3d((double)speed, (double)speed, (double)speed)
         .multiply(
            0.99,
            0.98,
            0.99
         );
   }
   public static float getPitchFactor(float pitch) {
      float absPitch = Math.abs(pitch);
      if (absPitch <= 5.0f) {
         return 1.0f;
      } else if (absPitch <= 15.0f) {
         return 0.949999988079071f;
      } else if (absPitch <= 25.0f) {
         return 0.8500000238418579f;
      } else if (absPitch <= 35.0f) {
         return 0.75f;
      } else if (absPitch <= 45.0f) {
         return 0.6499999761581421f;
      } else if (absPitch <= 55.0f) {
         return 0.550000011920929f;
      } else if (absPitch <= 65.0f) {
         return 0.44999998807907104f;
      } else {
         return absPitch <= 75.0f
            ? 0.3499999940395355f
            : 0.25f;
      }
   }

   public ObsidianComponent_396() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   static {
      float[] var10000 = new float[91];
      var10000[0] = 1.6100000143051147f;
      var10000[1] = 1.6100000143051147f;
      var10000[2] = 1.6100000143051147f;
      var10000[3] = 1.6100000143051147f;
      var10000[4] = 1.6100000143051147f;
      var10000[5] = 1.6100000143051147f;
      var10000[6] = 1.6200000047683716f;
      var10000[7] = 1.6200000047683716f;
      var10000[8] = 1.6200000047683716f;
      var10000[9] = 1.6299999952316284f;
      var10000[10] = 1.6299999952316284f;
      var10000[11] = 1.6399999856948853f;
      var10000[12] = 1.649999976158142f;
      var10000[13] = 1.649999976158142f;
      var10000[14] = 1.659999966621399f;
      var10000[15] = 1.6699999570846558f;
      var10000[16] = 1.6799999475479126f;
      var10000[17] = 1.690000057220459f;
      var10000[18] = 1.7F;
      var10000[19] = 1.7100000381469727f;
      var10000[20] = 1.7200000286102295f;
      var10000[21] = 1.7300000190734863f;
      var10000[22] = 1.7300000190734863f;
      var10000[23] = 1.75f;
      var10000[24] = 1.7599999904632568f;
      var10000[25] = 1.7799999713897705f;
      var10000[26] = 1.7899999618530273f;
      var10000[27] = 1.809999942779541f;
      var10000[28] = 1.8300000429153442f;
      var10000[29] = 1.850000023841858f;
      var10000[30] = 1.8700000047683716f;
      var10000[31] = 1.8899999856948853f;
      var10000[32] = 1.909999966621399f;
      var10000[33] = 1.9299999475479126f;
      var10000[34] = 1.9500000476837158f;
      var10000[35] = 1.9800000190734863f;
      var10000[36] = 2.009999990463257f;
      var10000[37] = 2.0299999713897705f;
      var10000[38] = 2.059999942779541f;
      var10000[39] = 2.0899999141693115f;
      var10000[40] = 2.119999885559082f;
      var10000[41] = 2.1600000858306885f;
      var10000[42] = 2.190000057220459f;
      var10000[43] = 2.2300000190734863f;
      var10000[44] = 2.2699999809265137f;
      var10000[45] = 2.309999942779541f;
      var10000[46] = 2.3499999046325684f;
      var10000[47] = 2.309999942779541f;
      var10000[48] = 2.2699999809265137f;
      var10000[49] = 2.2300000190734863f;
      var10000[50] = 2.190000057220459f;
      var10000[51] = 2.1600000858306885f;
      var10000[52] = 2.119999885559082f;
      var10000[53] = 2.0899999141693115f;
      var10000[54] = 2.059999942779541f;
      var10000[55] = 2.0299999713897705f;
      var10000[56] = 2.009999990463257f;
      var10000[57] = 1.9800000190734863f;
      var10000[58] = 1.9500000476837158f;
      var10000[59] = 1.9299999475479126f;
      var10000[60] = 1.8899999856948853f;
      var10000[61] = 1.8700000047683716f;
      var10000[62] = 1.85F;
      var10000[63] = 1.8300000429153442f;
      var10000[64] = 1.809999942779541f;
      var10000[65] = 1.7899999618530273f;
      var10000[66] = 1.7799999713897705f;
      var10000[67] = 1.7599999904632568f;
      var10000[68] = 1.75f;
      var10000[69] = 1.7300000190734863f;
      var10000[70] = 1.7200000286102295f;
      var10000[71] = 1.7100000381469727f;
      var10000[72] = 1.7000000476837158f;
      var10000[73] = 1.690000057220459f;
      var10000[74] = 1.6799999475479126f;
      var10000[75] = 1.6699999570846558f;
      var10000[76] = 1.659999966621399f;
      var10000[77] = 1.649999976158142f;
      var10000[78] = 1.6399999856948853f;
      var10000[79] = 1.6299999952316284f;
      var10000[80] = 1.6299999952316284f;
      var10000[81] = 1.6299999952316284f;
      var10000[82] = 1.6200000047683716f;
      var10000[83] = 1.6200000047683716f;
      var10000[84] = 1.6200000047683716f;
      var10000[85] = 1.6100000143051147f;
      var10000[86] = 1.6100000143051147f;
      var10000[87] = 1.6100000143051147f;
      var10000[88] = 1.6100000143051147f;
      var10000[89] = 1.6100000143051147f;
      var10000[90] = 1.6100000143051147f;
      YAW_TABLE = var10000;
      var10000 = new float[91];
      var10000[0] = 1.61F;
      var10000[1] = 1.6100000143051147f;
      var10000[2] = 1.6100000143051147f;
      var10000[3] = 1.62F;
      var10000[4] = 1.62F;
      var10000[5] = 1.62F;
      var10000[6] = 1.6299999952316284f;
      var10000[7] = 1.6299999952316284f;
      var10000[8] = 1.6399999856948853f;
      var10000[9] = 1.649999976158142f;
      var10000[10] = 1.649999976158142f;
      var10000[11] = 1.659999966621399f;
      var10000[12] = 1.6699999570846558f;
      var10000[13] = 1.6799999475479126f;
      var10000[14] = 1.690000057220459f;
      var10000[15] = 1.7000000476837158f;
      var10000[16] = 1.7100000381469727f;
      var10000[17] = 1.7200000286102295f;
      var10000[18] = 1.7300000190734863f;
      var10000[19] = 1.7300000190734863f;
      var10000[20] = 1.75f;
      var10000[21] = 1.7599999904632568f;
      var10000[22] = 1.7799999713897705f;
      var10000[23] = 1.7899999618530273f;
      var10000[24] = 1.809999942779541f;
      var10000[25] = 1.8300000429153442f;
      var10000[26] = 1.850000023841858f;
      var10000[27] = 1.8700000047683716f;
      var10000[28] = 1.8899999856948853f;
      var10000[29] = 1.909999966621399f;
      var10000[30] = 1.9299999475479126f;
      var10000[31] = 1.9500000476837158f;
      var10000[32] = 1.9800000190734863f;
      var10000[33] = 2.009999990463257f;
      var10000[34] = 2.0299999713897705f;
      var10000[35] = 2.059999942779541f;
      var10000[36] = 2.0899999141693115f;
      var10000[37] = 2.119999885559082f;
      var10000[38] = 2.1600000858306885f;
      var10000[39] = 2.190000057220459f;
      var10000[40] = 2.2300000190734863f;
      var10000[41] = 2.240000009536743f;
      var10000[42] = 2.2100000381469727f;
      var10000[43] = 2.2100000381469727f;
      var10000[44] = 2.2100000381469727f;
      var10000[45] = 2.23F;
      var10000[46] = 2.2300000190734863f;
      var10000[47] = 2.190000057220459f;
      var10000[48] = 2.1600000858306885f;
      var10000[49] = 2.119999885559082f;
      var10000[50] = 2.0899999141693115f;
      var10000[51] = 2.059999942779541f;
      var10000[52] = 2.0299999713897705f;
      var10000[53] = 2.009999990463257f;
      var10000[54] = 1.9800000190734863f;
      var10000[55] = 1.9500000476837158f;
      var10000[56] = 1.9299999475479126f;
      var10000[57] = 1.8899999856948853f;
      var10000[58] = 1.8700000047683716f;
      var10000[59] = 1.850000023841858f;
      var10000[60] = 1.8300000429153442f;
      var10000[61] = 1.809999942779541f;
      var10000[62] = 1.7899999618530273f;
      var10000[63] = 1.7799999713897705f;
      var10000[64] = 1.7599999904632568f;
      var10000[65] = 1.75f;
      var10000[66] = 1.7300000190734863f;
      var10000[67] = 1.7200000286102295f;
      var10000[68] = 1.7100000381469727f;
      var10000[69] = 1.7000000476837158f;
      var10000[70] = 1.690000057220459f;
      var10000[71] = 1.6799999475479126f;
      var10000[72] = 1.6699999570846558f;
      var10000[73] = 1.659999966621399f;
      var10000[74] = 1.649999976158142f;
      var10000[75] = 1.6399999856948853f;
      var10000[76] = 1.6299999952316284f;
      var10000[77] = 1.6299999952316284f;
      var10000[78] = 1.63F;
      var10000[79] = 1.6200000047683716f;
      var10000[80] = 1.6200000047683716f;
      var10000[81] = 1.6200000047683716f;
      var10000[82] = 1.6100000143051147f;
      var10000[83] = 1.6100000143051147f;
      var10000[84] = 1.6100000143051147f;
      var10000[85] = 1.6100000143051147f;
      var10000[86] = 1.6100000143051147f;
      var10000[87] = 1.6100000143051147f;
      var10000[88] = 1.6100000143051147f;
      var10000[89] = 1.6100000143051147f;
      var10000[90] = 1.6100000143051147f;
      PITCH_TABLE = var10000;
   }
}
