package wtf.obsidian.modules.render;

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

























public class CustomWorldModule extends Module {
   public static CustomWorldModule INSTANCE = new CustomWorldModule();
   public final MultiBooleanSetting worldSettings;
   public final SliderSetting timeSetting;
   public final SliderSetting fogDistanceSetting;

   public CustomWorldModule() {
      super("CustomWorld", "Настройки мира", Category.RENDER);
      BooleanSetting[] var10004 = new BooleanSetting[2];
      var10004[0] = new BooleanSetting("Время", true);
      var10004[1] = new BooleanSetting("Фог", true);
      this.worldSettings = new MultiBooleanSetting("Настройки мира", var10004);
      this.timeSetting = new SliderSetting(
            "Время",
            12.0f,
            0.0f,
            24.0f,
            1.0f
         )
         .visible(() -> this.worldSettings.is("Время"));
      this.fogDistanceSetting = new SliderSetting(
            "Дистанция фога",
            100.0f,
            20.0f,
            200.0f,
            1.0f
         )
         .visible(() -> this.worldSettings.is("Фог"));
      Setting[] var10001 = new Setting[3];
      var10001[0] = this.worldSettings;
      var10001[1] = this.timeSetting;
      var10001[2] = this.fogDistanceSetting;
      this.addSettings(var10001);
   }

   public boolean isTimeEnabled() {
      return (this.isEnable() && this.worldSettings.is("Время"));
   }

   public boolean isFogEnabled() {
      return (this.isEnable() && this.worldSettings.is("Фог"));
   }

   public long getForcedTime() {
      return (long)this.timeSetting.get() * (1000L);
   }

   public float getFogDistance() {
      return this.fogDistanceSetting.get();
   }

   public int getFogColor() {
      return !Obsidian.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")
         ? Obsidian.INSTANCE.themeStorage.getThemes().getTheme().color[0]
         : ObsidianComponent_olorUtil.getThemeColor();
   }
}
