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























import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin({PlayerEntityRenderer.class})
public abstract class PlayerEntityRendererMixin {
   @Inject(
      method = {"<init>"},
      at = {@At("TAIL")}
   )
   private void obsidian$addShoulderPetFeature(Context context, boolean slim, CallbackInfo ci) {
      FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> rendererContext = (FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel>)this;
      ((LivingEntityRendererAccessor)this).obsidian$addFeature(new ObsidianComponent_313(rendererContext, context));
   }
}
