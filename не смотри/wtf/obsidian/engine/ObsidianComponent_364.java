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
























public enum ObsidianComponent_364 {
   CUSTOM("CUSTOM"),
   D_HELPER("D.HELPER"),
   FROSTINE("FROSTINE"),
   SPRING("SPRING"),
   AUTUMN("AUTUMN"),
   GLADIATOR("GLADIATOR"),
   PALADIN("PALADIN"),
   LUXE("LUXE"),
   STAFF("STAFF"),
   SUPPORT("SUPPORT"),
   ETERNITY("ETERNITY"),
   OVERLORD("OVERLORD"),
   D_ADMIN("D.ADMIN"),
   POVELITEL("POVELITEL"),
   IMPERATOR("IMPERATOR"),
   LEGENDA("LEGENDA"),
   PRAVITEL("PRAVITEL"),
   PHOENIX("PHOENIX"),
   PLAYER("PLAYER");

   public final String name;

   private ObsidianComponent_364(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public static ObsidianComponent_364 fromString(String text) {
      ObsidianComponent_364[] var1 = values();
      int var2 = var1.length;

      for (int var3 = 0; var3 < var2; var3++) {
         ObsidianComponent_364 rank = var1[var3];
         if (rank.name.equalsIgnoreCase(text)) {
            return rank;
         }
      }

      return null;
   }
}
