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

























public class HelpMessageModule extends Module {
   public static HelpMessageModule INSTANCE = new HelpMessageModule();
   private final ObsidianComponent_261 bind = new ObsidianComponent_261("Бинд", -1);

   public HelpMessageModule() {
      super("HelpMessage", "Отправляет координаты в глобальный чат", Category.PLAYER);
      Setting[] var10001 = new Setting[1];
      var10001[0] = this.bind;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onBinding(InputEvent event) {
      if (mc.player != null && mc.getNetworkHandler() != null && mc.currentScreen == null) {
         if (event.getKey() == this.bind.getKey()) {
            int x = mc.player.getBlockX();
            int y = mc.player.getBlockY();
            int z = mc.player.getBlockZ();
            mc.getNetworkHandler().sendChatMessage("! " + x + " " + y + " " + z);
         }
      }
   }
}
