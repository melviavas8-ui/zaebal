package wtf.obsidian.event;

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
























import lombok.Generated;
import net.minecraft.network.packet.Packet;

public class PacketEvent extends Event {
   public final Packet<?> packet;
   public final PacketType type;

   @Generated
   public PacketEvent(Packet<?> packet, PacketType type) {
      this.packet = packet;
      this.type = type;
   }

   @Generated
   public Packet<?> getPacket() {
      return this.packet;
   }

   @Generated
   public PacketType getType() {
      return this.type;
   }
}
