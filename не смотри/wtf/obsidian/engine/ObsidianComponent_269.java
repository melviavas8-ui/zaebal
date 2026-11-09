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
























import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.Objects;
import lombok.Generated;
import net.minecraft.util.math.MathHelper;

public class ObsidianComponent_269 {
   public static final ObsidianComponent_269 WHITE = new ObsidianComponent_269(255, 255, 255);
   public static final ObsidianComponent_269 BLACK = new ObsidianComponent_269(
      0, 0, 0
   );
   public static final ObsidianComponent_269 GREEN = new ObsidianComponent_269(0, 255, 0);
   public static final ObsidianComponent_269 RED = new ObsidianComponent_269(255, 0, 0);
   public static final ObsidianComponent_269 BLUE = new ObsidianComponent_269(0, 0, 255);
   public static final ObsidianComponent_269 YELLOW = new ObsidianComponent_269(255, 255, 0);
   public static final ObsidianComponent_269 GRAY = new ObsidianComponent_269(
      88, 87, 93
   );
   public static final ObsidianComponent_269 TRANSPARENT = new ObsidianComponent_269(
      0, 0, 0, 0
   );
   public transient float[] hsbValues;
   public final int red;
   public final int green;
   public final int blue;
   public final int alpha;
   private static final ByteBuffer PIXEL_BUFFER = ByteBuffer.allocateDirect(4);

   public ObsidianComponent_269(int color) {
      this(ObsidianComponent_olorUtil.red(color), ObsidianComponent_olorUtil.green(color), ObsidianComponent_olorUtil.blue(color), ObsidianComponent_olorUtil.alpha(color));
   }

