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

public class ObsidianComponent_159 {
   public String name;
   public BlockPos pos;

   public ObsidianComponent_159(String name, BlockPos pos) {
      this.name = name;
      this.pos = pos;
   }

   public String name() { return this.name; }
   public BlockPos pos() { return this.pos; }

}
