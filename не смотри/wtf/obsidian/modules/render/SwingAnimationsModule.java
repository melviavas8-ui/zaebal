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

























public class SwingAnimationsModule extends Module {
   public static SwingAnimationsModule INSTANCE = new SwingAnimationsModule();
   public boolean swimmingAnimation = true;
   public boolean climbAndCrawl = true;
   public boolean mb3DCompat = false;
   public final BooleanSetting hmiEnable = new BooleanSetting("Мод на красивые руки", false);
   public final ModeSetting hmiAnimationType;
   public final SliderSetting hmiSmoothness;
   public final BooleanSetting swingEnabled;
   public final ModeSetting swingType;
   public final SliderSetting swingStrength;
   public final SliderSetting corner;
   public final SliderSetting slant;
   public final BooleanSetting smoothEnabled;
   public final SliderSetting slowAnimationSpeed;
   public final BooleanSetting auraTargetOnly;
   public final BooleanSetting swapHands;
   public final BooleanSetting eatAnim;

   public SwingAnimationsModule() {
      super("SwingAnimations", "Кастомная анимация аттаки", Category.RENDER);
      String[] var10005 = new String[2];
      var10005[0] = "Классик";
      var10005[1] = "Шарп";
      this.hmiAnimationType = new ModeSetting("Вид анимации", "Классик", var10005).visible(this.hmiEnable::isState);
      this.hmiSmoothness = new SliderSetting(
            "Плавность анимации",
            1.0f,
            0.3499999940395355f,
            2.5f,
            0.05000000074505806f
         )
         .visible(this.hmiEnable::isState);
      this.swingEnabled = new BooleanSetting("Анимация свинга", true)
         .visible(() -> Boolean.valueOf((!this.hmiEnable.isState())));
      var10005 = new String[13];
      var10005[0] = "Smooth";
      var10005[1] = "Static";
      var10005[2] = "Down";
      var10005[3] = "DropDown";
      var10005[4] = "Poke";
      var10005[5] = "SelfBack";
      var10005[6] = "Feast";
      var10005[7] = "ToBack";
      var10005[8] = "Block";
      var10005[9] = "Akrien";
      var10005[10] = "Break";
      var10005[11] = "Pander";
      var10005[12] = "Slant";
      this.swingType = new ModeSetting("Тип свинга", "Smooth", var10005)
         .visible(
            () -> Boolean.valueOf(
                  (!this.hmiEnable.isState() && this.swingEnabled.isState())
               )
         );
      this.swingStrength = new SliderSetting(
            "Сила анимации",
            1.0f,
            0.10000000149011612f,
            3.0f,
            0.009999999776482582f
         )
         .visible(
            () -> Boolean.valueOf(
                  (!this.hmiEnable.isState() && this.swingEnabled.isState() && !this.swingType.is("Pander"))
               )
         );
      this.corner = new SliderSetting(
            "Угол DropDown",
            12.0f,
            1.0f,
            360.0f,
            1.0f
         )
         .visible(
            () -> Boolean.valueOf(
                  (!this.hmiEnable.isState() && this.swingEnabled.isState() && this.swingType.is("DropDown"))
               )
         );
      this.slant = new SliderSetting(
            "Наклон DropDown",
            12.0f,
            1.0f,
            360.0f,
            1.0f
         )
         .visible(
            () -> Boolean.valueOf(
                  (!this.hmiEnable.isState() && this.swingEnabled.isState() && this.swingType.is("DropDown"))
               )
         );
      this.smoothEnabled = new BooleanSetting("Плавная анимация", false)
         .visible(() -> Boolean.valueOf((!this.hmiEnable.isState())));
      this.slowAnimationSpeed = new SliderSetting(
            "Скорость анимации",
            12.0f,
            1.0f,
            50.0f,
            1.0f
         )
         .visible(
            () -> Boolean.valueOf(
                  (!this.hmiEnable.isState() && this.smoothEnabled.isState())
               )
         );
      this.auraTargetOnly = new BooleanSetting("Только при Aura", false)
         .visible(() -> Boolean.valueOf((!this.hmiEnable.isState())));
      this.swapHands = new BooleanSetting("Свап рук", false)
         .visible(
            () -> Boolean.valueOf(
                  (!this.hmiEnable.isState())
               )
         );
      this.eatAnim = new BooleanSetting("Анимация еды", false)
         .visible(() -> Boolean.valueOf((!this.hmiEnable.isState())));
      Setting[] var10001 = new Setting[13];
      var10001[0] = this.hmiEnable;
      var10001[1] = this.hmiAnimationType;
      var10001[2] = this.hmiSmoothness;
      var10001[3] = this.swingEnabled;
      var10001[4] = this.swingType;
      var10001[5] = this.swingStrength;
      var10001[6] = this.corner;
      var10001[7] = this.slant;
      var10001[8] = this.smoothEnabled;
      var10001[9] = this.slowAnimationSpeed;
      var10001[10] = this.auraTargetOnly;
      var10001[11] = this.swapHands;
      var10001[12] = this.eatAnim;
      this.addSettings(var10001);
   }
}
