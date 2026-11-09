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
























import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public class KeybindsHudElement3 extends DraggableHudElement {
   private final AnimationUtil widthAnimation = new AnimationUtil(
      80.0f, 10.5f, ObsidianComponent_57.QUAD_OUT
   );

   public KeybindsHudElement3(ObsidianComponent_280 draggable) {
      super(draggable);
   }
   public FontRenderer issue(int size) {
      return ObsidianComponent_201.getFont("suisse", size);
   }

   @Override
   public void onRender(EventRender2D eventRender) {
      List<ObsidianComponent_317> binds = this.getVisibleBinds();
      if (binds.isEmpty()) {
         this.widthAnimation.update(0.0f);
         this.draggable.setWidth(0.0f);
         this.draggable.setHeight(0.0f);
      } else {
         if (ModuleManager.interfaceModule.style.is("Обычный")) {
            this.DefaultStyle(eventRender, binds);
         } else {
            this.WaveStyle(eventRender, binds);
         }

         super.onRender(eventRender);
      }
   }
   public List<ObsidianComponent_317> getVisibleBinds() {
      ServerHelperModule serverHelper = ServerHelperModule.INSTANCE;
      List<ObsidianComponent_317> binds = new ArrayList<>();
      if (serverHelper == null) {
         return binds;
      } else {
         for (ObsidianComponent_317 bind : serverHelper.isLonyMode() ? serverHelper.getLonyHelperBinds() : serverHelper.getSpookyHelperBinds()) {
            if (bind.bind().getKey() != -1) {
               binds.add(bind);
            }
         }

         return binds;
      }
   }
   public void DefaultStyle(EventRender2D eventRender, List<ObsidianComponent_317> binds) {
      MatrixStack matrices = eventRender.getContext().getMatrices();
      float x = this.draggable.getX();
      float y = this.draggable.getY();
      int colorTheme = this.getThemeColor();
      int fontSize = 13;
      FontRenderer keyFont = this.issue(fontSize);
      float height = 19.0f;
      float itemSize = 9.800000190734863f;
      float itemScale = 0.6100000143051147f;
      float fontGap = 2.799999952316284f;
      float cellGap = 5.0f;
      float sidePadding = 6.0f;
      float width = this.getCompactWidth(binds, keyFont, itemSize, fontGap, cellGap, sidePadding, 60.0f);
      this.widthAnimation.update(width);
      float animatedWidth = this.widthAnimation.getValue();
      this.drawDefaultPanel(matrices, x, y, animatedWidth, height, colorTheme);
      if (binds.isEmpty()) {
         this.issue(12)
            .draw(
               matrices,
               "Helper",
               x + 5.0f,
               y + 6.0f,
               ObsidianComponent_olorUtil.rgba(255, 255, 255, 230)
            );
         this.draggable.setWidth(animatedWidth);
         this.draggable.setHeight(height);
      } else {
         this.drawCompactBinds(
            eventRender.getContext(),
            binds,
            keyFont,
            x,
            y,
            height,
            itemSize,
            itemScale,
            fontGap,
            cellGap,
            sidePadding,
            8.199999809265137f
         );
         this.draggable.setWidth(animatedWidth);
         this.draggable.setHeight(height);
      }
   }
   public void WaveStyle(EventRender2D eventRender, List<ObsidianComponent_317> binds) {
      MatrixStack matrices = eventRender.getContext().getMatrices();
      float x = this.draggable.getX();
      float y = this.draggable.getY();
      int time = (int)(
         (float)(System.currentTimeMillis() % (2000L))
            / 2000.0f
            * 360.0f
      );
      int leftTop = ObsidianComponent_olorUtil.getThemeColor(time);
      int leftBottom = ObsidianComponent_olorUtil.getThemeColor(time + (30));
      int centerTop = ObsidianComponent_olorUtil.getThemeColor(time + 90);
      int centerBottom = ObsidianComponent_olorUtil.getThemeColor(time + (120));
      int rightTop = ObsidianComponent_olorUtil.getThemeColor(time + (180));
      int rightBottom = ObsidianComponent_olorUtil.getThemeColor(time + 210);
      FontRenderer keyFont = this.issue(14);
      float height = 22.0f;
      float itemSize = 11.0f;
      float itemScale = 0.6899999976158142f;
      float fontGap = 3.5f;
      float cellGap = 6.0f;
      float sidePadding = 7.0f;
      float width = this.getCompactWidth(
         binds, keyFont, itemSize, fontGap, cellGap, sidePadding, 72.0f
      );
      this.widthAnimation.update(width);
      float animatedWidth = this.widthAnimation.getValue();
      if (binds.isEmpty()) {
         RenderUtil.drawWaveHudHeader(
            matrices,
            x,
            y,
            animatedWidth,
            15.0f,
            0.0f,
            10.0f,
            10.0f,
            leftTop,
            leftBottom,
            centerTop,
            centerBottom,
            rightTop,
            rightBottom
         );
         String title = "helper";
         float titleX = x
            + (animatedWidth - this.issue(15).getWidth(title))
               / 2.0f;
         this.issue(15)
            .drawStringWithShadow(matrices, title, titleX, y + 5.0f, -1);
         this.draggable.setWidth(animatedWidth);
         this.draggable.setHeight(18.0f);
      } else {
         RenderUtil.drawWaveHudPanel(
            matrices,
            x,
            y,
            animatedWidth,
            height,
            ObsidianComponent_olorUtil.rgba(
               25,
               25,
               25,
               150
            ),
            3.5f,
            0.0f,
            10.0f,
            10.0f,
            leftTop,
            leftBottom,
            centerTop,
            centerBottom,
            rightTop,
            rightBottom
         );
         this.drawCompactBinds(
            eventRender.getContext(),
            binds,
            keyFont,
            x,
            y,
            height,
            itemSize,
            itemScale,
            fontGap,
            cellGap,
            sidePadding,
            9.5f
         );
         this.draggable.setWidth(animatedWidth);
         this.draggable.setHeight(height);
      }
   }
   public float getCompactWidth(List<ObsidianComponent_317> binds, FontRenderer keyFont, float itemSize, float fontGap, float cellGap, float sidePadding, float emptyWidth) {
      if (binds.isEmpty()) {
         return emptyWidth;
      } else {
         float width = sidePadding * 2.0f;

         for (int i = 0; i < binds.size(); i++) {
            String keyName = ObsidianComponent_349.getBindName(binds.get(i).bind().getKey());
            width += itemSize + fontGap + keyFont.getWidth(keyName);
            if (i < binds.size() - 1) {
               width += cellGap;
            }
         }

         return width;
      }
   }
   public void drawCompactBinds(
      DrawContext context,
      List<ObsidianComponent_317> binds,
      FontRenderer keyFont,
      float x,
      float y,
      float height,
      float itemSize,
      float itemScale,
      float fontGap,
      float cellGap,
      float sidePadding,
      float textOffsetY
   ) {
      MatrixStack matrices = context.getMatrices();
      float offsetX = x + sidePadding;
      float itemY = y + (height - itemSize) * 0.5f;
      float textY = y + textOffsetY;

      for (int i = 0; i < binds.size(); i++) {
         ObsidianComponent_317 bind = binds.get(i);
         String keyName = ObsidianComponent_349.getBindName(bind.bind().getKey());
         this.drawItemIcon(context, new ItemStack(bind.item()), offsetX, itemY, itemScale);
         keyFont.draw(
            matrices,
            keyName,
            offsetX + itemSize + fontGap,
            textY,
            ObsidianComponent_olorUtil.rgba(
               255, 255, 255, 240
            )
         );
         offsetX += itemSize + fontGap + keyFont.getWidth(keyName);
         if (i < binds.size() - (1)) {
            offsetX += cellGap;
         }
      }
   }
   public void drawItemIcon(DrawContext context, ItemStack stack, float x, float y, float scale) {
      MatrixStack matrices = context.getMatrices();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      matrices.push();
      matrices.translate(x, y, 0.0f);
      matrices.scale(scale, scale, 1.0f);
      context.drawItem(stack, 0, 0);
      matrices.pop();
      RenderSystem.depthMask(false);
      RenderSystem.disableDepthTest();
   }
   public void drawDefaultPanel(MatrixStack matrices, float x, float y, float width, float height, int colorTheme) {
      RenderUtil.drawDefaultHudThemedPanel(
         matrices,
         x,
         y,
         width,
         height,
         3.0f,
         3.5f,
         colorTheme
      );
      if (this.isUnusualRectType()) {
         RenderUtil.drawHudSquarePattern(matrices, x, y, width, height, colorTheme);
      }
   }
   public int getThemeColor() {
      return !Obsidian.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")
         ? Obsidian.INSTANCE.themeStorage.getThemes().getTheme().color[0]
         : ObsidianComponent_olorUtil.getThemeColor();
   }
}
