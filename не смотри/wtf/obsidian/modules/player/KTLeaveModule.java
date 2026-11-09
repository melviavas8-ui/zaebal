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

























public class KTLeaveModule extends Module {
   public static KTLeaveModule INSTANCE = new KTLeaveModule();
   public boolean hasGM;
   public double lastX;
   public double lastY;
   public double lastZ;
   private ObsidianComponent_261 bind = new ObsidianComponent_261("Кнопка лива", -1);

   public KTLeaveModule() {
      super("KTLeave", "Позволяет ливнуть с пвп прямо в кт", Category.PLAYER);
      Setting[] var10001 = new Setting[1];
      var10001[0] = this.bind;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onKey(InputEvent e) {
      if (mc.player != null) {
         if (e.getKey() == this.bind.getKey()) {
            this.hasGM = (!this.hasGM);
            if (this.hasGM) {
               this.lastX = mc.player.getX();
               this.lastY = mc.player.getY();
               this.lastZ = mc.player.getZ();
               mc.player
                  .setPosition(
                     mc.player.getX() + 10.0,
                     mc.player.getY() + 10.0,
                     mc.player.getZ() + 10.0
                  );
            } else {
               mc.player.setPosition(this.lastX, this.lastY, this.lastZ);
            }
         }
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.hasGM = false;
   }
}
