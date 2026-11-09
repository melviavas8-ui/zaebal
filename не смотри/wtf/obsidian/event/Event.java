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

























import java.lang.reflect.InvocationTargetException;
import lombok.Generated;

public class Event {
   public boolean cancelled;

   public void cancel() {
      this.cancelled = true;
   }

   public void call() {
      try {
         EventManager.invoke(this);
      } catch (InvocationTargetException | InstantiationException | IllegalAccessException var2) {
         throw new RuntimeException("Failed to Invoke Method", var2);
      }
   }

   @Generated
   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   @Generated
   public boolean isCancelled() {
      return this.cancelled;
   }
}
