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























import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;



@Mixin({DrawContext.class})
public class DrawContextNameProtectMixin {
   @ModifyVariable(
      method = {"drawTextWithShadow"},
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 0
   )
   private String obsidian$patchStringShadow(String text) {
      return this.patch(text);
   }

   @ModifyVariable(
      method = {"drawText"},
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 0
   )
   private String obsidian$patchString(String text) {
      return this.patch(text);
   }

   private String patch(String text) {
      if (ModuleManager.INSTANCE == null) {
         return text;
      } else {
         NameProtectModule nameProtect = ModuleManager.nameProtect;
         return nameProtect != null && nameProtect.isEnable() ? nameProtect.patchIncomingText(text) : text;
      }
   }
}
