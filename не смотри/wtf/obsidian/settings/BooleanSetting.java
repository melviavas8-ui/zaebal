package wtf.obsidian.settings;

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

public class BooleanSetting extends Setting {
   public boolean state;

   public BooleanSetting(String name, boolean state) {
      super(name);
      this.state = state;
   }

   public static BooleanSetting of(String name, boolean state) {
      return new BooleanSetting(name, state);
   }

   public BooleanSetting visible(Supplier<Boolean> state) {
      this.visible = state;
      return this;
   }

   @Generated
   public boolean isState() {
      return this.state;
   }

   @Generated
   public void setState(boolean state) {
      this.state = state;
   }
}
