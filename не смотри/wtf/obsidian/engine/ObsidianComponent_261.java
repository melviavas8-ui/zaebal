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
























import java.util.function.Supplier;
import lombok.Generated;

public class ObsidianComponent_261 extends Setting {
   public int key;

   public ObsidianComponent_261(String name, int keyDefault) {
      super(name);
      this.key = keyDefault;
   }

   public ObsidianComponent_261 visible(Supplier<Boolean> state) {
      this.visible = state;
      return this;
   }

   @Generated
   public int getKey() {
      return this.key;
   }

   @Generated
   public void setKey(int key) {
      this.key = key;
   }
}
