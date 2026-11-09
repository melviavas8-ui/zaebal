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
























import java.util.List;
import net.minecraft.util.math.Vec3d;
public class ObsidianComponent_66 {
   public final Vec3d origin;
   public final long startTime;
   public final float baseRotation;
   public final List<ObsidianComponent_281> particles;
   public final List<ObsidianComponent_124> orbitLines;

   public ObsidianComponent_66(Vec3d origin, long startTime, float baseRotation, List<ObsidianComponent_281> particles, List<ObsidianComponent_124> orbitLines) {
      this.origin = origin;
      this.startTime = startTime;
      this.baseRotation = baseRotation;
      this.particles = particles;
      this.orbitLines = orbitLines;
   }
}
