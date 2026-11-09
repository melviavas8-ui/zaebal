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























public class ObsidianComponent_170 {
   public ObsidianComponent_208 potionInfo;
   public double lastX;
   public double lastY;
   public double lastZ;

   public ObsidianComponent_170(ObsidianComponent_208 potionInfo, double lastX, double lastY, double lastZ) {
      this.potionInfo = potionInfo;
      this.lastX = lastX;
      this.lastY = lastY;
      this.lastZ = lastZ;
   }
}
