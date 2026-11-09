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
























import lombok.Generated;

public class ObsidianComponent_372 {
   public long millis;

   public ObsidianComponent_372() {
      this.reset();
   }

   public boolean finished(float delay) {
      return ((float)System.currentTimeMillis() - delay >= (float)this.millis);
   }

   public boolean finished(long delay) {
      return (System.currentTimeMillis() - this.millis >= delay);
   }

   public void reset() {
      this.millis = System.currentTimeMillis();
   }

   public long getElapsedTime() {
      return System.currentTimeMillis() - this.millis;
   }

   @Generated
   public long getMillis() {
      return this.millis;
   }

   @Generated
   public void setMillis(long millis) {
      this.millis = millis;
   }
}
