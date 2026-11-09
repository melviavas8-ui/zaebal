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

public class ObsidianComponent_213 {
   public BlockPos center;
   public long runAt;

   public ObsidianComponent_213(BlockPos center, long runAt) {
      this.center = center;
      this.runAt = runAt;
   }

   public BlockPos center() { return this.center; }
   public long runAt() { return this.runAt; }

}
