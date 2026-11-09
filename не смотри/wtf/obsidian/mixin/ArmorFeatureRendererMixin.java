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























import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;




@Mixin({ArmorFeatureRenderer.class})
public class ArmorFeatureRendererMixin implements IInstance {
   @Inject(
      method = {"render"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void obsidian$hideArmor(
      MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, EntityRenderState state, float limbAngle, float limbDistance, CallbackInfo ci
   ) {
      if (state instanceof PlayerEntityRenderState playerState && ModuleManager.INSTANCE != null && mc.world != null) {
         ChamsModule chams = ModuleManager.chams;
         if (chams != null && chams.isEnable()) {
            if (mc.world.getEntityById(playerState.id) instanceof PlayerEntity player && chams.shouldHideItemsAndCape(player)) {
               ci.cancel();
            }

            return;
         }

         return;
      }
   }
}
