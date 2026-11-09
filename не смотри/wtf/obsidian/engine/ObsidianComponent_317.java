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























import net.minecraft.item.Item;

public class ObsidianComponent_317 {
   public String name;
   public Item item;
   public ObsidianComponent_261 bind;

   public ObsidianComponent_317(String name, Item item, ObsidianComponent_261 bind) {
      this.name = name;
      this.item = item;
      this.bind = bind;
   }

   public String name() { return this.name; }
   public Item item() { return this.item; }
   public ObsidianComponent_261 bind() { return this.bind; }

}
