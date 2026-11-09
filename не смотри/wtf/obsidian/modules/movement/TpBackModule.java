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

























public class TpBackModule extends Module {
   public static TpBackModule INSTANCE = new TpBackModule();
   public boolean isDead = false;
   private boolean waitingForRespawn = false;
   public int tickCounter = 0;
   public SliderSetting delay = new SliderSetting(
      "Задержка",
      5.0f,
      1.0f,
      20.0f,
      1.0f
   );

   public TpBackModule() {
      super("TpBack", "Возвращает на точки смерти", Category.MOVEMENT);
      Setting[] var10001 = new Setting[1];
      var10001[0] = this.delay;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onEvent(EventTick event) {
      if (mc.player != null && mc.world != null) {
         boolean playerDead = mc.player.getHealth() <= 0.0f || mc.player.deathTime > 0;
         if (playerDead && !this.isDead) {
            this.isDead = true;
            mc.player.networkHandler.sendChatMessage("/sethome obsidian");
            mc.player.requestRespawn();
            this.waitingForRespawn = true;
            this.tickCounter = 0;
         }

         if (this.waitingForRespawn && !playerDead) {
            this.tickCounter += 1;
            if ((float)this.tickCounter >= this.delay.get()) {
               mc.player.networkHandler.sendChatMessage("/home obsidian");
               this.waitingForRespawn = false;
               this.tickCounter = 0;
            }
         }

         if (!playerDead && !this.waitingForRespawn) {
            this.isDead = false;
         }
      }
   }
}
