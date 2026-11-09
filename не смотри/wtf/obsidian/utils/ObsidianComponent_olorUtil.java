package wtf.obsidian.utils;

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
























import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public class ObsidianComponent_olorUtil {
   public static final Color green = new Color(36, 218, 118);
   public static final Color yellow = new Color(
      255, 196, 67
   );
   public static final Color orange = new Color(255, 134, 0);
   public static final Color red = new Color(
      239, 72, 54
   );
   public static final Color Blues = new Color(125, 217, 250);

   public static int red(int c) {
      return c >> 16 & 255;
   }

   public static int green(int c) {
      return c >> 8 & 255;
   }

   public static float redf(int c) {
      return (float)red(c) / 255.0f;
   }

   public static float greenf(int c) {
      return (float)green(c) / 255.0f;
   }

   public static float bluef(int c) {
      return (float)blue(c) / 255.0f;
   }

   public static float alphaf(int c) {
      return (float)alpha(c) / 255.0f;
   }

   public static int getColor(int brightness, int alpha) {
      return getColor(brightness, brightness, brightness, alpha);
   }

   public static int gradient(int color1, int color2, float amount) {
      amount = MathHelper.clamp(amount, 0.0f, 1.0f);
      int r = MathHelper.lerp(amount, red(color1), red(color2));
      int g = MathHelper.lerp(amount, green(color1), green(color2));
      int b = MathHelper.lerp(amount, blue(color1), blue(color2));
      int a = MathHelper.lerp(amount, alpha(color1), alpha(color2));
      return rgba(r, g, b, a);
   }

   public static int toColor(String hexColor) {
      if (hexColor != null && hexColor.length() == (7) && hexColor.startsWith("#")) {
         try {
            int rgb = Integer.parseInt(hexColor.substring(1), 16);
            return -16777216 | rgb;
         } catch (NumberFormatException var2) {
            return -16777216;
         }
      } else {
         return -16777216;
      }
   }

   public static int applyAlpha(int color, float alphaMul) {
      int a = color >> 24 & (255);
      int na = (int)(
         (float)a
            * Math.max(
               0.0f, Math.min(1.0f, alphaMul)
            )
      );
      return color & (16777215) | na << 24;
   }

   public static int r(int color) {
      return color >> 16 & 255;
   }

   public static int g(int color) {
      return color >> (8) & (255);
   }

   public static int b(int color) {
      return color & (255);
   }

   public static int a(int color) {
      return color >> (24) & 255;
   }

   public static int hexToRgb(String hex) {
      if (hex.startsWith("#")) {
         hex = hex.substring(1);
      }

      if (hex.length() != 6) {
         throw new IllegalArgumentException("Недопустимый формат HEX: " + hex);
      } else {
         int r = Integer.parseInt(
            hex.substring(0, 2), 16
         );
         int g = Integer.parseInt(hex.substring(2, 4), 16);
         int b = Integer.parseInt(hex.substring(4, 6), 16);
         return rgb(r, g, b);
      }
   }

   public static int getThemeColor() {
      return !Obsidian.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")
         ? Obsidian.INSTANCE.themeStorage.getThemes().getTheme().color[0]
         : getThemeColor(0);
   }

   public static int getThemeColor(int index) {
      return Obsidian.INSTANCE.themeStorage.getThemes().getTheme().getColor(index);
   }

   public static int getThemeStaticColor() {
      return Obsidian.INSTANCE.themeStorage.getThemes().getTheme().color[0];
   }

   public static int rainbow(int speed, int index, float saturation, float brightness, float opacity) {
      int angle = (int)((System.currentTimeMillis() / (long)speed + (long)index) % (360L));
      float hue = (float)angle / 360.0f;
      int color = Color.HSBtoRGB(hue, saturation, brightness);
      return getColor(
         red(color),
         green(color),
         blue(color),
         Math.max(
            0,
            Math.min(255, (int)(opacity * 255.0f))
         )
      );
   }

   public static int interpolate(int color1, int color2, double amount) {
      amount = (double)(
         (float)MathLerpUtil.clamp(
            0.0,
            1.0,
            amount
         )
      );
      return getColor(
         MathLerpUtil.ler1p(red(color1), red(color2), amount),
         MathLerpUtil.ler1p(green(color1), green(color2), amount),
         MathLerpUtil.ler1p(blue(color1), blue(color2), amount),
         MathLerpUtil.ler1p(alpha(color1), alpha(color2), amount)
      );
   }

   public static int[] genGradientForText(int color1, int color2, int length) {
      int[] gradient = new int[length];

      for (int i = 0; i < length; i++) {
         double pc = (double)i / (double)(length - (1));
         gradient[i] = interpolate(color1, color2, pc);
      }

      return gradient;
   }

   public static int blue(int c) {
      return c & 255;
   }

   public static int overCol(int c1, int c2, float pc01) {
      return getColor(
         (float)red(c1) * (1.0f - pc01) + (float)red(c2) * pc01,
         (float)green(c1) * (1.0f - pc01) + (float)green(c2) * pc01,
         (float)blue(c1) * (1.0f - pc01) + (float)blue(c2) * pc01,
         (float)alpha(c1) * (1.0f - pc01) + (float)alpha(c2) * pc01
      );
   }

   public static int darken(int color, float factor) {
      float[] rgb = getColorT(color);
      float[] hsb = Color.RGBtoHSB(
         (int)(rgb[0] * 255.0f),
         (int)(rgb[1] * 255.0f),
         (int)(rgb[2] * 255.0f),
         null
      );
      hsb[2] = hsb[2] * factor;
      hsb[2] = Math.max(
         0.0f,
         Math.min(1.0f, hsb[2])
      );
      int darkenedRGB = Color.HSBtoRGB(
         hsb[0], hsb[1], hsb[2]
      );
      return applyOpacity(
         darkenedRGB, (float)((int)(rgb[3] * 255.0f))
      );
   }

   public static int multDark(int c, float brpc) {
      return getColor((float)red(c) * brpc, (float)green(c) * brpc, (float)blue(c) * brpc, (float)alpha(c));
   }

   public static int overCol(int c1, int c2) {
      return overCol(c1, c2, 0.5f);
   }

   public static int alpha(int c) {
      return c >> (24) & 255;
   }

   public static int multAlpha(int c, float apc) {
      return getColor((float)red(c), (float)green(c), (float)blue(c), (float)alpha(c) * apc);
   }

   public static int replAlpha(int color, int alpha) {
      alpha = Math.max(0, Math.min(255, alpha));
      return alpha << 24 | color & 16777215;
   }

   public static Color random() {
      return new Color(
         Color.HSBtoRGB(
            (float)Math.random(),
            (float)(
               0.75
                  + Math.random() / 4.0
            ),
            (float)(
               0.75
                  + Math.random() / 4.0
            )
         )
      );
   }

   public static int getColor(float r, float g, float b, float a) {
      return new Color((int)r, (int)g, (int)b, (int)a).getRGB();
   }

   public static float[] getRGBAf(int c) {
      float[] var10000 = new float[4];
      var10000[0] = redf(c);
      var10000[1] = greenf(c);
      var10000[2] = bluef(c);
      var10000[3] = alphaf(c);
      return var10000;
   }

   public static float[] getRGBAf1(int c) {
      float[] var10000 = new float[4];
      var10000[0] = (float)red(c) / 255.0f;
      var10000[1] = (float)green(c) / 255.0f;
      var10000[2] = (float)blue(c) / 255.0f;
      var10000[3] = (float)alpha(c) / 255.0f;
      return var10000;
   }

   public static Color interpolateTwoColors(int speed, int index, Color start, Color end, boolean trueColor) {
      int angle = 0;
      if (speed == 0) {
         angle = index % (360);
      } else {
         angle = (int)((System.currentTimeMillis() / (long)speed + (long)index) % (360L));
      }

      angle = (angle >= (180) ? 360 - angle : angle) * (2);
      return trueColor
         ? interpolateColorHue(start, end, (float)angle / 360.0f)
         : interpolateColorC(start, end, (float)angle / 360.0f);
   }

   public static Color interpolateTwoColors(int speed, int index, Color start, Color end) {
      return interpolateTwoColors(speed, index, start, end, false);
   }

   public static Color astolfo(float yDist, float yTotal, float saturation, float speedt) {
      float speed = 1800.0f;
      float hue = (float)(System.currentTimeMillis() % (long)((int)speed)) + (yTotal - yDist) * speedt;

      while (hue > speed) {
         hue -= speed;
      }

      hue /= speed;
      if (hue > 1.0f) {
         hue = 1.0f - (hue - 1.0f);
      }

      hue += 1.0f;
      return Color.getHSBColor(hue, saturation, 1.0f);
   }
   public static int calculateHueDegrees(int divisor, int offset) {
      long currentTime = System.currentTimeMillis();
      long calculatedValue = (currentTime / (long)divisor + (long)offset) % (360L);
      return (int)calculatedValue;
   }

   public static void setColor(Color color, float alpha) {
      float red = (float)color.getRed() / 255.0f;
      float green = (float)color.getGreen() / 255.0f;
      float blue = (float)color.getBlue() / 255.0f;
      RenderSystem.setShaderColor(red, green, blue, alpha);
   }

   public static int rgb(int r, int g, int b) {
      return -16777216 | r << (16) | g << 8 | b;
   }

   public static int rgba(int r, int g, int b, int a) {
      return a << (24) | r << (16) | g << (8) | b;
   }

   public static float[] rgba(int color) {
      float[] var10000 = new float[4];
      var10000[0] = (float)(color >> 16 & (255))
         / 255.0f;
      var10000[1] = (float)(color >> (8) & 255)
         / 255.0f;
      var10000[2] = (float)(color & (255))
         / 255.0f;
      var10000[3] = (float)(color >> (24) & (255))
         / 255.0f;
      return var10000;
   }

   public static int rgba(double r, double g, double b, double a) {
      return rgba((int)r, (int)g, (int)b, (int)a);
   }

   public static int getRed(int hex) {
      return hex >> 16 & (255);
   }

   public static int getGreen(int hex) {
      return hex >> 8 & 255;
   }

   public static int interpolate(int start, int end, float value) {
      float[] startColor = rgba(start);
      float[] endColor = rgba(end);
      return rgba(
         (int)MathLerpUtil.interpolate(
            startColor[0] * 255.0f,
            endColor[0] * 255.0f,
            value
         ),
         (int)MathLerpUtil.interpolate(
            startColor[1] * 255.0f,
            endColor[1] * 255.0f,
            value
         ),
         (int)MathLerpUtil.interpolate(
            startColor[2] * 255.0f,
            endColor[2] * 255.0f,
            value
         ),
         (int)MathLerpUtil.interpolate(
            startColor[3] * 255.0f,
            endColor[3] * 255.0f,
            value
         )
      );
   }

   public static int interpolateColor(int color1, int color2, float amount) {
      amount = Math.min(1.0f, Math.max(0.0f, amount));
      int red1 = getRed(color1);
      int green1 = getGreen(color1);
      int blue1 = getBlue(color1);
      int alpha1 = getAlpha(color1);
      int red2 = getRed(color2);
      int green2 = getGreen(color2);
      int blue2 = getBlue(color2);
      int alpha2 = getAlpha(color2);
      int interpolatedRed = interpolateInt(red1, red2, (double)amount);
      int interpolatedGreen = interpolateInt(green1, green2, (double)amount);
      int interpolatedBlue = interpolateInt(blue1, blue2, (double)amount);
      int interpolatedAlpha = interpolateInt(alpha1, alpha2, (double)amount);
      return interpolatedAlpha << (24)
         | interpolatedRed << 16
         | interpolatedGreen << 8
         | interpolatedBlue;
   }

   public static MutableText gradient(String message, int first, int end) {
      MutableText text = Text.empty();

      for (int i = 0; i < message.length(); i++) {
         int color = interpolateColor(first, end, (float)i / (float)message.length());
         MutableText charText = Text.literal(String.valueOf(message.charAt(i))).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)));
         text.append(charText);
      }

      return text;
   }

   public static Text replace(Text original, String find, String replaceWith) {
      if (original != null && find != null && replaceWith != null) {
         String originalText = original.getString();
         String replacedText = originalText.replace(find, replaceWith);
         return Text.literal(replacedText);
      } else {
         return original;
      }
   }

   public static int gradient(int speed, int index, int... colors) {
      int angle = (int)((System.currentTimeMillis() / (long)speed + (long)index) % (360L));
      angle = (angle > (180) ? 360 - angle : angle) + (180);
      int colorIndex = (int)((float)angle / 360.0f * (float)colors.length);
      if (colorIndex == colors.length) {
         colorIndex--;
      }

      int color1 = colors[colorIndex];
      int color2 = colors[colorIndex == colors.length - (1)
         ? 0
         : colorIndex + (1)];
      return interpolateColor(
         color1, color2, (float)angle / 360.0f * (float)colors.length - (float)colorIndex
      );
   }

   public static int themeGradient(int speed, int index, float darkenFactor) {
      int theme = getThemeColor();
      int[] var10002 = new int[2];
      var10002[0] = theme;
      var10002[1] = darken(theme, darkenFactor);
      return gradient(speed, index, var10002);
   }

   public static int getBlue(int hex) {
      return hex & 255;
   }

   public static int getAlpha(int hex) {
      return hex >> 24 & 255;
   }

   public static int getColor(int red, int green, int blue, int alpha) {
      int color = 0;
      color |= alpha << (24);
      color |= red << (16);
      color |= green << 8;
      return color | blue;
   }

   public static int getColor(int bright) {
      return getColor(bright, bright, bright, 255);
   }

   public static float[] getColorA(int color) {
      float[] var10000 = new float[4];
      var10000[0] = (float)red(color) / 255.0f;
      var10000[1] = (float)green(color) / 255.0f;
      var10000[2] = (float)blue(color) / 255.0f;
      var10000[3] = alphaf(color);
      return var10000;
   }

   public static float[] getColorT(int color) {
      float[] var10000 = new float[4];
      var10000[0] = (float)red(color) / 255.0f;
      var10000[1] = (float)green(color) / 255.0f;
      var10000[2] = (float)blue(color) / 255.0f;
      var10000[3] = alphaf(color);
      return var10000;
   }

   public static void setColor(double red, double green, double blue, double alpha) {
      GL11.glColor4d(red, green, blue, alpha);
   }

   public static int setAlphaColor(int color, int alpha) {
      return color & (16777215) | alpha << 24;
   }

   public static float lerp(float a, float b, float f) {
      return a + f * (b - a);
   }

   public static Color interpolateColorC(Color color1, Color color2, float amount) {
      amount = Math.min(
         1.0f, Math.max(0.0f, amount)
      );
      return new Color(
         interpolateInt(color1.getRed(), color2.getRed(), (double)amount),
         interpolateInt(color1.getGreen(), color2.getGreen(), (double)amount),
         interpolateInt(color1.getBlue(), color2.getBlue(), (double)amount),
         interpolateInt(color1.getAlpha(), color2.getAlpha(), (double)amount)
      );
   }

   public static Double interpolate(double oldValue, double newValue, double interpolationValue) {
      return oldValue + (newValue - oldValue) * interpolationValue;
   }

   public static float interpolateFloat(float oldValue, float newValue, double interpolationValue) {
      return interpolate((double)oldValue, (double)newValue, (double)((float)interpolationValue)).floatValue();
   }

   public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
      return interpolate(oldValue, newValue, (float)interpolationValue);
   }

   public static Color interpolateColorHue(Color color1, Color color2, float amount) {
      amount = Math.min(1.0f, Math.max(0.0f, amount));
      float[] color1HSB = Color.RGBtoHSB(color1.getRed(), color1.getGreen(), color1.getBlue(), null);
      float[] color2HSB = Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), null);
      Color resultColor = Color.getHSBColor(
         interpolateFloat(color1HSB[0], color2HSB[0], (double)amount),
         interpolateFloat(color1HSB[1], color2HSB[1], (double)amount),
         interpolateFloat(color1HSB[2], color2HSB[2], (double)amount)
      );
      return new Color(
         resultColor.getRed(), resultColor.getGreen(), resultColor.getBlue(), interpolateInt(color1.getAlpha(), color2.getAlpha(), (double)amount)
      );
   }

   public static void setColor(Color color) {
      if (color == null) {
         color = Color.white;
      }

      setColor(
         (double)((float)color.getRed() / 255.0f),
         (double)((float)color.getGreen() / 255.0f),
         (double)((float)color.getBlue() / 255.0f),
         (double)((float)color.getAlpha() / 255.0f)
      );
   }

   public static void setColor(int color) {
      setColor(
         color,
         (float)(color >> (24) & 255)
            / 255.0f
      );
   }

   public static void setColor(int color, float alpha) {
      float r = (float)(color >> (16) & 255)
         / 255.0f;
      float g = (float)(color >> 8 & (255))
         / 255.0f;
      float b = (float)(color & (255)) / 255.0f;
      RenderSystem.setShaderColor(r, g, b, alpha);
   }

   public static int applyOpacity(int color, float alpha) {
      return rgba(
         (double)getRed(color),
         (double)getGreen(color),
         (double)getBlue(color),
         (double)((float)getAlpha(color) * alpha / 255.0f)
      );
   }

   public static int reFactorColor(int color, float factor) {
      return rgba(
         (double)(extractRedf(color) * factor), (double)(extractGreenf(color) * factor), (double)(extractBluef(color) * factor), (double)extractAlphaf(color)
      );
   }

   public static float extractRedf(int color) {
      return (float)(color >> 16 & (255))
         / 255.0f;
   }

   public static int extractRed(int color) {
      return color >> 16 & (255);
   }

   public static float extractBluef(int color) {
      return (float)(color & (255)) / 255.0f;
   }

   public static int extractBlue(int color) {
      return color & 255;
   }

   public static float extractGreenf(int color) {
      return (float)(color >> (8) & 255)
         / 255.0f;
   }

   public static int extractGreen(int color) {
      return color >> 8 & (255);
   }

   public static float extractAlphaf(int color) {
      return (float)(color >> (24) & 255)
         / 255.0f;
   }

   public static int extractAlpha(int color) {
      return color >> (24) & 255;
   }
}
