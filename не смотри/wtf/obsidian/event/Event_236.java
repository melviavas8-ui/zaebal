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

public class Event_236 extends Event {
   public final int chunkX;
   public final int chunkZ;

   @Generated
   public int getChunkX() {
      return this.chunkX;
   }

   @Generated
   public int getChunkZ() {
      return this.chunkZ;
   }

   @Generated
   public Event_236(int chunkX, int chunkZ) {
      this.chunkX = chunkX;
      this.chunkZ = chunkZ;
   }
}
