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
























import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Generated;
import org.lwjgl.glfw.GLFW;

public final class ObsidianComponent_349 implements IInstance {
   public static final int MOUSE_BUTTON_OFFSET = 1000;

   public static void call(int key, int action) {
      if (key > -1) {
         if (action == (1)) {
            if (key == 344) {
               SoundManager.playSound(
                  "opengui.wav",
                  0.6,
                  1.0f
               );
               mc.setScreen(new ObsidianScreen());
            }

            if (key == ModuleManager.autoBuy.openKey.getKey()) {
               mc.setScreen(new ObsidianScreen2());
            }

            new InputEvent(key, ObsidianComponent_125.KEYBOARD).call();
            ObjectArrayList<Module> modules = ModuleManager.INSTANCE.getObject();
            int i = 0;

            for (int size = modules.size(); i < size; i++) {
               Module module = (Module)modules.get(i);
               if (module.getKey() == key) {
                  module.toggle();
               }
            }
         }
      }
   }

   public static String getKeyName(int keyCode) {
      if (keyCode == -1) {
         return "None";
      } else {
         String name = GLFW.glfwGetKeyName(keyCode, 0);
         if (name != null) {
            return name.toUpperCase();
         } else {
            return switch (keyCode) {
               case 32 -> "SPACE";
               case 256 -> "ESC";
               case 340 -> "LSHIFT";
               case 341 -> "LCTRL";
               case 344 -> "RSHIFT";
               case 345 -> "RCTRL";
               default -> "KEY" + keyCode;
            };
         }
      }
   }

   public static void callMouse(int button, int action) {
      if (mc.currentScreen == null) {
         if (button >= 0) {
            if (action == 1) {
               int mouseKey = 1000 + button;
               new InputEvent(mouseKey, ObsidianComponent_125.MOUSE).call();
               ObjectArrayList<Module> modules = ModuleManager.INSTANCE.getObject();
               int i = 0;

               for (int size = modules.size(); i < size; i++) {
                  Module module = (Module)modules.get(i);
                  if (module.getKey() == mouseKey) {
                     module.toggle();
                  }
               }
            }
         }
      }
   }

   public static boolean isBindHeld(int key) {
      if (key == -1) {
         return false;
      } else {
         long window = mc.getWindow().getHandle();
         if (key >= 1000) {
            int mouseButton = key - 1000;
            return (GLFW.glfwGetMouseButton(window, mouseButton) == (1));
         } else {
            return (GLFW.glfwGetKey(window, key) == (1));
         }
      }
   }

   public static boolean isBindPressed(int key) {
      return isBindHeld(key);
   }

   public static String getBindName(int key) {
      if (key == (-1)) {
         return "n/a";
      } else if (key >= (1000)) {
         int mouseButton = key - 1000;

         return switch (mouseButton) {
            case 0 -> "ЛКМ";
            case 1 -> "ПКМ";
            case 2 -> "СКМ";
            case 3 -> "MOUSE4";
            case 4 -> "MOUSE5";
            default -> "MOUSE" + (mouseButton + (1));
         };
      } else if (key >= 65 && key <= (90)) {
         return String.valueOf((char)(65 + (key - (65))));
      } else if (key >= (48) && key <= 57) {
         return String.valueOf((char)(48 + (key - 48)));
      } else {
         String symbol = switch (key) {
            case 39 -> "'";
            case 44 -> ",";
            case 45 -> "-";
            case 46 -> ".";
            case 47 -> "/";
            case 59 -> ";";
            case 61 -> "=";
            case 91 -> "[";
            case 92 -> "\\";
            case 93 -> "]";
            case 96 -> "`";
            default -> null;
         };
         if (symbol != null) {
            return symbol;
         } else {
            return switch (key) {
               case 32 -> "SPACE";
               case 256 -> "ESC";
               case 257 -> "ENTER";
               case 258 -> "TAB";
               case 259 -> "BACKSPACE";
               case 260 -> "INSERT";
               case 261 -> "DELETE";
               case 262 -> "RIGHT";
               case 263 -> "LEFT";
               case 264 -> "DOWN";
               case 265 -> "UP";
               case 266 -> "PAGEUP";
               case 267 -> "PAGEDOWN";
               case 268 -> "HOME";
               case 269 -> "END";
               case 280 -> "CAPS";
               case 290 -> "F1";
               case 291 -> "F2";
               case 292 -> "F3";
               case 293 -> "F4";
               case 294 -> "F5";
               case 295 -> "F6";
               case 296 -> "F7";
               case 297 -> "F8";
               case 298 -> "F9";
               case 299 -> "F10";
               case 300 -> "F11";
               case 301 -> "F12";
               case 320 -> "NUM0";
               case 321 -> "NUM1";
               case 322 -> "NUM2";
               case 323 -> "NUM3";
               case 324 -> "NUM4";
               case 325 -> "NUM5";
               case 326 -> "NUM6";
               case 327 -> "NUM7";
               case 328 -> "NUM8";
               case 329 -> "NUM9";
               case 330 -> "NUM.";
               case 331 -> "NUM/";
               case 332 -> "NUM*";
               case 333 -> "NUM-";
               case 334 -> "NUM+";
               case 335 -> "NUMENTER";
               case 340 -> "LSHIFT";
               case 341 -> "LCTRL";
               case 342 -> "LALT";
               case 344 -> "RSHIFT";
               case 345 -> "RCTRL";
               case 346 -> "RALT";
               default -> "KEY" + key;
            };
         }
      }
   }

   public static boolean isMouseButton(int key) {
      return (key >= (1000));
   }

   public static int getMouseButtonFromKey(int key) {
      return isMouseButton(key) ? key - (1000) : -1;
   }

   public static int createMouseBind(int mouseButton) {
      return 1000 + mouseButton;
   }

   @Generated
   public ObsidianComponent_349() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
