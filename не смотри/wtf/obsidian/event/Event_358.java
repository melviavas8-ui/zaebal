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
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class Event_358 extends Event {
   public final PlayerEntity player;
   public final Entity target;

   @Generated
   public Event_358(PlayerEntity player, Entity target) {
      this.player = player;
      this.target = target;
   }

   @Generated
   public PlayerEntity getPlayer() {
      return this.player;
   }

   @Generated
   public Entity getTarget() {
      return this.target;
   }
}
