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
























import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import lombok.Generated;

public class ObsidianComponent_423 {
   private ObjectArrayList<Theme> themeList = new ObjectArrayList();
   public Theme themes;

   public ObsidianComponent_423() {
      this.onInitialize();
   }
   public void onInitialize() {
      ObjectArrayList var10000 = this.themeList;
      Theme[] var10001 = new Theme[9];
      var10001[0] = Theme.Custom;
      var10001[1] = Theme.Purple;
      var10001[2] = Theme.Red;
      var10001[3] = Theme.Blue;
      var10001[4] = Theme.Green;
      var10001[5] = Theme.Pink;
      var10001[6] = Theme.Orange;
      var10001[7] = Theme.Blues;
      var10001[8] = Theme.Yellows;
      var10000.addAll(Arrays.asList(var10001));
      this.themes = (Theme)this.themeList.get(1);
   }

   @Generated
   public ObjectArrayList<Theme> getThemeList() {
      return this.themeList;
   }

   @Generated
   public Theme getThemes() {
      return this.themes;
   }

   @Generated
   public void setThemeList(ObjectArrayList<Theme> themeList) {
      this.themeList = themeList;
   }

   @Generated
   public void setThemes(Theme themes) {
      this.themes = themes;
   }
}
