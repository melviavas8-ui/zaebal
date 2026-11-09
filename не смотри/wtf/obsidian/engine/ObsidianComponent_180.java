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
























import java.util.Objects;
import lombok.Generated;
import net.minecraft.entity.Entity;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class ObsidianComponent_180 implements IInstance {
   public static boolean hasPlayerMovement() {
      return (!(mc.player.input.movementForward == 0.0f
            && mc.player.input.movementSideways == 0.0f));
   }

   public static double[] calculateDirection(double distance) {
      float forward = mc.player.input.movementForward;
      float sideways = mc.player.input.movementSideways;
      float yaw = mc.player.getYaw();
      if (forward != 0.0f) {
         if (sideways > 0.0f) {
            yaw += forward > 0.0f
               ? -45.0f
               : 45.0f;
         } else if (sideways < 0.0f) {
            yaw += forward > 0.0f
               ? 45.0f
               : -45.0f;
         }

         sideways = 0.0f;
         forward = forward > 0.0f
            ? 1.0f
            : -1.0f;
      }

      double sinYaw = Math.sin(Math.toRadians((double)(yaw + 90.0f)));
      double cosYaw = Math.cos(Math.toRadians((double)(yaw + 90.0f)));
      double xMovement = (double)forward * distance * cosYaw + (double)sideways * distance * sinYaw;
      double zMovement = (double)forward * distance * sinYaw - (double)sideways * distance * cosYaw;
      double[] var10000 = new double[2];
      var10000[0] = xMovement;
      var10000[1] = zMovement;
      return var10000;
   }

   public static double getSpeedSqrt(Entity entity) {
      double dx = entity.getX() - entity.prevX;
      double dy = entity.getY() - entity.prevY;
      double dz = entity.getZ() - entity.prevZ;
      return Math.sqrt(dx * dx + dz * dz + dy * dy);
   }

   public static void setVelocity(double velocity) {
      double[] direction = calculateDirection(velocity);
      Objects.requireNonNull(mc.player)
         .setVelocity(direction[0], mc.player.getVelocity().getY(), direction[1]);
   }

   public static void setVelocity(double velocity, double y) {
      double[] direction = calculateDirection(velocity);
      Objects.requireNonNull(mc.player).setVelocity(direction[0], y, direction[1]);
   }

   public static double getDegreesRelativeToView(Vec3d positionRelativeToPlayer, float yaw) {
      float optimalYaw = (float)Math.atan2(-positionRelativeToPlayer.x, positionRelativeToPlayer.z);
      double currentYaw = Math.toRadians((double)MathHelper.wrapDegrees(yaw));
      return Math.toDegrees(MathHelper.wrapDegrees((double)optimalYaw - currentYaw));
   }

   public static PlayerInput getDirectionalInputForDegrees(PlayerInput input, double dgs, float deadAngle) {
      boolean forwards = input.forward();
      boolean backwards = input.backward();
      boolean left = input.left();
      boolean right = input.right();
      if (dgs >= (double)(-90.0f + deadAngle)
         && dgs <= (double)(90.0f - deadAngle)) {
         forwards = true;
      } else if (dgs < (double)(-90.0f - deadAngle)
         || dgs > (double)(90.0f + deadAngle)) {
         backwards = true;
      }

      if (dgs >= (double)(0.0f + deadAngle)
         && dgs <= (double)(180.0f - deadAngle)) {
         right = true;
      } else if (dgs >= (double)(-180.0f + deadAngle)
         && dgs <= (double)(0.0f - deadAngle)) {
         left = true;
      }

      return new PlayerInput(forwards, backwards, left, right, input.jump(), input.sneak(), input.sprint());
   }

   public static void fixMovementFocus(Event_102 event, float yaw) {
      float forward = event.getForward();
      float strafe = event.getStrafe();
      if (forward != 0.0f || strafe != 0.0f) {
         double targetAngle = MathHelper.wrapDegrees(Math.toDegrees(direction(yaw, forward, strafe)));
         float bestForward = 0.0f;
         float bestStrafe = 0.0f;
         float smallestDifference = 340282346638528859811704183484516925440.0f;

         for (float testForward = -1.0f;
            testForward <= 1.0f;
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

         event.setForward(bestForward);
         event.setStrafe(bestStrafe);
      }
   }

   public static void fixMovementFree(Event_102 event) {
      float forward = event.getForward();
      float strafe = event.getStrafe();
      double angle = MathHelper.wrapDegrees(Math.toDegrees(direction(mc.player.isGliding() ? mc.player.getYaw() : ObsidianComponent_142.getFreeYaw(), forward, strafe)));
      if (forward != 0.0f || strafe != 0.0f) {
         float closestForward = 0.0f;
         float closestStrafe = 0.0f;
         float closestDifference = 340282346638528859811704183484516925440.0f;

         for (float predictedForward = -1.0f;
            predictedForward <= 1.0f;
            predictedForward += 1.0f
         ) {
            for (float predictedStrafe = -1.0f;
               predictedStrafe <= 1.0f;
               predictedStrafe += 1.0f
            ) {
               if (predictedStrafe != 0.0f
                  || predictedForward != 0.0f) {
                  double predictedAngle = MathHelper.wrapDegrees(Math.toDegrees(direction(mc.player.getYaw(), predictedForward, predictedStrafe)));
                  double difference = Math.abs(angle - predictedAngle);
                  if (difference < (double)closestDifference) {
                     closestDifference = (float)difference;
                     closestForward = predictedForward;
                     closestStrafe = predictedStrafe;
                  }
               }
            }
         }

         event.setForward(closestForward);
         event.setStrafe(closestStrafe);
      }
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

   public static PlayerInput getDirectionalInputForDegrees(PlayerInput input, double dgs) {
      return getDirectionalInputForDegrees(input, dgs, 20.0f);
   }

   @Generated
   public ObsidianComponent_180() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
