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
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class ObsidianComponent_318 extends CombatPredictionHelper implements IInstance {
   public LivingEntity currentTarget;
   public float currentFov = 45.0f;
   public static float rawDeltaYaw = 0.0f;
   public static float rawDeltaPitch = 0.0f;
   public float virtualYaw = 0.0f;
   public float virtualPitch = 0.0f;
   public float targetYaw = 0.0f;
   public float targetPitch = 0.0f;
   public float currentYaw = 0.0f;
   public float currentPitch = 0.0f;
   public float smoothFactor = 0.6000000238418579f;
   public int smoothUpdateTimer = 0;
   private boolean isApplyingNoise = false;
   public int noiseCooldownTimer = 0;
   public int noiseActiveTimer = 0;
   public float currentNoiseYaw = 0.0f;
   public float currentNoisePitch = 0.0f;
   public float targetNoiseYaw = 0.0f;
   public float targetNoisePitch = 0.0f;

   public float getCurrentFov() {
      return this.currentFov;
   }

   public void reset() {
      this.currentTarget = null;
      this.isApplyingNoise = false;
      this.noiseCooldownTimer = (int)MathLerpUtil.random(
         20.0f, 80.0f
      );
      this.noiseActiveTimer = 0;
      this.currentNoiseYaw = 0.0f;
      this.currentNoisePitch = 0.0f;
      this.targetNoiseYaw = 0.0f;
      this.targetNoisePitch = 0.0f;
      this.smoothFactor = MathLerpUtil.random(0.550000011920929f, 0.699999988079071f);
      this.smoothUpdateTimer = (int)MathLerpUtil.random(40.0f, 80.0f);
      rawDeltaYaw = 0.0f;
      rawDeltaPitch = 0.0f;
      if (mc.player != null) {
         this.virtualYaw = mc.player.getYaw();
         this.virtualPitch = mc.player.getPitch();
         this.targetYaw = this.virtualYaw;
         this.targetPitch = this.virtualPitch;
         this.currentYaw = this.virtualYaw;
         this.currentPitch = this.virtualPitch;
      }
   }

   public boolean isInFov(LivingEntity target) {
      if (mc.player != null && target != null && mc.gameRenderer != null && mc.gameRenderer.getCamera() != null) {
         Vec3d targetCenter = target.getBoundingBox().getCenter();
         Vec2f rotToTarget = ObsidianComponent_374.getRotations(targetCenter);
         float cameraYaw = mc.gameRenderer.getCamera().getYaw();
         float cameraPitch = mc.gameRenderer.getCamera().getPitch();
         float yawDiff = Math.abs(MathHelper.wrapDegrees(rotToTarget.x - cameraYaw));
         float pitchDiff = Math.abs(rotToTarget.y - cameraPitch);
         return (Math.hypot((double)yawDiff, (double)pitchDiff) <= (double)this.currentFov);
      } else {
         return false;
      }
   }

   @Override
   public void updateRotations(LivingEntity target) {
      if (mc.player != null && target != null) {
         if (this.currentTarget != target) {
            this.currentTarget = target;
            this.reset();
         }

         float deltaYaw = rawDeltaYaw;
         float deltaPitch = rawDeltaPitch;
         rawDeltaYaw = 0.0f;
         rawDeltaPitch = 0.0f;
         this.virtualYaw += deltaYaw;
         this.virtualPitch += deltaPitch;
         this.virtualPitch = MathHelper.clamp(
            this.virtualPitch, -89.0f, 89.0f
         );
         Vec3d eyePos = mc.player.getEyePos();
         Box box = this.getPredictedBox(target);
         Vec3d center = box.getCenter();
         Vec2f centerRot = ObsidianComponent_374.getRotations(center);
         float distance = Math.max(0.10000000149011612f, (float)eyePos.distanceTo(center));
         float halfWidth = (float)(box.maxX - box.minX) / 2.0f;
         float halfHeight = (float)(box.maxY - box.minY) / 2.0f;
         float yawRatio = Math.min(
            1.0f, halfWidth * 0.75f / distance
         );
         float pitchRatio = Math.min(
            1.0f, halfHeight * 0.75f / distance
         );
         float yawSpan = (float)Math.toDegrees(Math.asin((double)yawRatio));
         float pitchSpan = (float)Math.toDegrees(Math.asin((double)pitchRatio));
         float yawDiffFromCenter = MathHelper.wrapDegrees(this.virtualYaw - centerRot.x);
         float pitchDiffFromCenter = this.virtualPitch - centerRot.y;
         this.targetYaw = centerRot.x + MathHelper.clamp(yawDiffFromCenter, -yawSpan, yawSpan);
         this.targetPitch = centerRot.y + MathHelper.clamp(pitchDiffFromCenter, -pitchSpan, pitchSpan);
         this.smoothUpdateTimer -= 1;
         if (this.smoothUpdateTimer <= 0) {
            this.smoothUpdateTimer = (int)MathLerpUtil.random(
               30.0f, 70.0f
            );
            this.smoothFactor = MathLerpUtil.random(
               0.5f, 0.7200000286102295f
            );
         }

         float mouseSpeed = (float)Math.hypot((double)deltaYaw, (double)deltaPitch);
         float adaptiveFactor;
         if (mouseSpeed > 5.0f) {
            adaptiveFactor = MathLerpUtil.random(0.8F, 0.949999988079071f);
         } else if (mouseSpeed > 1.5f) {
            adaptiveFactor = this.smoothFactor + mouseSpeed * 0.029999999329447746f;
         } else {
            adaptiveFactor = this.smoothFactor;
         }

         adaptiveFactor = Math.min(adaptiveFactor, 0.949999988079071f);
         float yawDelta = MathHelper.wrapDegrees(this.targetYaw - this.currentYaw);
         float pitchDelta = this.targetPitch - this.currentPitch;
         this.currentYaw += yawDelta * adaptiveFactor;
         this.currentPitch += pitchDelta * adaptiveFactor;
         if (mouseSpeed < 2.5f) {
            if (!this.isApplyingNoise) {
               this.noiseCooldownTimer -= 1;
               if (this.noiseCooldownTimer <= 0) {
                  this.isApplyingNoise = true;
                  this.noiseActiveTimer = (int)MathLerpUtil.random(
                     35.0f, 45.0f
                  );
               }
            } else {
               this.noiseActiveTimer -= 1;
               if ((float)this.noiseActiveTimer
                     % MathLerpUtil.random(4.0f, 7.0f)
                  == 0.0f) {
                  this.targetNoiseYaw = (float)(
                     (Math.random() - 0.5)
                        * 1.2
                  );
                  this.targetNoisePitch = (float)(
                     (Math.random() - 0.5)
                        * 0.8
                  );
               }

               if (this.noiseActiveTimer <= 0) {
                  this.isApplyingNoise = false;
                  this.noiseCooldownTimer = (int)MathLerpUtil.random(20.0F, 80.0f);
                  this.targetNoiseYaw = 0.0f;
                  this.targetNoisePitch = 0.0f;
               }
            }
         } else {
            this.targetNoiseYaw = 0.0f;
            this.targetNoisePitch = 0.0f;
         }

         this.currentNoiseYaw = MathHelper.lerp(0.11999999731779099f, this.currentNoiseYaw, this.targetNoiseYaw);
         this.currentNoisePitch = MathHelper.lerp(0.11999999731779099f, this.currentNoisePitch, this.targetNoisePitch);
         float newYaw = this.currentYaw + this.currentNoiseYaw;
         float newPitch = this.currentPitch + this.currentNoisePitch;
         float gcd = ObsidianComponent_390.getGCD();
         if (gcd > 0.0f) {
            float dYaw = MathHelper.wrapDegrees(newYaw - this.currentYaw);
            float dPitch = newPitch - this.currentPitch;
            dYaw -= dYaw % gcd;
            dPitch -= dPitch % gcd;
            newYaw = this.currentYaw + dYaw + this.currentNoiseYaw;
            newPitch = this.currentPitch + dPitch + this.currentNoisePitch;
         }

         newPitch = MathHelper.clamp(newPitch, -89.0f, 89.0f);
         RotationManager.update(
            new ObsidianComponent_olorSetting(newYaw, newPitch),
            360.0f,
            360.0f,
            360.0f,
            360.0f,
            0,
            1,
            false
         );
      }
   }
}
