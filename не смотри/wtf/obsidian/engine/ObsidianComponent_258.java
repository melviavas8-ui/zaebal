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
public final class ObsidianComponent_258 {
   public final float pitch;
   public final double distanceToTarget;
   public final int ticks;
   public final Vec3d landingPos;

   public ObsidianComponent_258(float pitch, double distanceToTarget, int ticks, Vec3d landingPos) {
      this.pitch = pitch;
      this.distanceToTarget = distanceToTarget;
      this.ticks = ticks;
      this.landingPos = landingPos;
   }
}
