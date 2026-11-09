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

























import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;

public class NoFallModule extends Module {
   public static NoFallModule INSTANCE = new NoFallModule();

   public NoFallModule() {
      super("NoFall", "Убирает урон от падения", Category.MOVEMENT);
   }

   @EventTarget
   public void onUpdate(EventTick ignored) {
      if (mc.player != null && mc.getNetworkHandler() != null) {
         if (!mc.player.isOnGround() && mc.player.fallDistance > 1.0f) {
            mc.getNetworkHandler()
               .sendPacket(
                  new Full(
                     mc.player.getX(),
                     mc.player.getY() + 1e-09,
                     mc.player.getZ(),
                     mc.player.getYaw(),
                     mc.player.getPitch(),
                     true,
                     false
                  )
               );
            mc.player.onLanding();
         }
      }
   }
}
