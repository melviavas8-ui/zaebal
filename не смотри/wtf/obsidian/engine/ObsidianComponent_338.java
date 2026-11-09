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

public class ObsidianComponent_338 extends CombatPredictionHelper implements IInstance {
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

   public ObsidianComponent_338(KillAuraModule aura) {
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
   }

   public void checkAndAutoSwitchProfile() {
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
            float huina1 = (float)Math.sin((double)((float)this.tickCounter * 0.12999999523162842f))
               * 0.07999999821186066f;
            float huina2 = (float)Math.cos((double)((float)this.tickCounter * 0.050999999046325684f))
               * 0.04699999839067459f;
            float random = (ThreadLocalRandom.current().nextFloat() - 0.5099999904632568f)
               * 0.019999999552965164f;
            this.jitterFadeScale = Math.min(
               1.0f, this.jitterFadeScale + 0.10000000149011612f
            );
            this.jitterOffset = (huina1 + huina2 + random) * this.jitterFadeScale;
            Vec3d point = ObsidianComponent_311.getMultipoint(target, 128.0);
            Vec2f angle = ObsidianComponent_374.getRotations(point);
            float targetYaw = angle.x;
            float targetPitch = angle.y;
            float yawDiff = Math.abs(MathHelper.wrapDegrees(targetYaw - this.lastYaw));
            boolean readyToAttack = (mc.player.getAttackCooldownProgress(1.0f)
                     > 0.8999999761581421f
                  && this.aura.getWhiteRiseTicksToAttack() <= 1);
            if (!this.back) {
               float gain = 0.00419999985024333f;
               if (yawDiff > 60.0f) {
                  gain += 0.019999999552965164f;
               } else if (yawDiff > 30.0F) {
                  gain += 0.009999999776482582f;
               } else {
                  gain += 0.003800000064074993f;
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
               mc.player.isGliding() ? 0.3400000035762787f : 0.2199999988079071f
            );
            if (readyToAttack) {
               smooth = Math.min(
                  smooth + 0.05999999865889549f,
                  mc.player.isGliding()
                     ? 0.4000000059604645f
                     : 0.27000001072883606f
               );
            }

            smooth += this.jitterOffset * 0.5f;
            if (this.tickCounter % 7 == 0) {
               smooth += 0.029999999329447746f;
            }

            float deltaYaw = MathHelper.wrapDegrees(targetYaw - this.lastYaw);
            float deltaPitch = targetPitch - this.lastPitch;
            float yawLimit = mc.player.isGliding()
               ? 38.0f
               : (readyToAttack ? 24.0f : 17.0f);
            float pitchLimit = mc.player.isGliding()
               ? 10.0f
               : (readyToAttack ? 3.799999952316284f : 2.299999952316284f);
            deltaYaw = MathHelper.clamp(deltaYaw, -yawLimit, yawLimit);
            deltaPitch = MathHelper.clamp(deltaPitch, -pitchLimit, pitchLimit);
            float pitchSpeed = smooth * 0.25999999046325684f;
            float yawSpeed = smooth
               * (
                  0.8299999833106995f
                     + this.jitterOffset * 0.2199999988079071f
               );
            float newYaw = this.lastYaw + deltaYaw * yawSpeed;
            float newPitch = this.lastPitch + deltaPitch * pitchSpeed;
            float gcd = ObsidianComponent_390.getGCDValue();
            if (gcd > 0.0f) {
               newYaw = this.lastYaw + (float)Math.round((newYaw - this.lastYaw) / gcd) * gcd;
               newPitch = this.lastPitch + (float)Math.round((newPitch - this.lastPitch) / gcd) * gcd;
            }

            newPitch = MathHelper.clamp(
               newPitch, -89.0f, 89.0f
            );
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
            this.rotate = new Vec2f(finalRot.getYaw(), finalRot.getPitch());
            this.lastYaw = finalRot.getYaw();
            this.lastPitch = finalRot.getPitch();
         }
      }
   }
}
