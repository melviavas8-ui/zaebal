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
import net.minecraft.util.math.Vec3d;

public class Event_321 extends Event {
   public Vec3d movePos;

   @Generated
   public Vec3d getMovePos() {
      return this.movePos;
   }

   @Generated
   public void setMovePos(Vec3d movePos) {
      this.movePos = movePos;
   }

   @Generated
   public Event_321() {
   }

   @Generated
   public Event_321(Vec3d movePos) {
      this.movePos = movePos;
   }
}
