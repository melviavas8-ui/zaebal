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
























import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import net.minecraft.util.Identifier;

public class ObsidianComponent_256 {
   public static final String MOD_ID = "obsidian";
   private static final Map<String, Map<Float, ObsidianComponent_240>> regularFonts = new HashMap<>();
   private static final Map<String, Map<Float, ObsidianComponent_405>> gradientFonts = new HashMap<>();
   public static ObsidianComponent_240 comfortaa16;
   public static ObsidianComponent_240 comfortaa18;
   public static ObsidianComponent_240 comfortaa20;
   public static ObsidianComponent_405 comfortaaGradient18;
   public static ObsidianComponent_240 roboto16;
   public static ObsidianComponent_240 roboto18;
   public static ObsidianComponent_240 roboto20;
   public static ObsidianComponent_405 robotoGradient18;
   public static ObsidianComponent_240 montserrat16;
   public static ObsidianComponent_240 montserrat18;
   public static ObsidianComponent_240 montserrat20;
   public static ObsidianComponent_405 montserratGradient18;
   public static boolean initialized = false;

   public static void init() {
      if (!initialized) {
         comfortaa16 = getFont("comfortaa.ttf", 16.0f);
         comfortaa18 = getFont("comfortaa.ttf", 18.0f);
         comfortaa20 = getFont("comfortaa.ttf", 20.0f);
         comfortaaGradient18 = getGradientFont("comfortaa.ttf", 18.0f);
         roboto16 = getFont("roboto.ttf", 16.0f);
         roboto18 = getFont("roboto.ttf", 18.0f);
         roboto20 = getFont("roboto.ttf", 20.0f);
         robotoGradient18 = getGradientFont("roboto.ttf", 18.0f);
         montserrat16 = getFont("montserrat.ttf", 16.0f);
         montserrat18 = getFont("montserrat.ttf", 18.0f);
         montserrat20 = getFont("montserrat.ttf", 20.0f);
         montserratGradient18 = getGradientFont("montserrat.ttf", 18.0f);
         initialized = true;
      }
   }

   public static ObsidianComponent_240 getFont(String fontName, float size) {
      regularFonts.computeIfAbsent(fontName, k -> new HashMap<>());
      Map<Float, ObsidianComponent_240> fontSizes = regularFonts.get(fontName);
      if (fontSizes.containsKey(size)) {
         return fontSizes.get(size);
      } else {
         Font font = ObsidianComponent_415.getFontFromTTF(Identifier.of("obsidian", "fonts/ttf/" + fontName), size, 0);
         if (font == null) {
            font = new Font("Arial", 0, (int)size);
         }

         ObsidianComponent_240 renderer = new ObsidianComponent_240(font, true, true);
         fontSizes.put(size, renderer);
         return renderer;
      }
   }

   public static ObsidianComponent_405 getGradientFont(String fontName, float size) {
      gradientFonts.computeIfAbsent(fontName, k -> new HashMap<>());
      Map<Float, ObsidianComponent_405> fontSizes = gradientFonts.get(fontName);
      if (fontSizes.containsKey(size)) {
         return fontSizes.get(size);
      } else {
         Font font = ObsidianComponent_415.getFontFromTTF(Identifier.of("obsidian", "fonts/" + fontName), size, 0);
         if (font == null) {
            font = new Font("Arial", 0, (int)size);
         }

         ObsidianComponent_405 renderer = new ObsidianComponent_405(font, true, true);
         fontSizes.put(size, renderer);
         return renderer;
      }
   }

   public static void drawStringWithFade(ObsidianComponent_240 font, String text, float x, float y, float maxWidth, int color) {
      if (text != null && !text.isEmpty() && !(maxWidth <= 0.0f)) {
         float currentX = x;
         float fadeZoneWidth = Math.min(
            22.0f,
            Math.max(8.0f, maxWidth * 0.3499999940395355f)
         );
         float fadeStartX = x + maxWidth - fadeZoneWidth;
         int originalAlpha = color >> 24 & 255;

         for (int i = 0; i < text.length(); i++) {
            String ch = String.valueOf(text.charAt(i));
            float charWidth = (float)font.getStringWidth(ch);
            if (currentX > x + maxWidth && i > 0) {
               break;
            }

            int finalColor = color;
            if (currentX > fadeStartX) {
               float progress = (currentX - fadeStartX) / fadeZoneWidth;
               progress = Math.max(
                  0.0f, Math.min(1.0f, progress)
               );
               float fadeFactor = (float)Math.cos(
                  (double)progress
                     * 3.141592653589793
                     / 2.0
               );
               int newAlpha = (int)((float)originalAlpha * fadeFactor);
               finalColor = color & 16777215 | newAlpha << 24;
            }

            if ((finalColor >> 24 & 255) > (4)) {
               font.drawString(ch, currentX, y, finalColor);
            }

            currentX += charWidth;
         }
      }
   }

   public static ObsidianComponent_240 getSystemFont(String fontName, float size) {
      String key = "system_" + fontName;
      regularFonts.computeIfAbsent(key, k -> new HashMap<>());
      Map<Float, ObsidianComponent_240> fontSizes = regularFonts.get(key);
      if (fontSizes.containsKey(size)) {
         return fontSizes.get(size);
      } else {
         Font font = new Font(fontName, 0, (int)size);
         ObsidianComponent_240 renderer = new ObsidianComponent_240(font, true, true);
         fontSizes.put(size, renderer);
         return renderer;
      }
   }

   public static ObsidianComponent_240 getSystemFont(String fontName, float size, int style) {
      String key = "system_" + fontName + "_" + style;
      regularFonts.computeIfAbsent(key, k -> new HashMap<>());
      Map<Float, ObsidianComponent_240> fontSizes = regularFonts.get(key);
      if (fontSizes.containsKey(size)) {
         return fontSizes.get(size);
      } else {
         Font font = new Font(fontName, style, (int)size);
         ObsidianComponent_240 renderer = new ObsidianComponent_240(font, true, true);
         fontSizes.put(size, renderer);
         return renderer;
      }
   }

   public static void clearCache() {
      regularFonts.clear();
      gradientFonts.clear();
      initialized = false;
   }

   public static void clearCache(String fontName) {
      regularFonts.remove(fontName);
      gradientFonts.remove(fontName);
   }

   @Generated
   public static boolean isInitialized() {
      return initialized;
   }
}