   public ObsidianComponent_269(Color color) {
      this(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
   }

   public ObsidianComponent_269(int red, int green, int blue) {
      this(red, green, blue, 255);
   }

   public ObsidianComponent_269(int red, int green, int blue, int alpha) {
      red = MathHelper.clamp(red, 0, 255);
      green = MathHelper.clamp(green, 0, 255);
      blue = MathHelper.clamp(blue, 0, 255);
      alpha = MathHelper.clamp(alpha, 0, 255);
      this.red = red;
      this.green = green;
      this.blue = blue;
      this.alpha = alpha;
   }

   public ObsidianComponent_269(int red, int green, int blue, float alpha) {
      red = MathHelper.clamp(red, 0, 255);
      green = MathHelper.clamp(green, 0, 255);
      blue = MathHelper.clamp(blue, 0, 255);
      alpha = MathHelper.clamp(
         alpha, 0.0f, 255.0f
      );
      this.red = red;
      this.green = green;
      this.blue = blue;
      this.alpha = (int)alpha;
   }

   public int getRGB() {
      int a = Math.round((float)this.clamp((float)this.alpha));
      int r = Math.round((float)this.clamp((float)this.red));
      int g = Math.round((float)this.clamp((float)this.green));
      int b = Math.round((float)this.clamp((float)this.blue));
      return (a & 255) << (24)
         | (r & 255) << 16
         | (g & 255) << (8)
         | b & 255;
   }
   public int clamp(float value) {
      return (int)Math.max(
         0.0f, Math.min(255.0f, value)
      );
   }

   public static ObsidianComponent_269 fromHex(String hex) {
      String sanitized = hex.startsWith("#") ? hex.substring(1) : hex;
      if (sanitized.length() != 6 && sanitized.length() != 8) {
         throw new IllegalArgumentException("Hex color must be in the format #RRGGBB or #RRGGBBAA");
      } else {
         int red = Integer.parseInt(
            sanitized.substring(0, 2), 16
         );
         int green = Integer.parseInt(
            sanitized.substring(2, 4), 16
         );
         int blue = Integer.parseInt(sanitized.substring(4, 6), 16);
         int alpha = sanitized.length() == (8)
            ? Integer.parseInt(
               sanitized.substring(6, 8), 16
            )
            : 255;
         return new ObsidianComponent_269(red, green, blue, alpha);
      }
   }

   public static ObsidianComponent_269 lerp(ObsidianComponent_269 startColor, ObsidianComponent_269 endColor, float delta) {
      float clampedDelta = Math.max(
         0.0f, Math.min(1.0f, delta)
      );
      int r = (int)((float)startColor.getRed() + (float)(endColor.getRed() - startColor.getRed()) * clampedDelta);
      int g = (int)((float)startColor.getGreen() + (float)(endColor.getGreen() - startColor.getGreen()) * clampedDelta);
      int b = (int)((float)startColor.getBlue() + (float)(endColor.getBlue() - startColor.getBlue()) * clampedDelta);
      int a = (int)((float)startColor.getAlpha() + (float)(endColor.getAlpha() - startColor.getAlpha()) * clampedDelta);
      return new ObsidianComponent_269(r, g, b, a);
   }

   public static ObsidianComponent_269 fromInt(int colorInt) {
      int alpha = colorInt >> 24 & 255;
      int red = colorInt >> (16) & (255);
      int green = colorInt >> (8) & (255);
      int blue = colorInt & (255);
      return new ObsidianComponent_269(red, green, blue, alpha);
   }

   public ObsidianComponent_269 withAlpha(float newAlpha) {
      return new ObsidianComponent_269(this.red, this.green, this.blue, (int)newAlpha);
   }

   public ObsidianComponent_269 withAlpha(int newAlpha) {
      return new ObsidianComponent_269(this.red, this.green, this.blue, newAlpha);
   }

   public ObsidianComponent_269 mulAlpha(float percent) {
      return this.withAlpha((int)((float)this.alpha * percent));
   }

   public ObsidianComponent_269 mix(ObsidianComponent_269 color2, float amount) {
      amount = Math.min(1.0f, Math.max(0.0f, amount));
      return new ObsidianComponent_269(
         (int)MathLerpUtil.interpolate((double)this.getRed(), (double)color2.getRed(), (double)amount),
         (int)MathLerpUtil.interpolate((double)this.getGreen(), (double)color2.getGreen(), (double)amount),
         (int)MathLerpUtil.interpolate((double)this.getBlue(), (double)color2.getBlue(), (double)amount),
         (int)MathLerpUtil.interpolate((double)this.getAlpha(), (double)color2.getAlpha(), (double)amount)
      );
   }

   public ObsidianComponent_269 darker(float amount) {
      amount = MathHelper.clamp(amount, 0.0f, 1.0f);
      return new ObsidianComponent_269(
         (int)((float)this.red * (1.0f - amount)),
         (int)((float)this.green * (1.0f - amount)),
         (int)((float)this.blue * (1.0f - amount)),
         this.alpha
      );
   }

   public static ObsidianComponent_269 fromHSB(float hue, float saturation, float brightness) {
      if (saturation == 0.0f) {
         int grayValue = (int)(brightness * 255.0f + 0.5f);
         return new ObsidianComponent_269(grayValue, grayValue, grayValue);
      } else {
         float h = (hue - (float)Math.floor((double)hue)) * 6.0f;
         float f = h - (float)Math.floor((double)h);
         float p = brightness * (1.0f - saturation);
         float q = brightness * (1.0f - saturation * f);
         float t = brightness * (1.0f - saturation * (1.0f - f));
         float r = 0.0f;
         float g = 0.0f;
         float b = 0.0f;
         switch ((int)h) {
            case 0:
               r = brightness;
               g = t;
               b = p;
               break;
            case 1:
               r = q;
               g = brightness;
               b = p;
               break;
            case 2:
               r = p;
               g = brightness;
               b = t;
               break;
            case 3:
               r = p;
               g = q;
               b = brightness;
               break;
            case 4:
               r = t;
               g = p;
               b = brightness;
               break;
            case 5:
               r = brightness;
               g = p;
               b = q;
         }

         return new ObsidianComponent_269(
            (int)(r * 255.0f),
            (int)(g * 255.0f),
            (int)(b * 255.0f)
         );
      }
   }

   public float getHue() {
      return this.getHSBValues()[0];
   }

   public float getSaturation() {
      return this.getHSBValues()[2];
   }

   public float getBrightness() {
      return this.getHSBValues()[1];
   }
   public float[] getHSBValues() {
      if (this.hsbValues == null) {
         this.hsbValues = this.calculateHSB();
      }

      return this.hsbValues;
   }
   public float[] calculateHSB() {
      float r = (float)this.red / 255.0f;
      float g = (float)this.green / 255.0f;
      float b = (float)this.blue / 255.0f;
      float maxC = Math.max(r, Math.max(g, b));
      float minC = Math.min(r, Math.min(g, b));
      float delta = maxC - minC;
      float hue = 0.0f;
      if (delta != 0.0f) {
         if (maxC == r) {
            hue = (g - b) / delta;
         } else if (maxC == g) {
            hue = (b - r) / delta + 2.0f;
         } else {
            hue = (r - g) / delta + 4.0f;
         }

         hue /= 6.0f;
         if (hue < 0.0f) {
            hue += 1.0f;
         }
      }

      float saturation = maxC == 0.0f
         ? 0.0f
         : delta / maxC;
      float[] var10000 = new float[3];
      var10000[0] = hue;
      var10000[1] = saturation;
      var10000[2] = maxC;
      return var10000;
   }

   public ObsidianComponent_269 brighter(float amount) {
      amount = MathHelper.clamp(amount, 0.0f, 1.0f);
      return new ObsidianComponent_269(
         (int)((float)this.red + (255.0f - (float)this.red) * amount),
         (int)((float)this.green + (255.0f - (float)this.green) * amount),
         (int)((float)this.blue + (255.0f - (float)this.blue) * amount),
         this.alpha
      );
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ObsidianComponent_269 colorRGBA = (ObsidianComponent_269)o;
         return (Float.compare((float)this.red, (float)colorRGBA.red) == 0
               && Float.compare((float)this.green, (float)colorRGBA.green) == 0
               && Float.compare((float)this.blue, (float)colorRGBA.blue) == 0
               && Float.compare((float)this.alpha, (float)colorRGBA.alpha) == 0);
      } else {
         return false;
      }
   }

   public float difference(ObsidianComponent_269 colorRGBA) {
      return Math.abs(this.getHue() - colorRGBA.getHue())
         + Math.abs(this.getBrightness() - colorRGBA.getBrightness())
         + Math.abs(this.getSaturation() - colorRGBA.getSaturation());
   }

   @Override
   public int hashCode() {
      Object[] var10000 = new Object[4];
      var10000[0] = this.red;
      var10000[1] = this.green;
      var10000[2] = this.blue;
      var10000[3] = this.alpha;
      return Objects.hash(var10000);
   }

   @Generated
   public int getRed() {
      return this.red;
   }

   @Generated
   public int getGreen() {
      return this.green;
   }

   @Generated
   public int getBlue() {
      return this.blue;
   }

   @Generated
   public int getAlpha() {
      return this.alpha;
   }
}
