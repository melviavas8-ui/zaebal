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























import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;



@Mixin({ClientPlayerInteractionManager.class})
public abstract class ClientPlayerInteractionManagerMixin {
   @Inject(
      method = {"attackEntity"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void attackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
      try {
         if (player != null && target != null) {
            Event_358 event = new Event_358(player, target);
            EventManager.invoke(event);
            if (event.isCancelled()) {
               ci.cancel();
            }
         }
      } catch (Exception var5) {
      }
   }
}
