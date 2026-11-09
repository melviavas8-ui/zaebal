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























import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;







@Mixin({ClientPlayNetworkHandler.class})
public abstract class ClientPlayNetworkHandlerMixin {
   @Shadow
   private ClientWorld world;

   @Inject(
      method = {"sendChatMessage"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void sendChatMessage(@NotNull String message, CallbackInfo ci) {
      if (message.startsWith(Obsidian.INSTANCE.commandStorage.getPrefix())) {
         try {
            Obsidian.INSTANCE
               .commandStorage
               .getDispatcher()
               .execute(message.substring(Obsidian.INSTANCE.commandStorage.getPrefix().length()), Obsidian.INSTANCE.commandStorage.getSource());
         } catch (CommandSyntaxException var4) {
            ObsidianComponent_hatUtil.sendMessage(Formatting.RED + "Ошибка в использовании!");
         }

         ci.cancel();
      }
   }

   @Inject(
      method = {"onEntityVelocityUpdate"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onVelocityUpdate(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
      PacketEvent event = new PacketEvent(packet, PacketType.RECEIVE);
      event.call();
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"onExplosion"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onExplosion(ExplosionS2CPacket packet, CallbackInfo ci) {
      PacketEvent event = new PacketEvent(packet, PacketType.RECEIVE);
      event.call();
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"onEntityPositionSync"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onEntityPositionSync(EntityPositionSyncS2CPacket packet, CallbackInfo ci) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (this.world == null || mc.player == null || mc.world == null) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"onGameMessage"},
      at = {@At("HEAD")}
   )
   private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
      BaritoneHelper.onGameMessage(packet.content().getString());
   }

   @Inject(
      method = {"onGameJoin"},
      at = {@At("HEAD")}
   )
   private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
      BotManager.finishBotConnectStage();
   }
}
