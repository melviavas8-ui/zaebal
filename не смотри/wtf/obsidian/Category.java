package wtf.obsidian;

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

public enum Category {
   COMBAT("Combat", "b"),
   MOVEMENT("Movement", "c"),
   RENDER("Render", "d"),
   MISC("Misc", "h"),
   PLAYER("Player", "e");

   public final String name;
   public final String icons;

   @Generated
   private Category(final String name, final String icons) {
      this.name = name;
      this.icons = icons;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public String getIcons() {
      return this.icons;
   }
}
