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
























import lombok.Generated;

public enum Theme {
   Custom(new ObsidianComponent_olorPalette("Rainbow", new int[]{ObsidianComponent_olorUtil.rgba(255, 255, 255, 0)})),
   Purple(new ObsidianComponent_olorPalette("Lavender", new int[]{ObsidianComponent_olorUtil.rgba(190, 143, 255, 255), ObsidianComponent_olorUtil.darken(ObsidianComponent_olorUtil.rgba(190, 143, 255, 255), 0.35f)})),
   Red(new ObsidianComponent_olorPalette("Blood", new int[]{ObsidianComponent_olorUtil.rgba(230, 50, 57, 255), ObsidianComponent_olorUtil.darken(ObsidianComponent_olorUtil.rgba(230, 50, 57, 255), 0.35f)})),
   Blue(new ObsidianComponent_olorPalette("Ocean", new int[]{ObsidianComponent_olorUtil.rgba(95, 113, 191, 255), ObsidianComponent_olorUtil.darken(ObsidianComponent_olorUtil.rgba(95, 113, 191, 255), 0.35f)})),
   Green(new ObsidianComponent_olorPalette("Emerald", new int[]{ObsidianComponent_olorUtil.rgba(60, 220, 140, 255), ObsidianComponent_olorUtil.darken(ObsidianComponent_olorUtil.rgba(60, 220, 140, 255), 0.35f)})),
   Pink(new ObsidianComponent_olorPalette("Rose", new int[]{ObsidianComponent_olorUtil.rgba(255, 120, 190, 255), ObsidianComponent_olorUtil.darken(ObsidianComponent_olorUtil.rgba(255, 120, 190, 255), 0.35f)})),
   Orange(new ObsidianComponent_olorPalette("Gold", new int[]{ObsidianComponent_olorUtil.rgba(252, 192, 88, 255), ObsidianComponent_olorUtil.darken(ObsidianComponent_olorUtil.rgba(252, 192, 88, 255), 0.35f)})),
   Blues(new ObsidianComponent_olorPalette("Diamond", new int[]{ObsidianComponent_olorUtil.rgba(125, 217, 250, 255), ObsidianComponent_olorUtil.darken(ObsidianComponent_olorUtil.rgba(125, 217, 250, 255), 0.35f)})),
   Yellows(new ObsidianComponent_olorPalette("Sun", new int[]{ObsidianComponent_olorUtil.rgba(252, 231, 88, 255), ObsidianComponent_olorUtil.darken(ObsidianComponent_olorUtil.rgba(252, 231, 88, 255), 0.35f)}));

   public final ObsidianComponent_olorPalette theme;

   private Theme(final ObsidianComponent_olorPalette theme) {
      this.theme = theme;
   }

   @Generated
   public ObsidianComponent_olorPalette getTheme() {
      return this.theme;
   }
}
