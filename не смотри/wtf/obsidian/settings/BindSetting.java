package wtf.obsidian.settings;

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

public class BindSetting extends CombatPredictionHelper implements IInstance {
   public final KillAuraModule aura;
   public LivingEntity trackedTarget;
   public float currentYaw;
   public float currentPitch;
   public boolean initialized;
   public int tickCounter;
   public float seedX;
   public float seedY;
   public float seedZ;
   public float smoothedSpeed = 0.11999999731779099f;

   public BindSetting(KillAuraModule aura) {
      this.aura = aura;
      this.randomizeSeeds();
   }
   public void randomizeSeeds() {
      this.seedX = ThreadLocalRandom.current().nextFloat() * 100.0f;
      this.seedY = ThreadLocalRandom.current().nextFloat() * 100.0f;
      this.seedZ = ThreadLocalRandom.current().nextFloat() * 100.0f;
   }

   public void reset() {
      this.trackedTarget = null;
      this.tickCounter = 0;
      this.initialized = false;
      this.smoothedSpeed = 0.11999999731779099f;
      this.randomizeSeeds();
      if (mc.player != null) {
         this.currentYaw = mc.player.getYaw();
         this.currentPitch = mc.player.getPitch();
      }
   }

   public void onAttack() {
   }

   @Override
   public void updateRotations(LivingEntity target) {
      if (mc.player != null && target != null) {
         if (mc.player.isBlocking()) {
            this.rotate = new Vec2f(mc.player.getYaw(), mc.player.getPitch());
            this.currentYaw = this.rotate.x;
            this.currentPitch = this.rotate.y;
         } else {
            if (!this.initialized) {
               this.currentYaw = mc.player.getYaw();
               this.currentPitch = mc.player.getPitch();
               this.initialized = true;
            }

            if (this.trackedTarget != target) {
               this.trackedTarget = target;
               this.tickCounter = 0;
               this.smoothedSpeed = 0.10000000149011612f;
               this.randomizeSeeds();
            }

            this.tickCounter += 1;
            double timeSec = (double)System.currentTimeMillis() / 1000.0;
            double offsetX = Math.sin(
                  timeSec * 1.4 + (double)this.seedX
               )
               * 0.22;
            double offsetY = 0.52
               + Math.sin(timeSec * 0.9 + (double)this.seedY)
                  * 0.18
               + Math.cos(timeSec * 1.8 + (double)this.seedZ) * 0.06;
            double offsetZ = Math.cos(
                  timeSec * 1.4 + (double)this.seedZ
               )
               * 0.22;
            Box box = target.getBoundingBox();
            double aimX = target.getX() + box.getLengthX() * offsetX;
            double aimY = target.getY()
               + MathHelper.clamp(
                  box.getLengthY() * offsetY,
                  0.25,
                  box.getLengthY() * 0.85
               );
            double aimZ = target.getZ() + box.getLengthZ() * offsetZ;
            Vec3d aimVector = new Vec3d(aimX, aimY, aimZ);
            Vec2f targetRot = ObsidianComponent_374.getRotations(aimVector);
            float targetYaw = targetRot.x;
            float targetPitch = targetRot.y;
            float yawDelta = MathHelper.wrapDegrees(targetYaw - this.currentYaw);
            float pitchDelta = targetPitch - this.currentPitch;
            double totalDistance = Math.hypot((double)yawDelta, (double)pitchDelta);
            boolean readyToAttack = (mc.player.getAttackCooldownProgress(1.0f)
                     > 0.8999999761581421f
                  && this.aura.getWhiteRiseTicksToAttack() <= (1));
            float baseTargetSpeed = readyToAttack
               ? 0.2199999988079071f
               : 0.14000000059604645f;
            if (totalDistance > 45.0) {
               baseTargetSpeed += 0.07999999821186066f;
            } else if (totalDistance < 3.0) {
               baseTargetSpeed *= 0.6499999761581421f;
               float speedOrganicNoise = (float)(
                  Math.sin(timeSec * 2.5 + (double)this.seedX)
                     * 0.025
               );
               float desiredSpeed = MathHelper.clamp(
                  baseTargetSpeed + speedOrganicNoise,
                  0.05999999865889549f,
                  0.3199999928474426f
               );
               this.smoothedSpeed = this.smoothedSpeed + (desiredSpeed - this.smoothedSpeed) * 0.08F;
               float maxStepYaw = readyToAttack
                  ? 21.0f
                  : 15.5f;
               float maxStepPitch = readyToAttack
                  ? 3.200000047683716f
                  : 2.0999999046325684f;
               if (mc.player.isGliding()) {
                  maxStepYaw = 38.0f;
                  maxStepPitch = 9.0f;
               }

               float stepYaw = MathHelper.clamp(yawDelta * this.smoothedSpeed, -maxStepYaw, maxStepYaw);
               float stepPitch = MathHelper.clamp(
                  pitchDelta * this.smoothedSpeed * 0.47999998927116394f, -maxStepPitch, maxStepPitch
               );
               float nextYaw = this.currentYaw + stepYaw;
               float nextPitch = MathHelper.clamp(this.currentPitch + stepPitch, -89.0f, 89.0F);
               float gcd = ObsidianComponent_390.getGCDValue();
               if (gcd > 0.0f) {
                  nextYaw = this.currentYaw + (float)Math.round((nextYaw - this.currentYaw) / gcd) * gcd;
                  nextPitch = this.currentPitch + (float)Math.round((nextPitch - this.currentPitch) / gcd) * gcd;
               }

               this.currentYaw = nextYaw;
               this.currentPitch = nextPitch;
               ObsidianComponent_olorSetting finalRot = new ObsidianComponent_olorSetting(this.currentYaw, this.currentPitch);
               RotationManager.update(
                  finalRot,
                  180.0f,
                  180.0f,
                  180.0f,
                  180.0f,
                  0,
                  1,
                  false
               );
               this.rotate = new Vec2f(this.currentYaw, this.currentPitch);
            }
         }
      }
   }
}
