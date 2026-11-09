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























public class ObsidianComponent_246 implements Cloneable {
   public boolean enabled;
   public int transX;
   public int transY;
   public int x;
   public int y;
   public int width;
   public int height;

   public ObsidianComponent_246() {
   }

   public ObsidianComponent_246 clone() {
      try {
         return (ObsidianComponent_246)super.clone();
      } catch (CloneNotSupportedException var2) {
         throw new AssertionError(var2);
      }
   }
}
