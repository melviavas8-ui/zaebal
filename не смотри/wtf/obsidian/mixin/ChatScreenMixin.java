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
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;





@Mixin({ChatScreen.class})
public class ChatScreenMixin {
   @Unique
   private boolean obsidian$leftPressed;

   @Inject(
      method = {"mouseClicked"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
      if (ModuleManager.interfaceModule.handleHudContextClick(mouseX, mouseY, button)) {
         cir.setReturnValue(true);
      } else {
         for (ObsidianComponent_280 draggable : ObsidianComponent_148.draggables.values()) {
            if (draggable.getModule().isEnable() && draggable.onClick(mouseX, mouseY, button)) {
               cir.setReturnValue(true);
               return;
            }
         }
      }
   }

   @Inject(
      method = {"render"},
      at = {@At("HEAD")}
   )
   private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      MinecraftClient mc = MinecraftClient.getInstance();
      Window window = mc.getWindow();
      boolean leftPressed = GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), 0) == 1;
      if (this.obsidian$leftPressed && !leftPressed) {
         for (ObsidianComponent_280 draggable : ObsidianComponent_148.draggables.values()) {
            draggable.onRelease(0);
         }
      }

      this.obsidian$leftPressed = leftPressed;

      for (ObsidianComponent_280 draggable : ObsidianComponent_148.draggables.values()) {
         if (draggable.getModule().isEnable()) {
            draggable.onDraw(mouseX, mouseY, window, context.getMatrices());
         }
      }

      ModuleManager.interfaceModule.renderHudContextMenu(context, mouseX, mouseY);
   }

   @Inject(
      method = {"removed"},
      at = {@At("HEAD")}
   )
   private void onRemoved(CallbackInfo ci) {
      this.obsidian$leftPressed = false;

      for (ObsidianComponent_280 draggable : ObsidianComponent_148.draggables.values()) {
         draggable.onRelease(0);
      }

      try {
         Obsidian.INSTANCE.configStorage.saveConfig(Obsidian.INSTANCE.configStorage.currentConfig);
      } catch (Exception var4) {
         var4.printStackTrace();
      }
   }
}
