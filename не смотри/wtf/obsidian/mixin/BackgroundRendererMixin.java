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























import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FogShape;
import net.minecraft.client.render.BackgroundRenderer.FogType;
import net.minecraft.entity.Entity;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;





@Mixin({BackgroundRenderer.class})
public class BackgroundRendererMixin {
   @Inject(
      method = {"getFogModifier"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void obsidian$getFogModifier(Entity entity, float tickDelta, CallbackInfoReturnable<Object> cir) {
      if (ModuleManager.INSTANCE != null) {
         RemovalsModule removals = ModuleManager.removals;
         if (removals != null && removals.isEnabled("Плохие эффекты")) {
            cir.setReturnValue(null);
         }
      }
   }

   @Inject(
      method = {"applyFog"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private static void obsidian$applyFog(
      Camera camera, FogType fogType, Vector4f color, float viewDistance, boolean thickenFog, float tickDelta, CallbackInfoReturnable<Fog> cir
   ) {
      if (ModuleManager.INSTANCE != null) {
         CustomWorldModule tweaks = ModuleManager.worldTweaks;
         if (tweaks != null && tweaks.isFogEnabled()) {
            float fogDistance = Math.max(12.0F, tweaks.getFogDistance());
            float fogEnd = Math.min(viewDistance, fogDistance);
            float fogStart = Math.max(0.0F, fogEnd * 0.05F);
            int color1 = tweaks.getFogColor();
            cir.setReturnValue(new Fog(fogStart, fogEnd, FogShape.SPHERE, ObsidianComponent_olorUtil.redf(color1), ObsidianComponent_olorUtil.greenf(color1), ObsidianComponent_olorUtil.bluef(color1), 1.0F));
         }
      }
   }
}
