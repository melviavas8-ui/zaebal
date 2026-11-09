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























import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;



@Mixin({InGameOverlayRenderer.class})
public class InGameOverlayRendererMixin {
   @Inject(
      method = {"renderFireOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void obsidian$renderFireOverlay(MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
      if (ModuleManager.INSTANCE != null) {
         RemovalsModule removals = ModuleManager.removals;
         if (removals != null && removals.isEnabled("Огонь")) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"renderInWallOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void obsidian$renderInWallOverlay(Sprite sprite, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
      if (ModuleManager.INSTANCE != null) {
         RemovalsModule removals = ModuleManager.removals;
         if (removals != null && removals.isEnabled("Оверлей в блоке")) {
            ci.cancel();
         }
      }
   }
}
