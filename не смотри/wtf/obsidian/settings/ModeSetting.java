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
























import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import lombok.Generated;

public class ModeSetting extends Setting {
   public List<String> mods;
   public String current;
   public int index;

   public ModeSetting(String name, String current, String... modes) {
      super(name);
      this.mods = Arrays.asList(modes);
      this.index = this.mods.indexOf(current);
      if (this.index < 0) {
         this.index = 0;
      }

      this.current = this.mods.get(this.index);
   }

   public void set(String selected) {
      int newIndex = this.mods.indexOf(selected);
      if (newIndex >= 0) {
         this.current = selected;
         this.index = newIndex;
      }
   }

   public boolean is(String mode) {
      return this.current.equals(mode);
   }

   public String displayMode(String mode) {
      return Obsidian.INSTANCE.localizationStorage == null ? mode : Obsidian.INSTANCE.localizationStorage.translate(mode);
   }

   public String displayCurrent() {
      return this.displayMode(this.current);
   }

   public ModeSetting visible(Supplier<Boolean> state) {
      this.visible = state;
      return this;
   }

   @Generated
   public List<String> getMods() {
      return this.mods;
   }

   @Generated
   public String getCurrent() {
      return this.current;
   }

   @Generated
   public int getIndex() {
      return this.index;
   }

   @Generated
   public void setMods(List<String> mods) {
      this.mods = mods;
   }

   @Generated
   public void setCurrent(String current) {
      this.current = current;
   }

   @Generated
   public void setIndex(int index) {
      this.index = index;
   }
}
