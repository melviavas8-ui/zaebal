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























import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;






@Mixin({Entity.class})
public abstract class EntityMixin implements IInstance {
   @ModifyExpressionValue(
      method = {"move"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/Entity;isControlledByPlayer()Z"
      )}
   )
   private boolean fixFallDistanceCalculation(boolean original) {
      return (Object)this == mc.player ? false : original;
   }

   @Inject(
      method = {"pushAwayFrom"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void pushAwayFrom(CallbackInfo ci) {
      if ((Object)this == mc.player && ModuleManager.INSTANCE != null) {
         NoPushModule noPush = ModuleManager.noPush;
         if (noPush != null && noPush.isEnable() && noPush.getCollisionList().is("Игроки")) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"isPushedByFluids"},
      at = {@At("RETURN")},
      cancellable = true
   )
   public void isPushedByFluids(CallbackInfoReturnable<Boolean> ci) {
      if ((Object)this == mc.player && ModuleManager.INSTANCE != null) {
         NoPushModule noPush = ModuleManager.noPush;
         if (noPush != null && noPush.isEnable() && noPush.getCollisionList().is("Вода")) {
            ci.setReturnValue(false);
         }
      }
   }

   @Inject(
      method = {"getTeamColorValue"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void obsidian$getTeamColorValue(CallbackInfoReturnable<Integer> cir) {
      if (ModuleManager.INSTANCE != null) {
         ShaderEspModule shaderEsp = ModuleManager.shaderEsp;
         if (shaderEsp != null && shaderEsp.shouldOutline((Entity)(Object)this)) {
            cir.setReturnValue(shaderEsp.getOutlineColor());
         }
      }
   }

   @Inject(
      method = {"isInvisibleTo"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void obsidian$allowSeeInvisibles(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
      if ((Object)this instanceof PlayerEntity target && ModuleManager.INSTANCE != null) {
         SeeInvisiblesModule seeInvisibles = ModuleManager.seeInvisibles;
         if (seeInvisibles != null && seeInvisibles.shouldRenderInvisible(target)) {
            cir.setReturnValue(false);
         }

         return;
      }
   }
}
