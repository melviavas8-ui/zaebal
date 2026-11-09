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
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;





@Mixin({LivingEntityRenderer.class})
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> implements IInstance {
   @Inject(
      method = {"updateRenderState"},
      at = {@At("TAIL")}
   )
   private void obsidian$updateSeeInvisiblesState(T entity, S state, float tickDelta, CallbackInfo ci) {
      boolean shouldRenderInvisible = this.obsidian$shouldRenderInvisible(entity);
      ((ObsidianComponent_144)state).obsidian$setSeeInvisiblesTarget(shouldRenderInvisible);
      if (shouldRenderInvisible) {
         state.invisible = true;
         state.invisibleToPlayer = false;
      }
   }

   @ModifyConstant(
      method = {"render"},
      constant = {@Constant(
         intValue = 654311423
      )}
   )
   private int obsidian$changeInvisibleAlpha(int original, S state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      return ((ObsidianComponent_144)state).obsidian$isSeeInvisiblesTarget() ? SeeInvisiblesModule.INVISIBLE_COLOR : original;
   }

   @Unique
   private boolean obsidian$shouldRenderInvisible(T entity) {
      if (entity instanceof PlayerEntity player && ModuleManager.INSTANCE != null) {
         SeeInvisiblesModule seeInvisibles = ModuleManager.seeInvisibles;
         return seeInvisibles != null && seeInvisibles.shouldRenderInvisible(player);
      }

      return false;
   }

   @Unique
   private PlayerEntity obsidian$resolvePlayer(S state) {
      if (state instanceof PlayerEntityRenderState playerState && mc.world != null) {
         return mc.world.getEntityById(playerState.id) instanceof PlayerEntity player ? player : null;
      }

      return null;
   }
}
