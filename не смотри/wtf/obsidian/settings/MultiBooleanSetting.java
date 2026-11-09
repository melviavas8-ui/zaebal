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
























import java.util.List;
import java.util.function.Supplier;
import lombok.Generated;

public class MultiBooleanSetting extends Setting {
   public List<BooleanSetting> settings;

   public MultiBooleanSetting(String name, BooleanSetting... settings) {
      super(name);
      this.settings = List.of(settings);
   }

   public MultiBooleanSetting of(String name, BooleanSetting... settings) {
      return new MultiBooleanSetting(name, settings);
   }

   public boolean is(String name) {
      return this.requireSetting(name).isState();
   }

   public void set(String name, boolean value) {
      this.requireSetting(name).setState(value);
   }

   public MultiBooleanSetting visible(Supplier<Boolean> state) {
      this.visible = state;
      return this;
   }
   public BooleanSetting requireSetting(String name) {
      for (BooleanSetting option : this.settings) {
         if (option.name().equalsIgnoreCase(name)) {
            return option;
         }
      }

      throw new NullPointerException("Unknown list setting entry: " + name);
   }

   @Generated
   public List<BooleanSetting> getSettings() {
      return this.settings;
   }

   @Generated
   public void setSettings(List<BooleanSetting> settings) {
      this.settings = settings;
   }
}
