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
























public class FontTextureAtlas {
   public final String moduleName;
   public final String categoryIcon;
   public final boolean enabled;
   public final String customText;
   public final long startTime;

   public FontTextureAtlas(String moduleName, String categoryIcon, boolean enabled, String customText, long startTime) {
      this.moduleName = moduleName;
      this.categoryIcon = categoryIcon;
      this.enabled = enabled;
      this.customText = customText;
      this.startTime = startTime;
   }

   public boolean isCustom() {
      return (this.customText != null && !this.customText.isEmpty());
   }
}
