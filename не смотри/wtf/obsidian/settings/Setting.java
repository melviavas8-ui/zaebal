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
























import java.awt.Color;
import java.util.function.Supplier;
import lombok.Generated;

public abstract class Setting implements IInstance {
   public final String name;
   public Supplier<Boolean> visible = () -> Boolean.valueOf(true);
   public Color color = Color.WHITE;

   public Setting(String name) {
      this.name = name;
   }

   public Boolean visible() {
      return this.visible.get();
   }

   public String displayName() {
      return Obsidian.INSTANCE.localizationStorage == null ? this.name : Obsidian.INSTANCE.localizationStorage.translate(this.name);
   }

   @Generated
   public String name() {
      return this.name;
   }

   @Generated
   public Color color() {
      return this.color;
   }
}
