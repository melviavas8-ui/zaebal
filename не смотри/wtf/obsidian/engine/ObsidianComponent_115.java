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

public class ObsidianComponent_115 {
   public BlockPos pos;
   public ObsidianComponent_179 type;
   public boolean isLooted;

   public ObsidianComponent_115(BlockPos pos, ObsidianComponent_179 type, boolean isLooted) {
      this.pos = pos;
      this.type = type;
      this.isLooted = isLooted;
   }

   public BlockPos pos() { return this.pos; }
   public ObsidianComponent_179 type() { return this.type; }
   public boolean isLooted() { return this.isLooted; }

}
