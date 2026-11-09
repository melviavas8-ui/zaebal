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
























import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ObsidianComponent_145 extends CombatPredictionHelper implements IInstance {
   public LivingEntity trackedTarget;
   public float currentYaw;
   public float currentPitch;
   public float velocityYaw;
   public float velocityPitch;
   public double aimPointX;
   public double aimPointY;
   public double aimPointZ;
   public float noiseAngle;
   public final float noiseAmplitude = 1.2999999523162842f;
   public int hitPhase;
   public int hitTimer;
   public float pitchBeforeHit;
   public long firstSeenTime;
   public int reactionMs;
   public boolean reactionComplete;
   public float lastSentYaw;
   public float lastSentPitch;
   public float smoothYaw;
   public float smoothPitch;

   public void reset() {
      this.trackedTarget = null;
      this.velocityYaw = this.velocityPitch = 0.0f;
      this.aimPointX = this.aimPointY = this.aimPointZ = 0.0;
      this.noiseAngle = 0.0f;
      this.hitPhase = this.hitTimer = 0;
      this.firstSeenTime = 0L;
      this.reactionComplete = false;
      this.reactionMs = 0;
      if (mc.player != null) {
         this.currentYaw = mc.player.getYaw();
         this.currentPitch = mc.player.getPitch();
         this.lastSentYaw = this.currentYaw;
         this.lastSentPitch = this.currentPitch;
         this.smoothYaw = this.currentYaw;
         this.smoothPitch = this.currentPitch;
      } else {
         this.currentYaw = this.currentPitch = 0.0f;
         this.lastSentYaw = this.lastSentPitch = 0.0f;
         this.smoothYaw = this.smoothPitch = 0.0f;
      }
   }
   public float calcGcd() {
      double s = (Double)mc.options.getMouseSensitivity().getValue()
            * 0.6
         + 0.2;
      return (float)(s * s * s * 1.2);
   }
   public void pickAimPoint(LivingEntity e) {
      Box bb = e.getBoundingBox();
      double w = bb.maxX - bb.minX;
      double h = bb.maxY - bb.minY;
      double d = bb.maxZ - bb.minZ;
      this.aimPointX = (Math.random() - 0.5)
         * w
         * 0.17;
      this.aimPointY = (Math.random() - 0.3)
         * h
         * 0.05;
      this.aimPointZ = (Math.random() - 0.8)
         * d
         * 0.21;
   }

   public void onAttack() {
      this.hitPhase = 1;
      this.hitTimer = 0;
      this.pitchBeforeHit = this.currentPitch;
   }
   public float measureAngle(LivingEntity e) {
      if (mc.player == null) {
         return 0.0f;
      } else {
         Vec3d eyes = mc.player.getEyePos();
         Vec3d mid = e.getBoundingBox().getCenter();
         Vec3d delta = mid.subtract(eyes);
         float needYaw = (float)Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0f;
         float needPitch = (float)(-Math.toDegrees(Math.atan2(delta.y, delta.horizontalLength())));
         float dYaw = Math.abs(MathHelper.wrapDegrees(needYaw - mc.player.getYaw()));
         float dPitch = Math.abs(needPitch - mc.player.getPitch());
         return dYaw + dPitch;
      }
   }
   public int computeReaction(float angle) {
      if (angle > 130.0f) {
         return (140)
            + (int)(Math.random() * 90.0);
      } else if (angle > 70.0f) {
         return 90
            + (int)(Math.random() * 60.0);
      } else {
         return angle > 30.0f
            ? 45
               + (int)(Math.random() * 35.0)
            : 12
               + (int)(Math.random() * 20.0);
      }
   }
   public boolean isMovingForward() {
      return (mc.player == null ? false : mc.options.forwardKey.isPressed());
   }
   public boolean isOvertakingTarget(LivingEntity target) {
      if (mc.player != null && target != null) {
         Vec3d playerPos = mc.player.getPos();
         Vec3d targetPos = target.getPos();
         Vec3d playerVel = new Vec3d(mc.player.getX() - mc.player.prevX, mc.player.getY() - mc.player.prevY, mc.player.getZ() - mc.player.prevZ);
         Vec3d targetVel = new Vec3d(target.getX() - target.prevX, target.getY() - target.prevY, target.getZ() - target.prevZ);
         Vec3d toTarget = targetPos.subtract(playerPos).normalize();
         double playerSpeedToTarget = playerVel.dotProduct(toTarget);
         double targetSpeedToPlayer = targetVel.dotProduct(
            toTarget.multiply(Double.longBitsToDouble(-4616189618054758400L))
         );
         double relativeSpeed = playerSpeedToTarget + targetSpeedToPlayer;
         double distance = Math.sqrt(
            Math.pow(playerPos.x - targetPos.x, 2.0)
               + Math.pow(playerPos.z - targetPos.z, 2.0)
         );
         return (relativeSpeed > 0.05
               && distance < 4.0);
      } else {
         return false;
      }
   }
   public float[] generateNoise(float dist) {
      this.noiseAngle = this.noiseAngle
         + 0.03099999949336052f
         + (float)(Math.random() * 0.027000000700354576);
      float scale = MathHelper.clamp(
         dist / 4.5f,
         0.25f,
         1.0f
      );
      float amp = 1.2999999523162842f * scale;
      float drift = (float)Math.sin(
            (double)this.noiseAngle * 0.31
               + 1.17
         )
         * 0.5199999809265137f;
      float mid1 = (float)Math.sin(
            (double)this.noiseAngle * 1.07
               + 0.43
         )
         * 0.3100000023841858f;
      float mid2 = (float)Math.cos(
            (double)this.noiseAngle * 0.79
               + 2.11
         )
         * 0.25999999046325684f;
      float twitch = (float)Math.sin(
            (double)this.noiseAngle * 2.63
               + 0.88
         )
         * 0.1899999976158142f;
      if (twitch < 0.0f) {
         twitch *= 0.3499999940395355f;
      }

      float yawNoise = (drift + mid1 + twitch) * amp;
      float pitchNoise = (mid2 + drift * 0.3799999952316284f) * amp * 0.3700000047683716f;
      yawNoise += ((float)Math.random() - 0.3799999952316284f)
         * amp
         * 0.12999999523162842f;
      pitchNoise += ((float)Math.random() - 0.550000011920929f)
         * amp
         * 0.07000000029802322f;
      float[] var10000 = new float[2];
      var10000[0] = yawNoise;
      var10000[1] = pitchNoise;
      return var10000;
   }
   public float smoothStep(float x) {
      x = MathHelper.clamp(x, 0.0f, 1.0f);
      return x * x * (3.0f - 2.0f * x);
   }
   public float accelCurve(float x) {
      x = MathHelper.clamp(x, 0.0f, 1.0f);
      return 1.0f
         - (1.0f - x) * (1.0f - x);
   }
   public float springInterp(float current, float target, float vel, float stiffness, float damping) {
      float diff = target - current;
      float acc = diff * stiffness - vel * damping;
      return vel + acc;
   }
   public float smoothLerp(float from, float to, float alpha) {
      alpha = MathHelper.clamp(alpha, 0.0f, 1.0f);
      float delta = MathHelper.wrapDegrees(to - from);
      return from + delta * alpha;
   }
   public float calculateCurrentAngle(float targetYaw, float targetPitch) {
      float dYaw = Math.abs(MathHelper.wrapDegrees(targetYaw - this.currentYaw));
      float dPitch = Math.abs(targetPitch - this.currentPitch);
      return dYaw + dPitch;
   }

   @Override
   public void updateRotations(LivingEntity target) {
      if (mc.player != null && target != null) {
         boolean playerFlying = mc.player.isGliding();
         if (this.trackedTarget != target) {
            this.trackedTarget = target;
            this.currentYaw = mc.player.getYaw();
            this.currentPitch = mc.player.getPitch();
            this.lastSentYaw = this.currentYaw;
            this.lastSentPitch = this.currentPitch;
            this.smoothYaw = this.currentYaw;
            this.smoothPitch = this.currentPitch;
            this.velocityYaw = this.velocityPitch = 0.0f;
            this.pickAimPoint(target);
            this.hitPhase = this.hitTimer = 0;
            this.noiseAngle = (float)(Math.random() * Math.PI * 2.0);
            float angleDiff = this.measureAngle(target);
            this.reactionMs = this.computeReaction(angleDiff);
            this.firstSeenTime = System.currentTimeMillis();
            this.reactionComplete = false;
         }

         Vec3d eyePos = mc.player.getEyePos();
         Vec3d targetCenter = this.getPredictedPoint(target, target.getBoundingBox().getCenter());
         float distance = (float)eyePos.distanceTo(targetCenter);
         float gcd = this.calcGcd();
         if (!this.reactionComplete) {
            long elapsed = System.currentTimeMillis() - this.firstSeenTime;
            if (elapsed < (long)this.reactionMs) {
               float jitterY = ((float)Math.random() - 0.4300000071525574f)
                  * 0.3400000035762787f;
               float jitterP = ((float)Math.random() - 0.55F) * 0.17000000178813934f;
               float outY = this.lastSentYaw + jitterY;
               float outP = MathHelper.clamp(
                  this.lastSentPitch + jitterP,
                  -89.0f,
                  89.0f
               );
               outY -= (outY - this.lastSentYaw) % gcd;
               outP -= (outP - this.lastSentPitch) % gcd;
               this.lastSentYaw = outY;
               this.lastSentPitch = outP;
               RotationManager.update(
                  new ObsidianComponent_olorSetting(outY, outP),
                  360.0f,
                  45.0f,
                  45.0f,
                  45.0f,
                  0,
                  1,
                  false
               );
               return;
            }

            this.reactionComplete = true;
         }

         float[] noise = this.generateNoise(distance);
         if (this.hitPhase > 0) {
            this.hitTimer += 1;
            int upDuration = 25;
            int downDuration = 20;
            float targetPitchUp = -89.0f;
            if (this.hitPhase == (1)) {
               float t = (float)this.hitTimer / (float)upDuration;
               t = MathHelper.clamp(
                  t, 0.0f, 1.0f
               );
               float curved = this.accelCurve(t);
               this.currentPitch = MathHelper.lerp(curved, this.pitchBeforeHit, targetPitchUp);
               if (this.hitTimer >= upDuration) {
                  this.hitPhase = 2;
                  this.hitTimer = 0;
               }
            } else if (this.hitPhase == (2)) {
               float goal = this.pitchBeforeHit;
               float t = (float)this.hitTimer / (float)downDuration;
               t = MathHelper.clamp(t, 0.0f, 1.0f);
               float curved = this.smoothStep(t);
               this.currentPitch = MathHelper.lerp(curved, targetPitchUp, goal);
               if (this.hitTimer >= downDuration) {
                  this.hitPhase = 0;
                  this.hitTimer = 0;
               }
            }

            float outY = this.currentYaw + noise[0];
            float outP = MathHelper.clamp(
               this.currentPitch + noise[1],
               -89.0f,
               89.0f
            );
            outY -= (outY - this.lastSentYaw) % gcd;
            outP -= (outP - this.lastSentPitch) % gcd;
            this.lastSentYaw = outY;
            this.lastSentPitch = outP;
            RotationManager.update(
               new ObsidianComponent_olorSetting(outY, outP),
               360.0f,
               45.0f,
               45.0f,
               45.0f,
               0,
               1,
               false
            );
         } else {
            if (Math.random() < 0.015) {
               this.pickAimPoint(target);
            }

            Vec3d targetVel = new Vec3d(target.getX() - target.prevX, target.getY() - target.prevY, target.getZ() - target.prevZ);
            int predictTicks = this.shouldUseElytraPredict(target) ? 0 : 2;
            Vec3d predictedCenter = targetCenter.add(targetVel.multiply((double)predictTicks));
            Vec3d aimPos = predictedCenter.add(this.aimPointX, this.aimPointY, this.aimPointZ);
            Vec3d direction = aimPos.subtract(eyePos);
            float wantYaw = (float)MathHelper.wrapDegrees(
               Math.toDegrees(Math.atan2(direction.z, direction.x))
                  - 90.0
            );
            float wantPitch = (float)(-Math.toDegrees(Math.atan2(direction.y, direction.horizontalLength())));
            float diffYaw = MathHelper.wrapDegrees(wantYaw - this.currentYaw);
            float diffPitch = wantPitch - this.currentPitch;
            float speedMultiplier = 0.550000011920929f;
            if (playerFlying) {
               float currentAngle = this.calculateCurrentAngle(wantYaw, wantPitch);
               if (currentAngle > 120.0f) {
                  speedMultiplier = 0.15000000596046448f;
               } else if (currentAngle > 80.0f) {
                  float t = (currentAngle - 80.0f) / 40.0f;
                  speedMultiplier = MathHelper.lerp(
                     this.smoothStep(t), 0.2800000011920929f, 0.15000000596046448f
                  );
               } else if (currentAngle > 25.0f) {
                  float t = (currentAngle - 25.0f) / 55.0f;
                  speedMultiplier = MathHelper.lerp(
                     this.smoothStep(t),
                     0.5199999809265137f,
                     0.2800000011920929f
                  );
               } else {
                  speedMultiplier = 0.36F
                     + 0.2800000011920929f
                        * (
                           1.0f
                              - currentAngle / 25.0f
                        );
               }
            } else {
               boolean movingForward = this.isMovingForward();
               boolean overtaking = this.isOvertakingTarget(target);
               if (movingForward || overtaking) {
                  speedMultiplier = 0.2800000011920929f;
               }
            }

            float stiffness = (
                  0.02800000086426735f
                     + (float)Math.random() * 0.007000000216066837f
               )
               * speedMultiplier;
            float damping = 0.6700000166893005f
               + 0.13F * (1.0f - speedMultiplier);
            float totalDiff = (float)Math.sqrt((double)(diffYaw * diffYaw + diffPitch * diffPitch));
            if (totalDiff > 32.0f) {
               stiffness += 0.014999999664723873f * speedMultiplier;
            } else if (totalDiff < 4.199999809265137f) {
               stiffness *= 0.47999998927116394f;
            }

            stiffness += MathHelper.clamp(
                  (distance - 1.600000023841858f) / 7.5F,
                  0.0f,
                  0.04500000178813934f
               )
               * speedMultiplier;
            this.velocityYaw = this.springInterp(this.currentYaw, this.currentYaw + diffYaw, this.velocityYaw, stiffness, damping);
            this.velocityPitch = this.springInterp(
               this.currentPitch, wantPitch, this.velocityPitch, stiffness * 0.8700000047683716f, damping
            );
            float maxVelYaw = 7.5f * speedMultiplier;
            float maxVelPitch = 5.800000190734863f * speedMultiplier;
            this.velocityYaw = MathHelper.clamp(this.velocityYaw, -maxVelYaw, maxVelYaw);
            this.velocityPitch = MathHelper.clamp(this.velocityPitch, -maxVelPitch, maxVelPitch);
            this.currentYaw = this.currentYaw + this.velocityYaw;
            this.currentPitch = this.currentPitch + this.velocityPitch;
            this.currentPitch = MathHelper.clamp(
               this.currentPitch, -89.0f, 89.0f
            );
            float smoothFactor = playerFlying
               ? 0.30000001192092896f + speedMultiplier * 0.4000000059604645f
               : 0.8500000238418579f;
            this.smoothYaw = this.smoothLerp(this.smoothYaw, this.currentYaw, smoothFactor);
            this.smoothPitch = this.smoothLerp(
               this.smoothPitch, this.currentPitch, smoothFactor * 0.949999988079071f
            );
            float outY = this.smoothYaw + noise[0];
            float outP = this.smoothPitch + noise[1];
            outP = MathHelper.clamp(
               outP, -83.0f, 83.0f
            );
            outY -= (outY - this.lastSentYaw) % gcd;
            outP -= (outP - this.lastSentPitch) % gcd;
            this.lastSentYaw = outY;
            this.lastSentPitch = outP;
            RotationManager.update(
               new ObsidianComponent_olorSetting(outY, outP),
               360.0f,
               45.0f,
               45.0f,
               45.0f,
               0,
               1,
               false
            );
         }
      }
   }
}
