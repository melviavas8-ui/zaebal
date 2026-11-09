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

























import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.util.math.MatrixStack;

public class ArrayListHudElement3 extends DraggableHudElement {
   public static final float LINE_HEIGHT = 9.5F;
   public static final float FLOW_SPEED = 1000.0F;
   private static final Comparator<ObsidianComponent_61> MODULE_WIDTH_COMPARATOR = Comparator.<ObsidianComponent_61>comparingDouble(entry -> (double)entry.width).reversed();
   private final List<ObsidianComponent_61> visibleModules = new ArrayList<>();

   public ArrayListHudElement3(ObsidianComponent_280 draggable) {
      super(draggable);
   }
   public FontRenderer font() {
      return ObsidianComponent_201.getFont("suisse", 14);
   }
   public void drawFlowingText(MatrixStack matrices, FontRenderer font, String text, float x, float y, int color, float alphaMul) {
      int textColor = ObsidianComponent_olorUtil.setAlphaColor(color, (int)(255.0f * alphaMul));
      font.draw(matrices, text, x, y, textColor);
   }

   @Override
   public void onRender(EventRender2D eventRender) {
      MatrixStack matrices = eventRender.getContext().getMatrices();
      FontRenderer font = this.font();
      List<Module> modules = ModuleManager.INSTANCE.getObject();
      this.visibleModules.clear();

      for (Module module : modules) {
         module.getArrayAnimka()
            .update(module.isEnable() ? 1.0f : 0.0f);
         float anim = module.getArrayAnimka().getValue();
         if (!(anim <= 0.029999999329447746f)) {
            String displayName = module.getDisplayName();
            this.visibleModules.add(new ObsidianComponent_61(displayName.toLowerCase(), font.getWidth(displayName), anim));
         }
      }

      this.visibleModules.sort(MODULE_WIDTH_COMPARATOR);
      long now = System.currentTimeMillis();
      float x = this.draggable.getX();
      float y = this.draggable.getY();
      float maxWidth = 0.0f;
      boolean leftHalf = (x <= (float)mc.getWindow().getScaledWidth() * 0.5f);

      for (ObsidianComponent_61 entry : this.visibleModules) {
         maxWidth = Math.max(maxWidth, entry.width);
      }

      float yOffset = 0.0f;

      for (int i = 0; i < this.visibleModules.size(); i++) {
         ObsidianComponent_61 entry = this.visibleModules.get(i);
         float anim = entry.anim;
         float lineStep = 9.5f * anim;
         int indexShift = (int)((float)now * 1000.0f / 10.0f)
            + i * (42);
         int rowColor = ObsidianComponent_olorUtil.getThemeColor(indexShift);
         int rowColor2 = ObsidianComponent_olorUtil.getThemeColor(indexShift + 90);
         int glowAlpha = (int)((float)(leftHalf ? 140 : 170) * anim);
         int glow1 = ObsidianComponent_olorUtil.setAlphaColor(rowColor, glowAlpha);
         int glow2 = ObsidianComponent_olorUtil.setAlphaColor(rowColor2, glowAlpha);
         float textWidth = entry.width;
         float drawX;
         if (leftHalf) {
            drawX = x - 3.0f;
         } else {
            drawX = x + (maxWidth - textWidth) - 3.0f;
         }

         float drawY = y
            + yOffset
            + (1.0f - anim) * 7.0f;
         float shadowX = leftHalf
            ? drawX - 0.6000000238418579f
            : drawX - 1.5f;
         float shadowW = leftHalf ? textWidth - 4.0f : textWidth;
         RenderUtil.drawShadow(
            matrices,
            shadowX,
            drawY,
            shadowW,
            6.0f,
            5.0f,
            11.0f,
            glow2,
            glow2,
            glow1,
            glow1
         );
         float textX = leftHalf
            ? drawX - 0.800000011920929f
            : drawX - 2.0f;
         this.drawFlowingText(
            matrices, font, entry.lowerName, textX, drawY + 1.5f, rowColor, anim
         );
         yOffset += lineStep;
      }

      if (yOffset > 0.5f) {
         float lineX = leftHalf
            ? x - 6.5f
            : x + maxWidth - 7.0f;
         float lineWidth = 2.5f;
         int topLineColor = ObsidianComponent_olorUtil.setAlphaColor(ObsidianComponent_olorUtil.getThemeColor(0), 220);
         int bottomLineColor = ObsidianComponent_olorUtil.setAlphaColor(ObsidianComponent_olorUtil.getThemeColor(180), 220);
         RenderUtil.drawGradientRect(
            matrices,
            lineX,
            y,
            lineWidth,
            yOffset - 2.0f,
            0.0f,
            topLineColor,
            bottomLineColor
         );
      }

      this.draggable.setWidth(maxWidth + 4.0f);
      this.draggable.setHeight(yOffset);
      super.onRender(eventRender);
   }
}
