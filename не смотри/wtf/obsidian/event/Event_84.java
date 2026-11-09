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
import net.minecraft.entity.projectile.FireworkRocketEntity;

public class Event_84 extends Event {
   public final FireworkRocketEntity firework;

   @Generated
   public FireworkRocketEntity getFirework() {
      return this.firework;
   }

   @Generated
   public Event_84(FireworkRocketEntity firework) {
      this.firework = firework;
   }
}
