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
























import com.google.common.collect.Lists;
import java.awt.Rectangle;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.lwjgl.opengl.GL30;

public class ObsidianComponent_343 {
   private static ObsidianComponent_246 state = new ObsidianComponent_246();
   private static final List<ObsidianComponent_246> stateStack = Lists.newArrayList();

   public static void push() {
      stateStack.add(state.clone());
   }

   public static void pop() {
      if (!stateStack.isEmpty()) {
         state = stateStack.remove(stateStack.size() - 1);
         if (state.enabled) {
            GL30.glEnable(3089);
            GL30.glScissor(state.x, state.y, state.width, state.height);
         } else {
            GL30.glDisable(3089);
         }
      }
   }

   public static void unset() {
      GL30.glDisable(3089);
      state.enabled = false;
   }
   public static Window getWindow() {
      MinecraftClient client = MinecraftClient.getInstance();
      return client == null ? null : client.getWindow();
   }
   public static double getScaleFactor() {
      Window window = getWindow();
      return window == null ? 1.0 : window.getScaleFactor();
   }

   public static void setFromComponentCoordinates(int x, int y, int width, int height) {
      Window window = getWindow();
      if (window != null) {
         double scaleFactor = getScaleFactor();
         int screenX = (int)((double)x * scaleFactor);
         int screenY = (int)((double)y * scaleFactor);
         int screenWidth = (int)((double)width * scaleFactor);
         int screenHeight = (int)((double)height * scaleFactor);
         screenY = window.getHeight() - screenY - screenHeight;
         set(screenX, screenY, screenWidth, screenHeight);
      }
   }

   public static void setFromComponentCoordinates(double x, double y, double width, double height) {
      Window window = getWindow();
      if (window != null) {
         double scaleFactor = getScaleFactor();
         int screenX = (int)(x * scaleFactor);
         int screenY = (int)(y * scaleFactor);
         int screenWidth = (int)(width * scaleFactor);
         int screenHeight = (int)(height * scaleFactor);
         screenY = window.getHeight() - screenY - screenHeight;
         set(screenX, screenY, screenWidth, screenHeight);
      }
   }

   public static void setFromComponentCoordinates(double x, double y, double width, double height, float scale) {
      Window window = getWindow();
      if (window != null) {
         double scaleFactor = getScaleFactor();
         float halfAnimationValueRest = (1.0f - scale)
            / 2.0f;
         double testX = x + width * (double)halfAnimationValueRest;
         double testY = y + height * (double)halfAnimationValueRest;
         double testW = width * (double)scale;
         double testH = height * (double)scale;
         testX = testX * (double)scale + ((double)window.getScaledWidth() - testW) * (double)halfAnimationValueRest;
         int screenX = (int)(testX * scaleFactor);
         int screenY = (int)(testY * scaleFactor);
         int screenWidth = (int)(testW * scaleFactor);
         int screenHeight = (int)(testH * scaleFactor);
         screenY = window.getHeight() - screenY - screenHeight;
         set(screenX, screenY, screenWidth, screenHeight);
      }
   }

   public static void set(int x, int y, int width, int height) {
      Window window = getWindow();
      if (window != null) {
         Rectangle screen = new Rectangle(0, 0, window.getWidth(), window.getHeight());
         Rectangle current;
         if (state.enabled) {
            current = new Rectangle(state.x, state.y, state.width, state.height);
         } else {
            current = screen;
         }

         Rectangle target = new Rectangle(x + state.transX, y + state.transY, width, height);
         Rectangle result = current.intersection(target);
         result = result.intersection(screen);
         if (result.width < 0) {
            result.width = 0;
         }

         if (result.height < 0) {
            result.height = 0;
         }

         state.enabled = true;
         state.x = result.x;
         state.y = result.y;
         state.width = result.width;
         state.height = result.height;
         GL30.glEnable(3089);
         GL30.glScissor(result.x, result.y, result.width, result.height);
      }
   }

   public static void translate(int x, int y) {
      state.transX = x;
      state.transY = y;
   }

   public static void translateFromComponentCoordinates(int x, int y) {
      Window window = getWindow();
      if (window != null) {
         int totalHeight = window.getScaledHeight();
         double scaleFactor = getScaleFactor();
         int screenX = (int)((double)x * scaleFactor);
         int screenY = (int)((double)y * scaleFactor);
         screenY = (int)((double)totalHeight * scaleFactor) - screenY;
         translate(screenX, screenY);
      }
   }
}
