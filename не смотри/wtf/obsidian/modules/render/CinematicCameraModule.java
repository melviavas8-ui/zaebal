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

























import net.minecraft.util.math.MathHelper;

public class CinematicCameraModule extends Module {
   public static CinematicCameraModule INSTANCE = new CinematicCameraModule();
   public static final float SWITCH_ANIM_SPEED = 0.26F;
   public static final float DISTANCE_SPEED = 0.13F;
   public static final float ROTATION_SMOOTH = 0.28F;
   public static final float CAMERA_DISTANCE = 4.1F;
   public static final float SNEAK_OFFSET = 0.5F;
   public static final float JUMP_MULTIPLIER = 2.0F;
   public static final float ANIM_SPEED = 0.13F;
   public float currentDistance;
   public float prevDistance;
   public float currentYaw;
   public float prevYaw;
   public float currentPitch;
   public float prevPitch;
   public float heightOffset;
   public float prevHeightOffset;
   public boolean switchAnimating;
   public boolean wasThirdPerson;
   private boolean needsInit = true;

   public CinematicCameraModule() {
      super("Cinematic Camera", "Плавная камера от ф5", Category.RENDER);
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.player != null && mc.world != null) {
         boolean isThirdPerson = (!mc.options.getPerspective().isFirstPerson());
         if (isThirdPerson && !this.wasThirdPerson) {
            this.initCamera(true);
         }

         if (!isThirdPerson && this.wasThirdPerson) {
            this.needsInit = true;
            this.switchAnimating = false;
         }

         this.wasThirdPerson = isThirdPerson;
         if (isThirdPerson) {
            this.updateCamera();
         }
      }
   }

   @EventTarget(
      priority = 100
   )
   public void onRotation(Event_222 event) {
      if (mc.player != null && mc.world != null) {
         if (!mc.options.getPerspective().isFirstPerson()) {
            event.setYaw(this.getInterpolatedYaw(event.getPartialTicks()));
            event.setPitch(this.getInterpolatedPitch(event.getPartialTicks()));
         }
      }
   }
   public void initCamera(boolean animateSwitch) {
      if (mc.player != null) {
         this.currentYaw = this.prevYaw = this.getReferenceYaw();
         this.currentPitch = this.prevPitch = this.getReferencePitch();
         this.currentDistance = this.prevDistance = animateSwitch
            ? 0.0f
            : 4.099999904632568f;
         this.heightOffset = this.prevHeightOffset = 0.0f;
         this.switchAnimating = animateSwitch;
         this.needsInit = false;
      }
   }
   public void updateCamera() {
      if (mc.player != null) {
         if (this.needsInit) {
            this.initCamera(true);
         } else {
            this.prevYaw = this.currentYaw;
            this.prevPitch = this.currentPitch;
            this.prevDistance = this.currentDistance;
            this.prevHeightOffset = this.heightOffset;
            float rotationSpeed = 0.28F;
            this.currentYaw = this.currentYaw + MathHelper.wrapDegrees(this.getReferenceYaw() - this.currentYaw) * rotationSpeed;
            this.currentPitch = MathHelper.clamp(
               this.currentPitch + (this.getReferencePitch() - this.currentPitch) * rotationSpeed,
               -90.0f,
               90.0f
            );
            float distanceSpeed = this.switchAnimating
               ? 0.25999999046325684f
               : 0.12999999523162842f;
            this.currentDistance = this.currentDistance + (4.099999904632568f - this.currentDistance) * distanceSpeed;
            if (this.switchAnimating
               && Math.abs(4.099999904632568f - this.currentDistance) <= 0.019999999552965164f) {
               this.currentDistance = 4.099999904632568f;
               this.switchAnimating = false;
            }

            float targetOffset = 0.0f;
            if (mc.player.isSneaking()) {
               targetOffset = -0.5f;
            }

            if (!mc.player.isOnGround()) {
               targetOffset += (float)(
                  -mc.player.getVelocity().y * 2.0
               );
            }

            this.heightOffset = this.heightOffset + (targetOffset - this.heightOffset) * 0.12999999523162842f;
         }
      }
   }

   public float getInterpolatedYaw(float partialTicks) {
      return mc.player == null
         ? 0.0f
         : this.prevYaw + (this.currentYaw - this.prevYaw) * partialTicks;
   }

   public float getInterpolatedPitch(float partialTicks) {
      return mc.player == null
         ? 0.0f
         : MathHelper.clamp(
            this.prevPitch + (this.currentPitch - this.prevPitch) * partialTicks,
            -90.0f,
            90.0f
         );
   }

   public float getInterpolatedDistance(float partialTicks) {
      return this.prevDistance + (this.currentDistance - this.prevDistance) * partialTicks;
   }

   public float getInterpolatedHeightOffset(float partialTicks) {
      return this.prevHeightOffset + (this.heightOffset - this.prevHeightOffset) * partialTicks;
   }
   public float getReferenceYaw() {
      if (ObsidianComponent_142.isActive()) {
         return ObsidianComponent_142.getFreeYaw();
      } else {
         return mc.player != null ? mc.player.getYaw() : 0.0f;
      }
   }
   public float getReferencePitch() {
      if (ObsidianComponent_142.isActive()) {
         return ObsidianComponent_142.getFreePitch();
      } else {
         return mc.player != null ? mc.player.getPitch() : 0.0f;
      }
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.needsInit = true;
      this.wasThirdPerson = false;
      if (mc.player != null && !mc.options.getPerspective().isFirstPerson()) {
         this.initCamera(true);
         this.wasThirdPerson = true;
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.needsInit = true;
      this.heightOffset = 0.0f;
      this.prevHeightOffset = 0.0f;
   }
}
