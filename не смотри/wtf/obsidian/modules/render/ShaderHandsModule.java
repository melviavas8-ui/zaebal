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

























public class ShaderHandsModule extends Module {
   public static ShaderHandsModule INSTANCE = new ShaderHandsModule();
   private static final ShaderHandsRenderer RENDERER = ShaderHandsRenderer.getInstance();
   public final ModeSetting mode;
   public final SliderSetting waveSpeed;
   public final SliderSetting waveScale;
   public final SliderSetting outline;
   public final SliderSetting glow;
   public final SliderSetting fill;
   public final SliderSetting alpha;

   public ShaderHandsModule() {
      super("ShaderHands", "Красивый Шейдер на руки и предметы", Category.RENDER);
      String[] var10005 = new String[2];
      var10005[0] = "Свечение";
      var10005[1] = "Красивый";
      this.mode = new ModeSetting("Режим", "Свечение", var10005);
      this.waveSpeed = new SliderSetting(
            "Скорость волн",
            1.2000000476837158f,
            0.10000000149011612f,
            5.0f,
            0.10000000149011612f
         )
         .visible(() -> this.mode.is("Красивый"));
      this.waveScale = new SliderSetting(
            "Частота волн",
            1.0f,
            1.0f,
            3.0f,
            0.10000000149011612f
         )
         .visible(() -> this.mode.is("Красивый"));
      this.outline = new SliderSetting(
         "Ширина обводки",
         1.2000000476837158f,
         0.10000000149011612f,
         5.0f,
         0.10000000149011612f
      );
      this.glow = new SliderSetting(
         "Сила свечения",
         1.0f,
         0.0f,
         5.0f,
         0.10000000149011612f
      );
      this.fill = new SliderSetting(
         "Заливка",
         0.6000000238418579f,
         0.0f,
         1.0f,
         0.009999999776482582f
      );
      this.alpha = new SliderSetting(
         "Прозрачность",
         1.0f,
         0.0f,
         1.0f,
         0.05000000074505806f
      );
      Setting[] var10001 = new Setting[7];
      var10001[0] = this.mode;
      var10001[1] = this.waveSpeed;
      var10001[2] = this.waveScale;
      var10001[3] = this.outline;
      var10001[4] = this.glow;
      var10001[5] = this.fill;
      var10001[6] = this.alpha;
      this.addSettings(var10001);
   }

   @Override
   public void onDisable() {
      RENDERER.invalidateState();
      super.onDisable();
   }

   @EventTarget(
      priority = 0
   )
   public void onRender2D(EventRender2D event) {
      if (this.isEnable()) {
         RENDERER.renderOverlayIfPending();
      }
   }
}
