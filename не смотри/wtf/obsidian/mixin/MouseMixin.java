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
import net.minecraft.client.Mouse;
import net.minecraft.util.math.Smoother;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;




@Mixin({Mouse.class})
public abstract class MouseMixin {
   @Shadow
   @Final
   private MinecraftClient client;
   @Shadow
   private double cursorDeltaX;
   @Shadow
   private double cursorDeltaY;
   @Shadow
   private Smoother cursorXSmoother;
   @Shadow
   private Smoother cursorYSmoother;

   @Inject(
      method = {"onMouseButton"},
      at = {@At("HEAD")},
      cancellable = false
   )
   private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
      try {
         if (this.client.player == null) {
            return;
         }

         int actionId = action == 1 ? 1 : 0;
         ObsidianComponent_349.callMouse(button, actionId);
      } catch (Exception var9) {
      }
   }

   @Inject(
      method = {"updateMouse"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onUpdateMouse(double timeDelta, CallbackInfo ci) {
      try {
         if (this.client.player == null) {
            return;
         }

         double sensitivity = (Double)this.client.options.getMouseSensitivity().getValue() * 0.6 + 0.2;
         double scaled = sensitivity * sensitivity * sensitivity * 8.0;
         double i;
         double j;
         if (this.client.options.smoothCameraEnabled) {
            i = this.cursorXSmoother.smooth(this.cursorDeltaX * scaled, timeDelta * scaled);
            j = this.cursorYSmoother.smooth(this.cursorDeltaY * scaled, timeDelta * scaled);
         } else if (this.client.options.getPerspective().isFirstPerson() && this.client.player.isUsingSpyglass()) {
            this.cursorXSmoother.clear();
            this.cursorYSmoother.clear();
            i = this.cursorDeltaX * sensitivity * sensitivity * sensitivity;
            j = this.cursorDeltaY * sensitivity * sensitivity * sensitivity;
         } else {
            this.cursorXSmoother.clear();
            this.cursorYSmoother.clear();
            i = this.cursorDeltaX * scaled;
            j = this.cursorDeltaY * scaled;
         }

         int invert = this.client.options.getInvertYMouse().getValue() ? -1 : 1;
         Event_289 event = new Event_289(i, j * (double)invert);
         EventManager.invoke(event);
         if (!event.isCancelled()) {
            this.client.getTutorialManager().onUpdateMouse(event.getYaw(), event.getPitch());
            this.client.player.changeLookDirection(event.getYaw(), event.getPitch());
         }

         this.cursorDeltaX = 0.0;
         this.cursorDeltaY = 0.0;
         ci.cancel();
      } catch (Exception var14) {
      }
   }
}
