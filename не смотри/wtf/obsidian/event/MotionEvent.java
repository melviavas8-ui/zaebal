package wtf.obsidian.event;

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

public class MotionEvent extends Event {
   public float movementForward;
   public float movementSideways;

   public void setYaw(float yaw, float yaw2) {
      float forward = this.getMovementForward();
      float sideways = this.getMovementSideways();
      double angle = MathHelper.wrapDegrees(Math.toDegrees(this.direction(yaw2, (double)forward, (double)sideways)));
      if (forward != 0.0f || sideways != 0.0f) {
         float closestForward = 0.0f;
         float closestSideways = 0.0f;
         float closestDifference = 340282346638528859811704183484516925440.0f;

         for (float predictedForward = -1.0f;
            predictedForward <= 1.0f;
            predictedForward += 1.0f
         ) {
            for (float predictedSideways = -1.0f;
               predictedSideways <= 1.0f;
               predictedSideways += 1.0f
            ) {
               if (predictedSideways != 0.0f
                  || predictedForward != 0.0f) {
                  double predictedAngle = MathHelper.wrapDegrees(Math.toDegrees(this.direction(yaw, (double)predictedForward, (double)predictedSideways)));
                  double difference = Math.abs(angle - predictedAngle);
                  if (difference < (double)closestDifference) {
                     closestDifference = (float)difference;
                     closestForward = predictedForward;
                     closestSideways = predictedSideways;
                  }
               }
            }
         }

         this.setMovementForward(closestForward);
         this.setMovementSideways(closestSideways);
      }
   }
   public double direction(float yaw, double movementForward, double movementSideways) {
      if (movementForward < 0.0) {
         yaw += 180.0f;
      }

      float forward = 1.0f;
      if (movementForward < 0.0) {
         forward = -0.5f;
      } else if (movementForward > 0.0) {
         forward = 0.5f;
      }

      if (movementSideways > 0.0) {
         yaw -= 90.0f * forward;
      }

      if (movementSideways < 0.0) {
         yaw += 90.0f * forward;
      }

      return Math.toRadians((double)yaw);
   }

   @Generated
   public MotionEvent(float movementForward, float movementSideways) {
      this.movementForward = movementForward;
      this.movementSideways = movementSideways;
   }

   @Generated
   public float getMovementForward() {
      return this.movementForward;
   }

   @Generated
   public float getMovementSideways() {
      return this.movementSideways;
   }

   @Generated
   public void setMovementForward(float movementForward) {
      this.movementForward = movementForward;
   }

   @Generated
   public void setMovementSideways(float movementSideways) {
      this.movementSideways = movementSideways;
   }
}
