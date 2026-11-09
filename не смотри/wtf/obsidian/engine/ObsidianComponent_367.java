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























import net.minecraft.util.math.Vec3d;

public class ObsidianComponent_367 {
   public Vec3d pos;
   public long startTimeMs;

   public ObsidianComponent_367(Vec3d pos, long startTimeMs) {
      this.pos = pos;
      this.startTimeMs = startTimeMs;
   }

   public Vec3d pos() { return this.pos; }
   public long startTimeMs() { return this.startTimeMs; }

}
