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

























import java.util.UUID;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket.Status;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;

public class RPSpooferModule extends Module {
   public static RPSpooferModule INSTANCE = new RPSpooferModule();

   public RPSpooferModule() {
      super("RPSpoofer", "Убирает ресурс-пак сервера", Category.PLAYER);
   }

   @EventTarget
   public void onReceivePacket(PacketEvent e) {
      if (e.getPacket() instanceof ResourcePackSendS2CPacket packet && (this.isEnable() || BotManager.shouldBypassResourcePacks())) {
         UUID packId = packet.id();
         mc.getNetworkHandler().sendPacket(new ResourcePackStatusC2SPacket(packId, Status.ACCEPTED));
         mc.getNetworkHandler().sendPacket(new ResourcePackStatusC2SPacket(packId, Status.SUCCESSFULLY_LOADED));
         e.setCancelled(true);
      }
   }
}
