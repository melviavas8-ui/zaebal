package wtf.obsidian.engine;

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
























public class ObsidianComponent_95 extends DraggableHudElement {
   public ObsidianComponent_95(ObsidianComponent_280 draggable) {
      super(draggable);
   }

   @Override
   public void onRender(EventRender2D eventRender) {
      if (!ModuleManager.interfaceModule.style.is("Wave")) {
         this.DefaultStyle(eventRender);
      } else {
         this.WaveStyle(eventRender);
      }

      super.onRender(eventRender);
   }

   public void DefaultStyle(EventRender2D eventRender) {
      float x = this.draggable.getX();
      float y = this.draggable.getY();
      FontRenderer font = ObsidianComponent_201.getFont("suisse", 13);
      FontRenderer iconFont = ObsidianComponent_201.getFont("icon", 16);
      FontRenderer smallIconFont = ObsidianComponent_201.getFont("icon", 15);
      int colorTheme;
      if (!Obsidian.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")) {
         colorTheme = Obsidian.INSTANCE.themeStorage.getThemes().getTheme().color[0];
      } else {
         colorTheme = ObsidianComponent_olorUtil.getThemeColor();
      }

      boolean drawSquares = this.isUnusualRectType();
      int px = (int)Math.floor(mc.player.getX());
      int py = (int)Math.floor(mc.player.getY());
      int pz = (int)Math.floor(mc.player.getZ());
      float height = 16.0f;
      double bps = (double)MathLerpUtil.calculateBPS();
      String xValue = String.valueOf(px);
      String yValue = String.valueOf(py);
      String zValue = String.valueOf(pz);
      String coordsText = xValue + "x " + yValue + "y " + zValue + "z";
      String bpsValue = this.formatTwoDecimals(bps);
      String bpsSuffix = " b/s";
      float widthbps = font.getWidth(bpsValue + bpsSuffix);
      float xbps = x + 17.0f + widthbps;
      float widthCords = font.getWidth(coordsText);
      float totalWidth = 13.0f
         + widthCords
         + widthbps
         + 2.0f
         + 13.800000190734863f;
      RenderUtil.drawDefaultHudThemedPanel(
         eventRender.getContext().getMatrices(),
         x,
         y,
         totalWidth,
         height,
         3.0f,
         3.5f,
         colorTheme
      );
      if (drawSquares) {
         RenderUtil.drawHudSquarePattern(eventRender.getContext().getMatrices(), x, y, totalWidth, height, colorTheme);
      }

      float speedTextX = x + 13.5f;
      float bpsValueWidth = font.getWidth(bpsValue);
      font.draw(
         eventRender.getContext().getMatrices(),
         bpsValue,
         (double)speedTextX,
         (double)y + 6.6,
         -1
      );
      font.draw(
         eventRender.getContext().getMatrices(),
         bpsSuffix,
         (double)(speedTextX + bpsValueWidth - 2.0f),
         (double)y + 6.6,
         colorTheme
      );
      float coordsX = xbps + 9.0f;
      font.draw(
         eventRender.getContext().getMatrices(),
         xValue,
         (double)coordsX,
         (double)y + 6.6,
         -1
      );
      coordsX += font.getWidth(xValue);
      font.draw(
         eventRender.getContext().getMatrices(),
         "x",
         (double)(coordsX - 1.0f),
         (double)y + 6.6,
         colorTheme
      );
      coordsX += font.getWidth("x ");
      font.draw(
         eventRender.getContext().getMatrices(),
         yValue,
         (double)coordsX,
         (double)y + 6.6,
         -1
      );
      coordsX += font.getWidth(yValue);
      font.draw(
         eventRender.getContext().getMatrices(),
         "y",
         (double)(coordsX - 1.0f),
         (double)y + 6.6,
         colorTheme
      );
      coordsX += font.getWidth("y ");
      font.draw(
         eventRender.getContext().getMatrices(),
         zValue,
         (double)coordsX,
         (double)y + 6.6,
         -1
      );
      coordsX += font.getWidth(zValue);
      font.draw(
         eventRender.getContext().getMatrices(),
         "z",
         (double)(coordsX - 1.0F),
         (double)y + 6.6,
         colorTheme
      );
      iconFont.draw(
         eventRender.getContext().getMatrices(),
         "c",
         (double)x + 3.25,
         (double)y + 6.6,
         colorTheme
      );
      smallIconFont.draw(
         eventRender.getContext().getMatrices(),
         "x",
         (double)(xbps - 1.0f),
         (double)y + 6.85,
         colorTheme
      );
      this.draggable.setHeight(height);
      this.draggable.setWidth(totalWidth);
   }

