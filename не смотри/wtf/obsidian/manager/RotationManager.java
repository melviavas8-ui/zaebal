package wtf.obsidian.manager;

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
























import lombok.Generated;
import net.minecraft.util.math.MathHelper;

public class RotationManager implements IInstance {
   public static RotationManager instance;
   public ObsidianComponent_398 currentTask = ObsidianComponent_398.IDLE;
   public float currentYawSpeed;
   public float currentPitchSpeed;
   public float currentYawReturnSpeed;
   public float currentPitchReturnSpeed;
   public int currentPriority;
   public int currentTimeout;
   public int idleTicks;
   public ObsidianComponent_olorSetting targetRotation;

   public RotationManager() {
      instance = this;
      EventManager.register(this);
   }

   public static double direction(float rotationYaw, float moveForward, float moveStrafing) {
      if (moveForward < 0.0f) {
         rotationYaw += 180.0f;
      }

      float forward = 1.0f;
      if (moveForward < 0.0f) {
         forward = -0.5f;
      }

      if (moveForward > 0.0f) {
         forward = 0.5f;
      }

      if (moveStrafing > 0.0f) {
         rotationYaw -= 90.0f * forward;
      }

      if (moveStrafing < 0.0f) {
         rotationYaw += 90.0f * forward;
      }

      return Math.toRadians((double)rotationYaw);
   }

   public static void fixMovement(MotionEvent event, float yaw) {
      float forward = event.getMovementForward();
      float strafe = event.getMovementSideways();
      if (forward != 0.0f || strafe != 0.0f) {
         double targetAngle = MathHelper.wrapDegrees(Math.toDegrees(direction(yaw, forward, strafe)));
         float bestForward = 0.0f;
         float bestStrafe = 0.0f;
         float smallestDifference = 340282346638528859811704183484516925440.0f;

         for (float testForward = -1.0f;
            testForward <= 1.0F;
            testForward += 1.0f
         ) {
            for (float testStrafe = -1.0f;
               testStrafe <= 1.0f;
               testStrafe += 1.0f
            ) {
               if (testForward != 0.0f
                  || testStrafe != 0.0f) {
                  double testAngle = MathHelper.wrapDegrees(Math.toDegrees(direction(yaw, testForward, testStrafe)));
                  float difference = Math.abs(MathHelper.wrapDegrees((float)(targetAngle - testAngle)));
                  if (difference < smallestDifference) {
                     smallestDifference = difference;
                     bestForward = testForward;
                     bestStrafe = testStrafe;
                  }
               }
            }
         }

         event.setMovementForward(bestForward);
         event.setMovementSideways(bestStrafe);
      }
   }

   @EventTarget
   public void onInput(MotionEvent event) {
      if (this.isRotating()) {
         fixMovement(event, MathHelper.wrapDegrees(mc.gameRenderer.getCamera().getYaw()));
      }
   }
   public void resetRotation() {
      if (mc.player != null) {
         ObsidianComponent_olorSetting freeTarget = new ObsidianComponent_olorSetting(ObsidianComponent_142.getFreeYaw(), ObsidianComponent_142.getFreePitch());
         float remainingYaw = Math.abs(MathHelper.wrapDegrees(freeTarget.getYaw() - mc.player.getYaw()));
         float remainingPitch = Math.abs(freeTarget.getPitch() - mc.player.getPitch());
         float yawSpeed = Math.max(
            3.0f, remainingYaw * 0.44999998807907104f
         );
         float pitchSpeed = Math.max(
            3.0f, remainingPitch * 0.44999998807907104f
         );
         if (this.updateRotation(freeTarget, yawSpeed, pitchSpeed)) {
            this.stopRotation();
         }
      }
   }

   @EventTarget
   public void onEventTick(EventTick event) {
      if (this.currentTask().equals(ObsidianComponent_398.AIM) && this.idleTicks() > this.currentTimeout()) {
         this.currentTask(ObsidianComponent_398.RESET);
      }

      if (this.currentTask().equals(ObsidianComponent_398.RESET)) {
         this.resetRotation();
      }

      this.idleTicks += 1;
   }

   public static void update(
      ObsidianComponent_olorSetting target, float yawSpeed, float pitchSpeed, float yawReturnSpeed, float pitchReturnSpeed, int timeout, int priority, boolean clientRotation
   ) {
      RotationManager instance = RotationManager.instance;
      if (mc.player != null) {
         if (instance.currentPriority() <= priority) {
            if (clientRotation) {
               ObsidianComponent_142.setActive(false);
            } else {
               ObsidianComponent_142.setActive(true);
            }

            instance.currentYawSpeed(yawSpeed);
            instance.currentPitchSpeed(pitchSpeed);
            instance.currentYawReturnSpeed(yawReturnSpeed);
            instance.currentPitchReturnSpeed(pitchReturnSpeed);
            instance.currentTimeout(timeout);
            instance.currentPriority(priority);
            instance.currentTask(ObsidianComponent_398.AIM);
            instance.targetRotation(target);
            instance.updateRotation(target, yawSpeed, pitchSpeed);
         }
      }
   }

