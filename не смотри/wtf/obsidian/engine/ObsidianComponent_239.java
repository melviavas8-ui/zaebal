package wtf.obsidian.engine;

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
























import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.network.packet.Packet;

public final class ObsidianComponent_239 implements IInstance {
   private static final List<Packet<?>> silentPackets = new ArrayList<>();

   public static void sendSilentPacket(Packet<?> packet) {
      silentPackets.add(packet);
      mc.getNetworkHandler().sendPacket(packet);
   }

   public static void sendPacket(Packet<?> packet) {
      mc.getNetworkHandler().sendPacket(packet);
   }

   @Generated
   public ObsidianComponent_239() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   @Generated
   public static List<Packet<?>> getSilentPackets() {
      return silentPackets;
   }
}
