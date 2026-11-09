package wtf.obsidian.utils;

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























public enum Language {
   RUSSIAN("Русский"),
   ENGLISH("English"),
   UKRAINIAN("Українська");

   public final String displayName;

   private Language(String displayName) {
      this.displayName = displayName;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public Language next() {
      Language[] values = values();
      return values[(this.ordinal() + 1) % values.length];
   }
}
