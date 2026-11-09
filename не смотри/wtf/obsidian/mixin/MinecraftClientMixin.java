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























import java.lang.reflect.InvocationTargetException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;









@Mixin({MinecraftClient.class})
public abstract class MinecraftClientMixin {
   @Unique
   private long lastHookTime = Util.getMeasuringTimeNano();
   @Unique
   private int accumulatedCalls = 0;

   @Inject(
      method = {"tick"},
      at = {@At("HEAD")}
   )
   public void tick(CallbackInfo ci) throws InvocationTargetException, IllegalAccessException, InstantiationException {
      if (EventManager.hasListeners(Event_204.class)) {
         Event_204 event = new Event_204();
         EventManager.invoke(event);
      }

      ObsidianComponent_292.updateFPS();
   }

   @Inject(
      method = {"tick"},
      at = {@At("RETURN")}
   )
   public void tickEnd(CallbackInfo ci) throws InvocationTargetException, IllegalAccessException, InstantiationException {
      if (EventManager.hasListeners(Event_379.class)) {
         Event_379 event = new Event_379();
         EventManager.invoke(event);
      }

      BaritoneHelper.tick();
   }

   @Inject(
      method = {"render"},
      at = {@At("HEAD")}
   )
   private void render(boolean tick, CallbackInfo ci) throws InvocationTargetException, IllegalAccessException, InstantiationException {
      if (!EventManager.hasListeners(Event_93.class)) {
         this.lastHookTime = Util.getMeasuringTimeNano();
         this.accumulatedCalls = 0;
      } else {
         long now = Util.getMeasuringTimeNano();
         long delta = now - this.lastHookTime;
         this.accumulatedCalls += (int)(delta / 4166666L);
         this.lastHookTime = this.lastHookTime + (long)this.accumulatedCalls * 4166666L;

         for (this.accumulatedCalls = Math.min(this.accumulatedCalls, 240); this.accumulatedCalls > 0; this.accumulatedCalls--) {
            EventManager.invoke(new Event_93());
         }
      }
   }

   @Inject(
      method = {"hasOutline"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void obsidian$hasOutline(Entity entity, CallbackInfoReturnable<Boolean> cir) {
      if (ModuleManager.INSTANCE != null) {
         ShaderEspModule shaderEsp = ModuleManager.shaderEsp;
         if (shaderEsp != null && shaderEsp.shouldOutline(entity)) {
            cir.setReturnValue(true);
         }
      }
   }
}
