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























import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin({Main.class})
public class MainMixin {
   @Inject(
      method = {"main"},
      at = {@At("HEAD")}
   )
   private static void onMain(String[] args, CallbackInfo ci) {
      if (Obsidian.INSTANCE.isServer) {
         try {
            Obsidian.INSTANCE.closeMinecraft();
         } catch (Exception var3) {
            var3.printStackTrace();
         }

         Obsidian.INSTANCE.isServer = false;
      }
   }
}
