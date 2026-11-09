package wtf.obsidian.modules.combat;

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

























import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;

public class PacketCriticalsModule extends Module {
   public static PacketCriticalsModule INSTANCE = new PacketCriticalsModule();

   public PacketCriticalsModule() {
      super("PacketCriticals", "Бьет критами под эффект плавного падения / в паутине", Category.COMBAT);
   }

   @EventTarget
   public void onAttack(Event_358 event) {
      if (mc.player != null && mc.world != null) {
         boolean inWeb = ObsidianComponent_243.isInCobweb();
         double x = mc.player.getX();
         double y = mc.player.getY();
         double z = mc.player.getZ();
         if (inWeb) {
            mc.player
               .networkHandler
               .sendPacket(
                  new PositionAndOnGround(
                     x,
                     y + 0.003,
                     z,
                     false,
                     false
                  )
               );
            mc.player
               .networkHandler
               .sendPacket(new PositionAndOnGround(x, y, z, false, false));
         }
      }
   }
}
