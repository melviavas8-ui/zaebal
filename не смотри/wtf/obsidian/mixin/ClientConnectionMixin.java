package wtf.obsidian.mixin;

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























import io.netty.channel.ChannelHandlerContext;
import java.lang.reflect.InvocationTargetException;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;





@Mixin({ClientConnection.class})
public abstract class ClientConnectionMixin {
   @Inject(
      method = {"channelRead0"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void channelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) throws InvocationTargetException, IllegalAccessException, InstantiationException {
      PacketEvent eventReceive = new PacketEvent(packet, PacketType.RECEIVE);
      EventManager.invoke(eventReceive);
      if (eventReceive.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"send"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void send(Packet<?> packet, CallbackInfo ci) throws InvocationTargetException, IllegalAccessException, InstantiationException {
      if (ObsidianComponent_239.getSilentPackets().contains(packet)) {
         ObsidianComponent_239.getSilentPackets().remove(packet);
      } else {
         PacketEvent eventSend = new PacketEvent(packet, PacketType.SEND);
         EventManager.invoke(eventSend);
         if (eventSend.isCancelled()) {
            ci.cancel();
         }
      }
   }
}
