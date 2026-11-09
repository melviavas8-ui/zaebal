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
public class ObsidianComponent_139 {
   public final Vec3d pos;
   public final long timestamp;
   public final float ageSec;
public ObsidianComponent_139(Vec3d pos, long timestamp, float ageSec) {
      this.pos = pos;
      this.timestamp = timestamp;
      this.ageSec = ageSec;
   }
}
