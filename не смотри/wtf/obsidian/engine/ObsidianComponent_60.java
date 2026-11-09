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

public class ObsidianComponent_60 {
   public Vec3d pos;
   public float seconds;

   public ObsidianComponent_60(Vec3d pos, float seconds) {
      this.pos = pos;
      this.seconds = seconds;
   }

   public Vec3d pos() { return this.pos; }
   public float seconds() { return this.seconds; }

}
