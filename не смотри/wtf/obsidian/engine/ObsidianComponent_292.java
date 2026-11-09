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

public final class ObsidianComponent_292 implements IInstance {
   public static int currentFPS;

   public static void updateFPS() {
      int prevFPS = mc.getCurrentFps();
      currentFPS = MathHelper.lerp(0.5f, prevFPS, currentFPS);
   }

   @Generated
   public ObsidianComponent_292() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   @Generated
   public static int getCurrentFPS() {
      return currentFPS;
   }
}
