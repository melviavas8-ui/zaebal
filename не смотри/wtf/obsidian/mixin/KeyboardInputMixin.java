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
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;



@Mixin({KeyboardInput.class})
public abstract class KeyboardInputMixin extends Input {
   private static final MinecraftClient mc = MinecraftClient.getInstance();

   @Inject(
      method = {"tick"},
      at = {@At("TAIL")}
   )
   private void onTickTail(CallbackInfo ci) {
      if (EventManager.hasListeners(Event_102.class)) {
         Event_102 eventInput = new Event_102(this.movementForward, this.movementSideways, this.playerInput.jump(), this.playerInput.sneak());
         eventInput.call();
         float forward = eventInput.getForward();
         float strafe = eventInput.getStrafe();
         this.playerInput = new PlayerInput(
            forward > 0.0F, forward < 0.0F, strafe > 0.0F, strafe < 0.0F, eventInput.isJump(), eventInput.isSneak(), this.playerInput.sprint()
         );
         this.movementForward = forward;
         this.movementSideways = strafe;
      }
   }
}
