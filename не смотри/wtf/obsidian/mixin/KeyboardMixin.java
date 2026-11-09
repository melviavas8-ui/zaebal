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























import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;




@Mixin({Keyboard.class})
public class KeyboardMixin implements IInstance {
   @Inject(
      method = {"onKey"},
      at = {@At("HEAD")}
   )
   public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
      if (mc.currentScreen == null) {
         ObsidianComponent_349.call(key, action);
      }
   }

   @Inject(
      method = {"processF3"},
      at = {@At("RETURN")}
   )
   private void processF3(int key, CallbackInfoReturnable<Boolean> cir) {
      if (key == 65 && (Boolean)cir.getReturnValue()) {
         new Event_114().call();
      }
   }
}
