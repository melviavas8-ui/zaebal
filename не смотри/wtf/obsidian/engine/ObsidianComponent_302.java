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























import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ObsidianComponent_302 {
   public BlockPos blockPos;
   public Vec3d pos;
   public long endTime;

   public ObsidianComponent_302(BlockPos blockPos, Vec3d pos, long endTime) {
      this.blockPos = blockPos;
      this.pos = pos;
      this.endTime = endTime;
   }

   public BlockPos blockPos() { return this.blockPos; }
   public Vec3d pos() { return this.pos; }
   public long endTime() { return this.endTime; }

}
