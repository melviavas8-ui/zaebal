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























import net.minecraft.client.world.ClientWorld.Properties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;



@Mixin({Properties.class})
public class ClientWorldPropertiesMixin {
   @Shadow
   private long timeOfDay;

   @Inject(
      method = {"setTimeOfDay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void obsidian$setTimeOfDay(long timeOfDay, CallbackInfo ci) {
      if (ModuleManager.INSTANCE != null) {
         CustomWorldModule tweaks = ModuleManager.worldTweaks;
         if (tweaks != null && tweaks.isTimeEnabled()) {
            this.timeOfDay = tweaks.getForcedTime();
            ci.cancel();
         }
      }
   }
}
