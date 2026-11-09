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
import net.minecraft.util.math.MathHelper;

public class SliderSetting extends Setting {
   public float value;
   public final float min;
   public final float max;
   public final float increment;
   public boolean active;

   public SliderSetting(String name, float value, float min, float max, float increment) {
      super(name);
      this.value = value;
      this.min = min;
      this.max = max;
      this.increment = increment;
   }

   public Number getValue() {
      return MathHelper.clamp(this.value, this.getMin(), this.getMax());
   }

   public void setValue(float value) {
      this.value = MathHelper.clamp(value, this.getMin(), this.getMax());
   }

   public float get() {
      return this.getValue().floatValue();
   }

   public SliderSetting visible(Supplier<Boolean> state) {
      this.visible = state;
      return this;
   }

   @Generated
   public float getMin() {
      return this.min;
   }

   @Generated
   public float getMax() {
      return this.max;
   }

   @Generated
   public float getIncrement() {
      return this.increment;
   }

   @Generated
   public boolean isActive() {
      return this.active;
   }

   @Generated
   public void setActive(boolean active) {
      this.active = active;
   }
}
