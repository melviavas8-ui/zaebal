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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class ObsidianComponent_419 extends CombatPredictionHelper implements IInstance {
   public static final float SWING_GROWTH = 0.05F;
   public static final int MAX_HIT_BONUS = 30;
   public static final int JITTER_HALF = 60;
   public final KillAuraModule aura;
   public LivingEntity trackedTarget;
   public float lastYaw;
   public float lastPitch;
   public float speedAcceleration;
   public boolean back;
   public boolean initialized;
   public float jitterOffset;
   public int tickCounter;
   public float jitterFadeScale = 1.0f;
   public int hitCount;
   public int attackJitterTime;
   public float swingYawStep;
   public float swingPitchStep;

   public ObsidianComponent_419(KillAuraModule aura) {
      this.aura = aura;
   }

   public void reset() {
      this.trackedTarget = null;
      this.speedAcceleration = 0.0f;
      this.back = false;
      this.jitterFadeScale = Math.max(
         0.0f, this.jitterFadeScale - 0.07999999821186066f
      );
      this.tickCounter = 0;
      this.initialized = (mc.player != null);
      if (mc.player != null) {
         this.lastYaw = mc.player.getYaw();
         this.lastPitch = mc.player.getPitch();
      } else {
         this.lastYaw = 0.0f;
         this.lastPitch = 0.0f;
      }
   }

   public void onAttack() {
      if (mc.player != null) {
         this.hitCount = Math.min(this.hitCount + (1), 30);
         this.attackJitterTime = 120;
         float growth = 1.0f
            + Math.min(
               0.05000000074505806f * (float)this.hitCount, 1.0f
            );
         float randomness = 1.0f
            + (float)this.hitCount / 30.0f * 0.800000011920929f;
         float totalYaw = (
               32.0f
                  + ThreadLocalRandom.current().nextFloat() * 14.0f * randomness
            )
            * growth;
         float totalPitch = (
               4.5f
                  + ThreadLocalRandom.current().nextFloat() * 2.0f * randomness
            )
            * growth;
         float yawDir = ThreadLocalRandom.current().nextBoolean()
            ? 1.0f
            : -1.0f;
         int chance = ThreadLocalRandom.current().nextInt(100);
         float pitchDir;
         if (chance < 12) {
            pitchDir = -2.5999999046325684f;
         } else if (chance < (28)) {
            pitchDir = -1.2999999523162842f;
         } else {
            pitchDir = 1.0f;
         }

         this.swingYawStep = yawDir * totalYaw;
         this.swingPitchStep = pitchDir * totalPitch;
      }
   }

   @Override
   public void updateRotations(LivingEntity target) {
      if (mc.player != null && target != null) {
         if (mc.player.isBlocking()) {
            this.rotate = new Vec2f(mc.player.getYaw(), mc.player.getPitch());
            this.lastYaw = this.rotate.x;
            this.lastPitch = this.rotate.y;
         } else {
            if (!this.initialized) {
               this.lastYaw = mc.player.getYaw();
               this.lastPitch = mc.player.getPitch();
               this.initialized = true;
            }

            if (this.trackedTarget != target) {
               this.trackedTarget = target;
               this.speedAcceleration = 0.0f;
               this.back = false;
               this.tickCounter = 0;
               this.jitterFadeScale = 1.0f;
            }

            this.tickCounter += 1;
            float wave1 = (float)Math.sin((double)((float)this.tickCounter * 0.20000000298023224f))
               * 0.12999999523162842f;
            float wave2 = (float)Math.cos((double)((float)this.tickCounter * 0.03200000151991844f))
               * 0.07500000298023224f;
            float random = (ThreadLocalRandom.current().nextFloat() - 0.5f) * 0.008F;
            this.jitterFadeScale = Math.min(
               1.0f, this.jitterFadeScale + 0.07999999821186066f
            );
            this.jitterOffset = (wave1 + wave2 + random) * this.jitterFadeScale;
            Vec3d point = ObsidianComponent_311.getMultipoint(target, 128.0);
            Vec2f angle = ObsidianComponent_374.getRotations(point);
            float targetYaw = angle.x;
            float targetPitch = angle.y;
            float yawDiff = Math.abs(MathHelper.wrapDegrees(targetYaw - this.lastYaw));
            boolean readyToAttack = (mc.player.getAttackCooldownProgress(1.0f)
                     > 0.8999999761581421f
                  && this.aura.getWhiteRiseTicksToAttack() <= 1);
            if (!this.back) {
               float gain = 0.004100000020116568f;
               if (yawDiff > 60.0f) {
                  gain += 0.019999999552965164f;
               } else if (yawDiff > 30.0f) {
                  gain += 0.009999999776482582f;
               } else {
                  gain += 0.0034000000450760126f;
               }

               if (readyToAttack) {
                  gain += 0.009999999776482582f;
               }

               this.speedAcceleration = this.speedAcceleration + gain * (1.600000023841858f + this.jitterOffset);
               if (this.speedAcceleration >= 0.2199999988079071f) {
                  this.back = true;
               }
            } else {
               float loss = readyToAttack
                  ? 0.03200000151991844f
                  : 0.007000000216066837f;
               this.speedAcceleration = this.speedAcceleration - loss * (2.0999999046325684f + this.jitterOffset);
               if (this.speedAcceleration <= -0.03999999910593033f) {
                  this.back = false;
               }
            }

            float smooth = MathHelper.clamp(
               this.speedAcceleration,
               0.0f,
               mc.player.isGliding() ? 0.3400000035762787f : 0.22F
            );
            if (readyToAttack) {
               smooth = Math.min(
                  smooth + 0.05999999865889549f,
                  mc.player.isGliding() ? 0.4000000059604645f : 0.27000001072883606f
               );
            }

            smooth += this.jitterOffset * 0.5f;
            if (this.tickCounter % 7 == 0) {
               smooth += 0.029999999329447746f;
            }

            float yawLimit = mc.player.isGliding()
               ? 38.0f
               : (readyToAttack ? 24.0f : 17.0f);
            float pitchLimit = mc.player.isGliding()
               ? 10.0f
               : (
                  readyToAttack
                     ? 3.799999952316284f
                     : 2.299999952316284f
               );
            float deltaYaw = MathHelper.clamp(MathHelper.wrapDegrees(targetYaw - this.lastYaw), -yawLimit, yawLimit);
            float deltaPitch = MathHelper.clamp(targetPitch - this.lastPitch, -pitchLimit, pitchLimit);
            float yawNoise = this.jitterOffset * 0.4699999988079071f;
            float pitchNoise = this.jitterOffset * 0.05000000074505806f;
            float newYaw = this.lastYaw + deltaYaw * smooth * (0.84F + yawNoise);
            float newPitch = this.lastPitch + deltaPitch * smooth * 0.6700000166893005f + pitchNoise;
            float gcd = ObsidianComponent_390.getGCDValue();
            if (gcd > 0.0f) {
               newYaw = this.lastYaw + (float)Math.round((newYaw - this.lastYaw) / gcd) * gcd;
               newPitch = this.lastPitch + (float)Math.round((newPitch - this.lastPitch) / gcd) * gcd;
            }

            newPitch = MathHelper.clamp(newPitch, -89.0f, 89.0f);
            if (this.attackJitterTime > 0) {
               int total = 120;
               float progress = (float)(total - this.attackJitterTime) / (float)total;
               float prevProgress = (float)(total - this.attackJitterTime - 1) / (float)total;
               float currPos = (float)Math.sin(3.141592653589793 * (double)progress);
               float prevPos = (float)Math.sin(
                  3.141592653589793
                     * (double)Math.max(0.0f, prevProgress)
               );
               float delta = currPos - prevPos;
               newYaw = MathHelper.wrapDegrees(newYaw + this.swingYawStep * delta);
               newPitch = MathHelper.clamp(
                  newPitch + this.swingPitchStep * delta,
                  -89.0f,
                  89.0f
               );
               this.attackJitterTime -= 1;
            }

            this.lastYaw = newYaw;
            this.lastPitch = newPitch;
            ObsidianComponent_olorSetting finalRot = new ObsidianComponent_olorSetting(newYaw, newPitch);
            float rotSpeed = mc.player.isGliding() && target.isGliding()
               ? 360.0f
               : 45.0f;
            RotationManager.update(
               finalRot,
               rotSpeed,
               rotSpeed,
               rotSpeed,
               rotSpeed,
               0,
               1,
               false
            );
            this.rotate = new Vec2f(newYaw, newPitch);
         }
      }
   }
}
