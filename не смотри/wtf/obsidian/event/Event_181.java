package wtf.obsidian.event;

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
























import lombok.Generated;
import net.minecraft.util.math.BlockPos;

public class Event_181 extends Event {
   public final BlockPos pos;

   public Event_181(BlockPos pos) {
      this.pos = pos;
   }

   @Generated
   public BlockPos getPos() {
      return this.pos;
   }
}
