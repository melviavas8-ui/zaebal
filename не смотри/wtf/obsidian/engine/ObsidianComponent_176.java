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

public class ObsidianComponent_176 {
   public Vec3d pos;
   public long spawnTime;
   public float spinSeed;
   public float sideYaw;

   public ObsidianComponent_176(Vec3d pos, long spawnTime, float spinSeed, float sideYaw) {
      this.pos = pos;
      this.spawnTime = spawnTime;
      this.spinSeed = spinSeed;
      this.sideYaw = sideYaw;
   }

   public Vec3d pos() { return this.pos; }
   public long spawnTime() { return this.spawnTime; }
   public float spinSeed() { return this.spinSeed; }
   public float sideYaw() { return this.sideYaw; }

}
