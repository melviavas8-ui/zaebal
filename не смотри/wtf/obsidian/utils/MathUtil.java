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
























public class MathUtil {
   public static boolean isHovering(float x, float y, float width, float height, int mouseX, int mouseY) {
      return ((float)mouseX >= x && (float)mouseY >= y && (float)mouseX < x + width && (float)mouseY < y + height);
   }

   public static boolean isInRegion(int mouseX, int mouseY, int x, int y, int width, int height) {
      return (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height);
   }

   public static boolean isInRegion(double mouseX, double mouseY, float x, float y, float width, float height) {
      return (mouseX >= (double)x && mouseX <= (double)(x + width) && mouseY >= (double)y && mouseY <= (double)(y + height));
   }

   public static boolean isHovering(float x, float y, float width, float height, double mouseX, double mouseY) {
      return (mouseX >= (double)x && mouseY >= (double)y && mouseX < (double)(x + width) && mouseY < (double)(y + height));
   }

   public static boolean isInRegion(double mouseX, double mouseY, int x, int y, int width, int height) {
      return (mouseX >= (double)x && mouseX <= (double)(x + width) && mouseY >= (double)y && mouseY <= (double)(y + height));
   }

   public static boolean isHovered(double mouseX, double mouseY, double x, double y, double width, double height) {
      return (mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height);
   }
}
