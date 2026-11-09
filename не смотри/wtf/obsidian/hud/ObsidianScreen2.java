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
import net.minecraft.text.Text;

public class ObsidianScreen2 extends Screen implements IInstance {
   public final float WIDTH = 170.0f;
   public final float HEIGHT = 240.0f;

   public ObsidianScreen2() {
      super(Text.of("AutoBuy"));
   }

   public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      float X = (float)mw.getScaledWidth() / 2.0f
         - 170.0f;
      float Y = (float)mw.getScaledHeight() / 2.0f
         - 240.0f;
      RenderUtil.drawGradientRect(
         context.getMatrices(),
         X,
         Y,
         170.0f,
         240.0f,
         5.0f,
         ObsidianComponent_olorUtil.getThemeColor(),
         ObsidianComponent_olorUtil.darken(ObsidianComponent_olorUtil.getThemeColor(), 0.5f),
         true
      );
      super.render(context, mouseX, mouseY, delta);
   }
}
