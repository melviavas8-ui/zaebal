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
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ObsidianComponent_268 {
   private static final MinecraftClient mc = MinecraftClient.getInstance();

   public static void setMotion(double motion) {
      if (mc.player != null) {
         double forward = (double)mc.player.input.movementForward;
         double strafe = (double)mc.player.input.movementSideways;
         float yaw = mc.player.getYaw();
         if (forward == 0.0
            && strafe == 0.0) {
            mc.player
               .setVelocity(
                  0.0,
                  mc.player.getVelocity().y,
                  0.0
               );
         } else {
            if (forward != 0.0) {
               if (strafe > 0.0) {
                  yaw += (float)(
                     forward > 0.0
                        ? -45
                        : 45
                  );
               } else if (strafe < 0.0) {
                  yaw += (float)(
                     forward > 0.0
                        ? 45
                        : -45
                  );
               }

               strafe = 0.0;
               if (forward > 0.0) {
                  forward = 1.0;
               } else if (forward < 0.0) {
                  forward = Double.longBitsToDouble(-4616189618054758400L);
               }
            }

            double motionX = forward
                  * motion
                  * (double)MathHelper.cos((float)Math.toRadians((double)(yaw + 90.0f)))
               + strafe
                  * motion
                  * (double)MathHelper.sin((float)Math.toRadians((double)(yaw + 90.0f)));
            double motionZ = forward
                  * motion
                  * (double)MathHelper.sin((float)Math.toRadians((double)(yaw + 90.0f)))
               - strafe * motion * (double)MathHelper.cos((float)Math.toRadians((double)(yaw + 90.0f)));
            mc.player.setVelocity(motionX, mc.player.getVelocity().y, motionZ);
         }
      }
   }

   public static double getSpeed() {
      if (mc.player == null) {
         return 0.0;
      } else {
         Vec3d velocity = mc.player.getVelocity();
         return Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
      }
   }

   public static void setVelocity(double velocity) {
      double[] direction = ObsidianComponent_180.calculateDirection(velocity);
      Objects.requireNonNull(mc.player)
         .setVelocity(direction[0], mc.player.getVelocity().getY(), direction[1]);
   }

   public static void setVelocity(double velocity, double y) {
      double[] direction = ObsidianComponent_180.calculateDirection(velocity);
      Objects.requireNonNull(mc.player).setVelocity(direction[0], y, direction[1]);
   }

   public static void strafe() {
      strafe(getSpeed());
   }

   public static void strafe(double speed) {
      if (mc.player != null) {
         float yaw = mc.player.getYaw();
         double forward = (double)mc.player.input.movementForward;
         double strafe = (double)mc.player.input.movementSideways;
         if (forward == 0.0
            && strafe == 0.0) {
            mc.player
               .setVelocity(
                  0.0,
                  mc.player.getVelocity().y,
                  0.0
               );
         } else {
            if (forward != 0.0) {
               if (strafe > 0.0) {
                  yaw += forward > 0.0
                     ? -45.0f
                     : 45.0f;
               } else if (strafe < 0.0) {
                  yaw += forward > 0.0
                     ? 45.0f
                     : -45.0f;
               }

               strafe = 0.0;
               forward = forward > 0.0
                  ? 1.0
                  : Double.longBitsToDouble(-4616189618054758400L);
            }

            double rad = Math.toRadians((double)(yaw + 90.0f));
            double motionX = forward * speed * Math.cos(rad) + strafe * speed * Math.sin(rad);
            double motionZ = forward * speed * Math.sin(rad) - strafe * speed * Math.cos(rad);
            mc.player.setVelocity(motionX, mc.player.getVelocity().y, motionZ);
         }
      }
   }

   public static boolean isMoving() {
      if (mc.player == null) {
         return false;
      } else {
         return (!(mc.player.input.movementForward == 0.0f
               && mc.player.input.movementSideways == 0.0f));
      }
   }
}
