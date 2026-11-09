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























public enum ObsidianComponent_208 {
   HOLY_WATER("[✦] Святая вода", 16774507, 12123970),
   WRATH("[✦] Зелье Гнева", 12849682, 16757051),
   PALADIN("[✦] Зелье Паладина", 12123970, 16773280),
   ASSASSIN("[✦] Зелье Ассасина", 5592405, 11545130);

   public final String displayName;
   public final int startColor;
   public final int endColor;

   private ObsidianComponent_208(String displayName, int startColor, int endColor) {
      this.displayName = displayName;
      this.startColor = startColor;
      this.endColor = endColor;
   }
   public String plainName() {
      int index = this.displayName.indexOf("] ");
      return index >= 0 ? this.displayName.substring(index + (2)) : this.displayName;
   }
}