   public static void update(ObsidianComponent_olorSetting targetRotation, float turnSpeed, float returnSpeed, int timeout, int priority) {
      update(targetRotation, turnSpeed, turnSpeed, returnSpeed, returnSpeed, timeout, priority, false);
   }

   public static void update(ObsidianComponent_olorSetting targetRotation, float yawSpeed, float pitchSpeed, float returnSpeed, int timeout, int priority) {
      update(targetRotation, yawSpeed, pitchSpeed, returnSpeed, returnSpeed, timeout, priority, false);
   }
   public boolean updateRotation(ObsidianComponent_olorSetting targetRotation, float yawSpeed, float pitchSpeed) {
      if (mc.player == null) {
         return false;
      } else {
         ObsidianComponent_olorSetting currentRotation = new ObsidianComponent_olorSetting(mc.player);
         float yawDelta = MathHelper.wrapDegrees(targetRotation.getYaw() - currentRotation.getYaw());
         float pitchDelta = targetRotation.getPitch() - currentRotation.getPitch();
         float clampedYaw = Math.min(Math.abs(yawDelta), yawSpeed);
         float clampedPitch = Math.min(Math.abs(pitchDelta), pitchSpeed);
         float yaw = mc.player.getYaw();
         yaw += ObsidianComponent_390.getFixedRotation(MathHelper.clamp(yawDelta, -clampedYaw, clampedYaw));
         mc.player.setYaw(yaw);
         mc.player
            .setPitch(
               MathHelper.clamp(
                  mc.player.getPitch() + ObsidianComponent_390.getFixedRotation(MathHelper.clamp(pitchDelta, -clampedPitch, clampedPitch)),
                  -90.0f,
                  90.0f
               )
            );
         this.idleTicks(0);
         return (new ObsidianComponent_olorSetting(mc.player).getDelta(targetRotation) < 1.0f);
      }
   }

   public void stopRotation() {
      this.currentTask(ObsidianComponent_398.IDLE);
      this.currentPriority(0);
      ObsidianComponent_142.setActive(false);
   }

   public boolean isRotating() {
      return (!this.currentTask.equals(ObsidianComponent_398.IDLE));
   }

   @Generated
   public ObsidianComponent_398 currentTask() {
      return this.currentTask;
   }

   @Generated
   public float currentYawSpeed() {
      return this.currentYawSpeed;
   }

   @Generated
   public float currentPitchSpeed() {
      return this.currentPitchSpeed;
   }

   @Generated
   public float currentYawReturnSpeed() {
      return this.currentYawReturnSpeed;
   }

   @Generated
   public float currentPitchReturnSpeed() {
      return this.currentPitchReturnSpeed;
   }

   @Generated
   public int currentPriority() {
      return this.currentPriority;
   }

   @Generated
   public int currentTimeout() {
      return this.currentTimeout;
   }

   @Generated
   public int idleTicks() {
      return this.idleTicks;
   }

   @Generated
   public ObsidianComponent_olorSetting targetRotation() {
      return this.targetRotation;
   }

   @Generated
   public RotationManager currentTask(ObsidianComponent_398 currentTask) {
      this.currentTask = currentTask;
      return this;
   }

   @Generated
   public RotationManager currentYawSpeed(float currentYawSpeed) {
      this.currentYawSpeed = currentYawSpeed;
      return this;
   }

   @Generated
   public RotationManager currentPitchSpeed(float currentPitchSpeed) {
      this.currentPitchSpeed = currentPitchSpeed;
      return this;
   }

   @Generated
   public RotationManager currentYawReturnSpeed(float currentYawReturnSpeed) {
      this.currentYawReturnSpeed = currentYawReturnSpeed;
      return this;
   }

   @Generated
   public RotationManager currentPitchReturnSpeed(float currentPitchReturnSpeed) {
      this.currentPitchReturnSpeed = currentPitchReturnSpeed;
      return this;
   }

   @Generated
   public RotationManager currentPriority(int currentPriority) {
      this.currentPriority = currentPriority;
      return this;
   }

   @Generated
   public RotationManager currentTimeout(int currentTimeout) {
      this.currentTimeout = currentTimeout;
      return this;
   }

   @Generated
   public RotationManager idleTicks(int idleTicks) {
      this.idleTicks = idleTicks;
      return this;
   }

   @Generated
   public RotationManager targetRotation(ObsidianComponent_olorSetting targetRotation) {
      this.targetRotation = targetRotation;
      return this;
   }
}
