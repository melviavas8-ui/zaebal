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
























import lombok.Generated;
import net.minecraft.util.math.MathHelper;

public final class ObsidianComponent_108 implements IInstance {
   public static double getBoost() {
      int[] var10000 = new int[4];
      var10000[0] = -45;
      var10000[1] = 45;
      var10000[2] = 135;
      var10000[3] = -135;
      int[] vectors = var10000;
      var10000 = new int[5];
      var10000[0] = -90;
      var10000[1] = 90;
      var10000[2] = 180;
      var10000[3] = -180;
      var10000[4] = 0;
      int[] addVectors = var10000;
      var10000 = new int[2];
      var10000[0] = -45;
      var10000[1] = 45;
      int[] pitchVectors = var10000;
      float lastYaw = mc.player.prevYaw;
      float lastPitch = mc.player.prevPitch;
      int minDist = findClosestVector(lastYaw, vectors);
      float maxDist = Math.abs(MathHelper.wrapDegrees(lastYaw) - (float)vectors[minDist]);
      int addMinDist = findClosestVector(lastYaw, addVectors);
      float addMaxDist = Math.abs(MathHelper.wrapDegrees(lastYaw) - (float)addVectors[addMinDist]);
      float countableSpeed = minDist == -1
         ? 1.5f
         : 1.9500000476837158f
            - maxDist * 0.5600000023841858f / 45.0f;
      if (addMaxDist < 10.0f) {
         countableSpeed += 0.10000000149011612f - 0.1F * addMaxDist / 10.0f;
      }

      int pitchMinDist = findClosestVector(lastPitch, pitchVectors);
      float pitchMaxDist = Math.abs(Math.abs(lastPitch) - (float)Math.abs(pitchVectors[pitchMinDist]));
      if (pitchMaxDist < 26.0f) {
         countableSpeed = Math.max(1.940000057220459f, countableSpeed);
         countableSpeed += 0.05000000074505806f
            - pitchMaxDist * 0.05000000074505806f / 26.0f;
      }

      countableSpeed = Math.min(2.0450000762939453f, countableSpeed);
      if (mc.player.prevPitch > -55.0f
         && mc.player.prevPitch < -19.0f) {
         countableSpeed = 1.909999966621399f;
      } else if (mc.player.prevPitch < -55.0f) {
         countableSpeed = 1.5399999618530273f;
      }

      if (mc.player.prevPitch > 19.0f
         && mc.player.prevPitch < 55.0f) {
         countableSpeed = 1.7999999523162842f;
      } else if (mc.player.prevPitch > 55.0f) {
         countableSpeed = 1.5399999618530273f;
      }

      return (double)countableSpeed;
   }
   public static int findClosestVector(float lastYaw, int[] vectors) {
      int index = 0;
      int minDistIndex = -1;
      float minDist = 340282346638528859811704183484516925440.0f;
      int[] var5 = vectors;
      int var6 = vectors.length;

      for (int var7 = 0; var7 < var6; var7++) {
         int vector = var5[var7];
         float dist = Math.abs(MathHelper.wrapDegrees(lastYaw) - (float)vector);
         if (dist < minDist) {
            minDist = dist;
            minDistIndex = index;
         }

         index++;
      }

      return minDistIndex;
   }

   @Generated
   public ObsidianComponent_108() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
