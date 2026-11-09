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























import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;



@Mixin({LivingEntity.class})
public abstract class LivingEntityMixin {
   @Inject(
      method = {"getHandSwingDuration"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetHandSwingDuration(CallbackInfoReturnable<Integer> cir) {
      if ((Object)this == MinecraftClient.getInstance().player) {
         if (ModuleManager.INSTANCE != null) {
            SwingAnimationsModule tweaks = ModuleManager.swingAnimations;
            if (tweaks != null && tweaks.isEnable() && tweaks.smoothEnabled.isState()) {
               cir.setReturnValue((int)tweaks.slowAnimationSpeed.get());
            }
         }
      }
   }
}
