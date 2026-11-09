package wtf.obsidian.modules.misc;

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

























import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;

public class XCarryModule extends Module {
   public static XCarryModule INSTANCE = new XCarryModule();
   public BooleanSetting autoDisable = new BooleanSetting("Авто выкл", true);
   private boolean wasInInventory = false;

   public XCarryModule() {
      super("XCarry", "Дополнительные слоты", Category.MISC);
      Setting[] var10001 = new Setting[1];
      var10001[0] = this.autoDisable;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onPacket(PacketEvent event) {
      if (mc.player != null && mc.world != null) {
         if (event.getPacket() instanceof CloseHandledScreenC2SPacket && mc.currentScreen instanceof InventoryScreen) {
            event.cancel();
            this.wasInInventory = true;
         }
      }
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.player != null && mc.world != null) {
         if (this.wasInInventory && mc.currentScreen == null) {
            if (this.autoDisable.isState()) {
               this.toggle();
            }

            this.wasInInventory = false;
         }
      }
   }
}
