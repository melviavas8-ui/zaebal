package wtf.obsidian.modules.movement;

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

























public class TimerModule extends Module {
   public static TimerModule INSTANCE = new TimerModule();
   public SliderSetting speed = new SliderSetting(
      "Скорость",
      2.0f,
      0.10000000149011612f,
      10.0f,
      0.10000000149011612f
   );

   public TimerModule() {
      super("Timer", "Ускоряет время в игре", Category.MOVEMENT);
      Setting[] var10001 = new Setting[1];
      var10001[0] = this.speed;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      mc.player.speed = this.speed.getValue().floatValue();
   }

   @Override
   public void onDisable() {
      super.onDisable();
      mc.player.speed = 1.0f;
   }
}
