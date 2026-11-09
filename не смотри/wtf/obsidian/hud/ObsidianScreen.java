package wtf.obsidian.hud;

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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class ObsidianScreen extends Screen implements IInstance {
   private static final ObsidianComponent_271 SHARED_STATE = new ObsidianComponent_271();
   private final int categoryCount = Category.values().length;
   public final ObsidianComponent_271 state = SHARED_STATE;
   private final ObsidianComponent_325 themeSelector = new ObsidianComponent_325();
   private final ObsidianComponent_218 renderer = new ObsidianComponent_218(this.state, new ObsidianComponent_190(), this.themeSelector);
   private final ObsidianComponent_248 inputHandler = new ObsidianComponent_248(this.state, this.themeSelector, this.renderer.getConfigManager());
   private final AnimationUtil openAnimation = new AnimationUtil(
      0.0f, 7.5f, ObsidianComponent_57.CUBIC_OUT
   );
   public boolean closing;
   public boolean closeSoundPlayed;

   public ObsidianScreen() {
      super(Text.of("ClickGui"));
      this.state.refreshModules();
   }
   public Window getWindow() {
      return mc == null ? null : mc.getWindow();
   }
   public void syncLayout() {
      Window window = this.getWindow();
      if (window != null) {
         this.state.updatePosition(window, this.categoryCount);
         this.renderer.getConfigManager().updatePosition(window, this.categoryCount);
      }
   }

   public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      Window window = this.getWindow();
      if (window != null) {
         this.updateAnimation();
         float progress = this.getAnimationProgress();
         if (this.closing && progress <= 0.0010000000474974513f) {
            if (mc != null) {
               mc.setScreen(null);
            }
         } else {
            this.state.updatePosition(window, this.categoryCount);
            this.state.setRenderOffsetY(this.getPanelOffsetY(progress));
            this.renderer.render(context, mouseX, mouseY, window, progress);
            super.render(context, mouseX, mouseY, delta);
         }
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.closing) {
         return true;
      } else {
         this.syncLayout();
         this.state.setRenderOffsetY(this.getPanelOffsetY(this.getAnimationProgress()));
         return (!(!this.inputHandler.mouseClicked(mouseX, mouseY, button, this.getWindow()) && !super.mouseClicked(mouseX, mouseY, button)));
      }
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (this.closing) {
         return true;
      } else {
         this.syncLayout();
         return (!(!this.inputHandler.mouseReleased(button) && !super.mouseReleased(mouseX, mouseY, button)));
      }
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.closing) {
         return true;
      } else {
         this.syncLayout();
         this.state.setRenderOffsetY(this.getPanelOffsetY(this.getAnimationProgress()));
         return (!(!this.inputHandler.mouseDragged(mouseX, mouseY, button) && !super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)));
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      if (this.closing) {
         return true;
      } else {
         this.syncLayout();
         this.state.setRenderOffsetY(this.getPanelOffsetY(this.getAnimationProgress()));
         return (!(!this.inputHandler.mouseScrolled(mouseX, mouseY, verticalAmount)
               && !super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)));
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.closing) {
         return true;
      } else if (this.inputHandler.keyPressed(keyCode, modifiers)) {
         return true;
      } else if (keyCode == 256) {
         this.startClosing();
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public boolean charTyped(char chr, int modifiers) {
      if (this.closing) {
         return true;
      } else {
         return (!(!this.inputHandler.charTyped(chr) && !super.charTyped(chr, modifiers)));
      }
   }

   public void close() {
      this.startClosing();
   }

   public void removed() {
      if (!this.closeSoundPlayed) {
         this.closeSoundPlayed = true;
         SoundManager.playSound(
            "closegui.wav",
            0.6,
            1.0f
         );
      }

      super.removed();
   }
   public void startClosing() {
      if (!this.closing) {
         this.closing = true;
         this.openAnimation.setEasing(ObsidianComponent_57.CUBIC_IN);
         if (!this.closeSoundPlayed) {
            this.closeSoundPlayed = true;
            SoundManager.playSound(
               "closegui.wav",
               0.6,
               1.0f
            );
         }
      }
   }
   public void updateAnimation() {
      if (this.closing) {
         this.openAnimation.update(0.0f);
      } else {
         this.openAnimation.setEasing(ObsidianComponent_57.CUBIC_OUT);
         this.openAnimation.update(1.0f);
      }
   }
   public float getAnimationProgress() {
      return MathHelper.clamp(
         this.openAnimation.getValue(), 0.0f, 1.0f
      );
   }
   public float getPanelOffsetY(float progress) {
      return (1.0f - progress) * 22.0f;
   }
}
