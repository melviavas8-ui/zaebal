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
























import java.util.HashMap;
import net.minecraft.client.util.math.MatrixStack;

public class ObsidianComponent_201 {
   private static final HashMap<String, ObsidianComponent_81> loadedFonts = new HashMap<>();
   private static final HashMap<String, FontRenderer[]> fontCache = new HashMap<>();
   public static boolean initialized = false;

   public static void init() {
      if (!initialized) {
         initialized = true;
         loadFont("sf_regular");
         loadFont("wave");
         loadFont("icon");
         loadFont("icon1");
         loadFont("iconnew");
         loadFont("suisse");
      }
   }
   public static void loadFont(String name) {
      try {
         ObsidianComponent_81 msdfFont = ObsidianComponent_81.builder().atlas(name).data(name).build();
         loadedFonts.put(name, msdfFont);
         FontRenderer[] fonts = new FontRenderer[100];

         for (int i = 8; i < 100; i++) {
            fonts[i] = new FontRenderer(msdfFont, (float)i);
         }

         fontCache.put(name, fonts);
      } catch (Exception var4) {
         System.err.println("[Fonts] Failed to load " + name + ": " + var4.getMessage());
      }
   }

   public static FontRenderer getFont(String name, int size) {
      if (!initialized) {
         init();
      }

      String cleanName = name.replace(".ttf", "");
      if (size < 8) {
         size = 8;
      }

      if (size >= (100)) {
         size = 99;
      }

      FontRenderer[] fonts = fontCache.get(cleanName);
      if (fonts != null && fonts[size] != null) {
         return fonts[size];
      } else {
         if (!loadedFonts.containsKey(cleanName)) {
            loadFont(cleanName);
         }

         fonts = fontCache.get(cleanName);
         return fonts != null && fonts[size] != null ? fonts[size] : null;
      }
   }

   public static void drawStringWithFade(FontRenderer font, String text, float x, float y, float maxWidth, int color) {
      if (font != null) {
         MatrixStack stack = new MatrixStack();
         font.drawStringWithFade(stack, text, x, y, maxWidth, color);
      }
   }
}