   public void WaveStyle(EventRender2D eventRender) {
      float x = this.draggable.getX();
      float y = this.draggable.getY();
      float time = (float)(System.currentTimeMillis() % (2000L))
         / 2000.0f
         * 360.0f;
      int leftTop1 = ObsidianComponent_olorUtil.getThemeColor((int)time);
      int leftBottom1 = ObsidianComponent_olorUtil.getThemeColor((int)(time + 30.0f));
      int centerTop1 = ObsidianComponent_olorUtil.getThemeColor((int)(time + 90.0f));
      int centerBottom1 = ObsidianComponent_olorUtil.getThemeColor((int)(time + 120.0f));
      int rightTop1 = ObsidianComponent_olorUtil.getThemeColor((int)(time + 180.0f));
      int rightBottom1 = ObsidianComponent_olorUtil.getThemeColor((int)(time + 210.0f));
      String title = "coords";
      String xText = "x: " + (int)mc.player.getPos().getX();
      String yText = "y: " + (int)mc.player.getPos().getY();
      String zText = "z: " + (int)mc.player.getPos().getZ();
      FontRenderer font = ObsidianComponent_201.getFont("suisse", 15);
      float xWidth = font.getWidth(xText);
      float yWidth = font.getWidth(yText);
      float zWidth = font.getWidth(zText);
      float titleWidth = font.getWidth(title);
      float maxCoordWidth = Math.max(xWidth, Math.max(yWidth, zWidth));
      float padding = 9.0f;
      float rectWidth = maxCoordWidth + padding;
      float rectHeight = 40.0f;
      rectWidth = Math.max(rectWidth, 35.0f);
      float centerX = x + rectWidth / 2.0f;
      RenderUtil.drawWaveHudPanel(
         eventRender.getContext().getMatrices(),
         x,
         y,
         rectWidth,
         rectHeight,
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
         leftTop1,
         leftBottom1,
         centerTop1,
         centerBottom1,
         rightTop1,
         rightBottom1
      );
      float barPadding = 5.0f;
      RenderUtil.drawWaveHudHeader(
         eventRender.getContext().getMatrices(),
         x + barPadding,
         y + 12.0f,
         rectWidth - barPadding * 2.0f,
         2.5f,
         0.0f,
         10.0f,
         10.0f,
         leftTop1,
         leftBottom1,
         centerTop1,
         centerBottom1,
         rightTop1,
         rightBottom1
      );
      font.drawStringWithShadow(
         eventRender.getContext().getMatrices(),
         title,
         centerX - titleWidth / 2.0f,
         y + 5.0f,
         -1
      );
      font.drawStringWithShadow(
         eventRender.getContext().getMatrices(),
         xText,
         x + 4.5f,
         y + 17.0f,
         -1
      );
      font.drawStringWithShadow(
         eventRender.getContext().getMatrices(),
         yText,
         x + 4.5f,
         y + 24.0f,
         -1
      );
      font.drawStringWithShadow(
         eventRender.getContext().getMatrices(),
         zText,
         x + 4.5f,
         y + 31.0f,
         -1
      );
      float bpsX = x + rectWidth + 5.0f;
      double bps = (double)MathLerpUtil.calculateBPS();
      String bpsTitle = "bps";
      String bpsText = String.valueOf((int)bps);
      float bpsTitleWidth = font.getWidth(bpsTitle);
      float bpsTextWidth = font.getWidth(bpsText);
      float bpsRectWidth = Math.max(bpsTitleWidth, bpsTextWidth) + 10.0f;
      float bpsRectHeight = 25.0f;
      bpsRectWidth = Math.max(bpsRectWidth, 30.0f);
      float bpsCenterX = bpsX + bpsRectWidth / 2.0f;
      RenderUtil.drawWaveHudPanel(
         eventRender.getContext().getMatrices(),
         bpsX,
         y,
         bpsRectWidth,
         bpsRectHeight,
         ObsidianComponent_olorUtil.rgba(
            25, 25, 25, 150
         ),
         3.5f,
         0.0f,
         10.0f,
         10.0f,
         leftTop1,
         leftBottom1,
         centerTop1,
         centerBottom1,
         rightTop1,
         rightBottom1
      );
      RenderUtil.drawWaveHudHeader(
         eventRender.getContext().getMatrices(),
         bpsX + barPadding,
         y + 12.0f,
         bpsRectWidth - barPadding * 2.0f,
         2.5f,
         0.0f,
         10.0f,
         10.0f,
         leftTop1,
         leftBottom1,
         centerTop1,
         centerBottom1,
         rightTop1,
         rightBottom1
      );
      font.drawStringWithShadow(
         eventRender.getContext().getMatrices(),
         bpsTitle,
         bpsCenterX - bpsTitleWidth / 2.0f,
         y + 5.0f,
         -1
      );
      font.drawStringWithShadow(
         eventRender.getContext().getMatrices(),
         bpsText,
         bpsCenterX - bpsTextWidth / 2.0f,
         y + 17.0f,
         -1
      );
      float totalWidth = rectWidth + 5.0f + bpsRectWidth;
      this.draggable.setWidth(totalWidth);
      this.draggable.setHeight(rectHeight);
   }
   public String formatTwoDecimals(double value) {
      int scaled = (int)Math.round(value * 100.0);
      int fraction = Math.abs(scaled % (100));
      return scaled / 100 + "." + (fraction < (10) ? "0" : "") + fraction;
   }
}
