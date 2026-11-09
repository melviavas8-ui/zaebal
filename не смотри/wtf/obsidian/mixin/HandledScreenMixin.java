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
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin({HandledScreen.class})
public abstract class HandledScreenMixin {
   @Shadow
   @Nullable
   protected abstract Slot getSlotAt(double var1, double var3);

   @Shadow
   protected abstract void onMouseClick(@Nullable Slot var1, int var2, int var3, SlotActionType var4);

   @Inject(
      method = {"render"},
      at = {@At("HEAD")}
   )
   private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      MinecraftClient mc = MinecraftClient.getInstance();
      ItemScrollerModule itemScroller = ItemScrollerModule.INSTANCE;
      if (itemScroller.isEnable() && mc.player != null && mc.interactionManager != null) {
         long window = mc.getWindow().getHandle();
         boolean leftMousePressed = GLFW.glfwGetMouseButton(window, 0) == 1;
         boolean shiftPressed = GLFW.glfwGetKey(window, 340) == 1 || GLFW.glfwGetKey(window, 344) == 1;
         if (leftMousePressed && shiftPressed) {
            Slot slot = this.getSlotAt((double)mouseX, (double)mouseY);
            if (slot != null && slot.hasStack()) {
               if (itemScroller.canQuickMove()) {
                  this.onMouseClick(slot, slot.id, 0, SlotActionType.QUICK_MOVE);
               }
            }
         } else {
            itemScroller.resetTimer();
         }
      }
   }
}
