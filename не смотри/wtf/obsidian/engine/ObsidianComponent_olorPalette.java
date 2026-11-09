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

public class ObsidianComponent_olorPalette implements IInstance {
   public String name;
   public int[] color;

   public ObsidianComponent_olorPalette(String name, int... color) {
      this.name = name;
      this.color = color;
   }

   public int getColor(int index) {
      return this.name.equals("Rainbow")
         ? ObsidianComponent_olorUtil.rainbow(
            10,
            index,
            0.6000000238418579f,
            1.0f,
            1.0f
         )
         : ObsidianComponent_olorUtil.gradient(5, index, this.color);
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public int[] getColor() {
      return this.color;
   }

   @Generated
   public void setName(String name) {
      this.name = name;
   }

   @Generated
   public void setColor(int[] color) {
      this.color = color;
   }
}
