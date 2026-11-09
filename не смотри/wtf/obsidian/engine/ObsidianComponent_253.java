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
























import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Generated;
import net.minecraft.util.math.MathHelper;

public final class ObsidianComponent_253 {
   public static float clamp01(float x) {
      return (float)clamp(
         0.0,
         1.0,
         (double)x
      );
   }

   public static double getRandom(double min, double max) {
      if (min == max) {
         return min;
      } else {
         if (min > max) {
            double d = min;
            min = max;
            max = d;
         }

         return ThreadLocalRandom.current().nextDouble() * (max - min) + min;
      }
   }

   public static float calculateDelta(float a, float b) {
      return a - b;
   }

   public static double round(double target, int decimal) {
      double p = Math.pow(10.0, (double)decimal);
      return (double)Math.round(target * p) / p;
   }

   public static Number round(double num, double increment) {
      if (increment <= 0.0) {
         throw new IllegalArgumentException("Increment must be greater than zero");
      } else {
         double roundedValue = (double)Math.round(num / increment) * increment;
         BigDecimal bigDecimal = BigDecimal.valueOf(roundedValue);
         bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_UP);
         return bigDecimal.doubleValue();
      }
   }

   public static String formatTime(long millis) {
      long hours = millis / (3600000L);
      long minutes = millis
         % (3600000L)
         / (60000L);
      long seconds = millis
         % (360000L)
         % (60000L)
         / (1000L);
      Object[] var10001 = new Object[3];
      var10001[0] = hours;
      var10001[1] = minutes;
      var10001[2] = seconds;
      return String.format("%02d:%02d:%02d", var10001);
   }

   public static float slerp(float start, float end, float t) {
      t = Math.max(
         0.0f, Math.min(1.0f, t)
      );
      float startRadians = (float)Math.toRadians((double)start);
      float endRadians = (float)Math.toRadians((double)end);
      float dotProduct = (float)Math.cos((double)startRadians) * (float)Math.cos((double)endRadians)
         + (float)Math.sin((double)startRadians) * (float)Math.sin((double)endRadians);
      float angle = (float)Math.acos((double)dotProduct);
      if (Math.abs(angle) < 0.0010000000474974513f) {
         return start;
      } else {
         float factorStart = (float)(
            Math.sin((double)((1.0f - t) * angle)) / Math.sin((double)angle)
         );
         float factorEnd = (float)(Math.sin((double)(t * angle)) / Math.sin((double)angle));
         float interpolatedValue = start * factorStart + end * factorEnd;
         return (float)MathHelper.clamp(MathHelper.wrapDegrees(Math.toDegrees((double)interpolatedValue)), (double)start, (double)end);
      }
   }

   public static double round(double value, int scale, double inc) {
      double halfOfInc = inc / 2.0;
      double floored = Math.floor(value / inc) * inc;
      return value >= floored + halfOfInc
         ? new BigDecimal(Math.ceil(value / inc) * inc).setScale(scale, RoundingMode.HALF_UP).doubleValue()
         : new BigDecimal(floored).setScale(scale, RoundingMode.HALF_UP).doubleValue();
   }

   public static double step(double value, double steps) {
      double a = (double)Math.round(value / steps) * steps;
      a *= 1000.0;
      a = (double)((int)a);
      return a / 1000.0;
   }

   public static double getDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
      double deltaX = x2 - x1;
      double deltaY = y2 - y1;
      double deltaZ = z2 - z1;
      return (double)MathHelper.sqrt((float)(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ));
   }

   public static double clamp(double min, double max, double n) {
      return Math.max(min, Math.min(max, n));
   }

   public static int clamp(int min, int max, int value) {
      return Math.max(min, Math.min(max, value));
   }

   public static float normalize(float value, float min, float max) {
      return (value - min) / (max - min);
   }

   public static double interporate(double p_219803_0_, double p_219803_2_, double p_219803_4_) {
      return p_219803_2_ + p_219803_0_ * (p_219803_4_ - p_219803_2_);
   }

   public static float lerp(float min, float max, float delta) {
      return min + (max - min) * delta;
   }

   public static float easeOutExpo(float x) {
      return x == 1.0f
         ? 1.0f
         : (float)(
            1.0
               - Math.pow(
                  2.0,
                  (double)(-10.0f * x)
               )
         );
   }

   @Generated
   public ObsidianComponent_253() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
