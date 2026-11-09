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























import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;




@Mixin({GameRenderer.class})
public class GameRendererMixin {
   @Inject(
      method = {"showFloatingItem"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void obsidian$hideTotemAnimation(ItemStack stack, CallbackInfo ci) {
      if (ModuleManager.INSTANCE != null && stack != null && stack.isOf(Items.TOTEM_OF_UNDYING)) {
         RemovalsModule removals = ModuleManager.removals;
         if (removals != null && removals.isTotemAnimationDisabled()) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"render"},
      at = {@At("HEAD")}
   )
   private void obsidian$onRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      float tickDelta = tickCounter.getTickDelta(true);
      AimAssistModule.onRenderFrame(tickDelta);
   }
}
