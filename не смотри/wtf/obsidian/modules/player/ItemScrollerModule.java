package wtf.obsidian.modules.player;

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

























public class ItemScrollerModule extends Module {
   public static ItemScrollerModule INSTANCE = new ItemScrollerModule();
   public final SliderSetting delay = new SliderSetting(
      "Задержка",
      50.0f,
      0.0f,
      200.0f,
      1.0f
   );
   public long lastQuickMoveAt;

   public ItemScrollerModule() {
      super("ItemScroller", "Убирает задержку перемещения предметов", Category.PLAYER);
      Setting[] var10001 = new Setting[1];
      var10001[0] = this.delay;
      this.addSettings(var10001);
   }

   public boolean canQuickMove() {
      long now = System.currentTimeMillis();
      if (now - this.lastQuickMoveAt < (long)this.delay.get()) {
         return false;
      } else {
         this.lastQuickMoveAt = now;
         return true;
      }
   }

   public void resetTimer() {
      this.lastQuickMoveAt = 0L;
   }

   @Override
   public void onDisable() {
      this.resetTimer();
      super.onDisable();
   }
}
