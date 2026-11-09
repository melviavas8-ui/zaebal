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
























import java.awt.Color;
import java.util.Arrays;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.math.MatrixStack;

public class KeybindsHudElement2 extends DraggableHudElement {
   private boolean showFps = true;
   public boolean showMs = true;
   public boolean showServer = true;
   public boolean showTps = true;

   public static String getUsername() {
      return "t.me/obsidiandlc";
   }

   public static String getUID() {
      return "1";
   }

   public KeybindsHudElement2(ObsidianComponent_280 draggable) {
      super(draggable);
   }

   public boolean isShowFps() {
      return this.showFps;
   }

   public void setShowFps(boolean showFps) {
      this.showFps = showFps;
   }

   public boolean isShowMs() {
      return this.showMs;
   }

   public void setShowMs(boolean showMs) {
      this.showMs = showMs;
   }

   public boolean isShowServer() {
      return this.showServer;
   }

   public void setShowServer(boolean showServer) {
      this.showServer = showServer;
   }

   public boolean isShowTps() {
      return this.showTps;
   }

   public void setShowTps(boolean showTps) {
      this.showTps = showTps;
   }

   @Override
   public void onRender(EventRender2D eventRender) {
      if (ModuleManager.interfaceModule.style.is("Obs")) {
         this.WaveStyle(eventRender);
      } else {
         this.DefaultStyle(eventRender);
      }

      super.onRender(eventRender);
   }

   public void DefaultStyle(EventRender2D eventRender) {
      MatrixStack matrices = eventRender.getContext().getMatrices();
      float x = this.draggable.getX();
      float y = this.draggable.getY();
      FontRenderer logoFont = ObsidianComponent_201.getFont("logo", 17);
      FontRenderer iconNew14 = ObsidianComponent_201.getFont("iconnew", 14);
      FontRenderer iconNew15 = ObsidianComponent_201.getFont("iconnew", 15);
      FontRenderer icon14 = ObsidianComponent_201.getFont("icon", 14);
      FontRenderer statsIconFont = ObsidianComponent_201.getFont("wonderful", 14);
      if (statsIconFont == null) {
         statsIconFont = iconNew14 != null ? iconNew14 : icon14;
      }

      FontRenderer suisse13 = ObsidianComponent_201.getFont("suisse", 13);
      float wonderfulRectH = 16.0f;
      int iconSize = 17;
      String iconGlyph = "A";
      float iconW = logoFont.getStringWidth(iconGlyph);
      float iconX = x
         + (17.0f - iconW) / 2.0f;
      float iconY = y + 5.5f;
      int iconTop;
      if (!Obsidian.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")) {
         iconTop = Obsidian.INSTANCE.themeStorage.getThemes().getTheme().color[0];
      } else {
         iconTop = ObsidianComponent_olorUtil.getThemeColor();
      }

      boolean drawSquares = this.isUnusualRectType();
      float rect2Pad = 3.0f;
      String username = getUsername();
      String UID = getUID();
      int whiteColor = new Color(
            255,
            255,
            255,
            255
         )
         .getRGB();
      float textY = y + 6.800000190734863f;
      String brandText = "";
      float brandTextX = iconX + iconW + 2.5f;
      float brandTextW = suisse13.getStringWidth(brandText);
      float wonderfulRectW = brandTextX + brandTextW + 1.5f - x;
      RenderUtil.drawDefaultHudThemedPanel(
         matrices,
         x,
         y,
         wonderfulRectW,
         wonderfulRectH,
         2.799999952316284f,
         3.299999952316284f,
         iconTop
      );
      if (drawSquares) {
      }

      int logoShadow = ObsidianComponent_olorUtil.applyAlpha(iconTop, 0.3199999928474426f);
      RenderUtil.drawShadow(
         matrices,
         iconX + 0.30000001192092896f,
         iconY - 1.25f,
         iconW - 1.0f,
         (float)(iconSize - 11),
         3.0f,
         5.0f,
         logoShadow
      );
      logoFont.drawGradientStringHorizontal(matrices, iconGlyph, iconX - 0.25f, iconY, iconTop, iconTop);
      suisse13.drawString(matrices, brandText, brandTextX, textY, whiteColor);
      float rect2X = x + wonderfulRectW + 2.5f;
      float rect2H = 15.850000381469727f;
      int icon2Size = 14;
      String iconGlyph2 = "e";
      float icon2Y = y + 7.449999809265137f;
      int icon3Size = 14;
      String fpsIconGlyph = "j";
      String pingIconGlyph = "f";
      float icon3Y = y + 7.25f;
      int fps = mc != null ? mc.getCurrentFps() : 0;
      String fpsValue = String.valueOf(fps);
      String fpsSuffix = "fps";
      String fpsText = fpsValue + fpsSuffix;
      int ping = 0;
      if (mc != null && mc.player != null && mc.getNetworkHandler() != null) {
         PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
         if (entry != null) {
            ping = entry.getLatency();
         }
      }

      String pingValue = String.valueOf(ping);
      String pingSuffix = "ms";
      String pingText = pingValue + pingSuffix;
      float contentW = rect2Pad + iconNew14.getStringWidth(iconGlyph2) + 1.0f;
      if (!username.isEmpty()) {
         contentW += suisse13.getStringWidth(username) + 2.0f;
      }

      if (this.showFps) {
         contentW += statsIconFont != null
            ? statsIconFont.getStringWidth(fpsIconGlyph) + 2.0f
            : 0.0f;
         contentW += suisse13.getStringWidth(fpsText) + 2.0F;
      }

      if (this.showMs) {
         contentW += statsIconFont != null
            ? statsIconFont.getStringWidth(pingIconGlyph) + 2.0f
            : 0.0f;
         contentW += suisse13.getStringWidth(pingText) + 2.0f;
      }

      contentW += rect2Pad;
      float rect2W = contentW - 1.0499999523162842f;
      RenderUtil.drawDefaultHudThemedPanel(
         matrices,
         rect2X,
         y,
         rect2W,
         rect2H,
         2.799999952316284f,
         3.299999952316284f,
         iconTop
      );
      if (drawSquares) {
         RenderUtil.drawHudSquarePattern(matrices, rect2X, y, rect2W, rect2H, iconTop);
      }

      float drawX = rect2X + rect2Pad + 1.5f;
      iconNew14.drawGradientStringHorizontal(matrices, iconGlyph2, drawX - 1.0f, icon2Y, iconTop, iconTop);
      drawX += iconNew14.getStringWidth(iconGlyph2) + 1.0f;
      if (!username.isEmpty()) {
         suisse13.drawString(matrices, username, drawX, textY, whiteColor);
         drawX += suisse13.getStringWidth(username) + 2.0f;
      }

      if (this.showFps) {
         if (statsIconFont != null) {
            statsIconFont.drawGradientStringHorizontal(matrices, fpsIconGlyph, drawX, icon3Y, iconTop, iconTop);
            drawX += statsIconFont.getStringWidth(fpsIconGlyph) + 2.0F;
         }

         suisse13.drawString(matrices, fpsValue, drawX, textY, whiteColor);
         suisse13.drawString(
            matrices, fpsSuffix, drawX + suisse13.getStringWidth(fpsValue) - 1.0f, textY, iconTop
         );
         drawX += suisse13.getStringWidth(fpsText) + 2.0f;
      }

      if (this.showMs) {
         if (statsIconFont != null) {
            statsIconFont.drawGradientStringHorizontal(matrices, pingIconGlyph, drawX, icon3Y, iconTop, iconTop);
            drawX += statsIconFont.getStringWidth(pingIconGlyph) + 2.0f;
         }

         suisse13.drawString(matrices, pingValue, drawX, textY, whiteColor);
         suisse13.drawString(
            matrices,
            pingSuffix,
            (double)(drawX + suisse13.getStringWidth(pingValue)) - 0.5,
            (double)textY,
            iconTop
         );
      }

      String serverName = "Singleplayer";
      if (mc != null) {
         ServerInfo info = mc.getCurrentServerEntry();
         if (info != null && info.address != null && !info.address.isEmpty()) {
            serverName = info.address;
         }
      }

      boolean showBottom = (!(!this.showServer && !this.showTps));
      float rectBtmY = y + wonderfulRectH + 2.0f;
      float rectBtmH = 15.850000381469727f;
      int iconSmallSize = 15;
      float iconSmallW = iconNew15.getStringWidth(iconGlyph);
      float iconSmallY = rectBtmY
         + (rectBtmH - (float)iconSmallSize) / 2.0f
         + 6.5f;
      float serverTextY = rectBtmY
         + (rectBtmH - 12.0f) / 2.0F
         + 4.800000190734863f;
      String serverDisplayName = this.formatServerNameForDisplay(serverName);
      float serverTextW = suisse13.getStringWidth(serverDisplayName);
      int extraIconSize = 15;
      String extraIconGlyph = "y";
      float extraIconW = iconNew15.getStringWidth(extraIconGlyph);
      float extraIconY = rectBtmY
         + (rectBtmH - (float)extraIconSize) / 2.0f
         + 6.400000095367432f;
      String tpsValue = this.formatOneDecimal(this.getServerTps());
      String tpsSuffix = "tps";
      String tpsText = tpsValue + tpsSuffix;
      float tpsTextW = suisse13.getStringWidth(tpsText);
      float rectBtmW = 0.0f;
      if (showBottom) {
         float bottomX = x + rect2Pad + 8.5f;
         if (this.showServer) {
            bottomX += iconSmallW + 3.0f + serverTextW;
         }

         if (this.showTps) {
            if (this.showServer) {
               bottomX += 3.0F;
            }

            bottomX += extraIconW + 3.0f + tpsTextW;
         }

         rectBtmW = Math.max(40.0f, bottomX + rect2Pad - x);
         RenderUtil.drawDefaultHudThemedPanel(
            matrices,
            x,
            rectBtmY,
            rectBtmW - 2.8499999046325684f,
            rectBtmH,
            2.799999952316284f,
            3.299999952316284f,
            iconTop
         );
         if (drawSquares) {
            RenderUtil.drawHudSquarePattern(matrices, x, rectBtmY, rectBtmW, rectBtmH, iconTop);
         }

         float drawBottomX = x + rect2Pad + 7.0f;
         if (this.showServer) {
            iconNew15.drawGradientStringHorizontal(
               matrices, "n", drawBottomX - 6.5f, iconSmallY, iconTop, iconTop
            );
            drawBottomX += iconSmallW + 3.0f;
            this.drawServerNameWithThemeParts(matrices, serverDisplayName, drawBottomX, serverTextY, iconTop, whiteColor);
            drawBottomX += serverTextW;
         }

         if (this.showTps) {
            if (this.showServer) {
               drawBottomX += 3.0f;
            }

            iconNew15.drawGradientStringHorizontal(
               matrices, extraIconGlyph, drawBottomX - 1.5f, extraIconY, iconTop, iconTop
            );
            drawBottomX += extraIconW + 3.0f;
            suisse13.drawString(matrices, tpsValue, drawBottomX - 1.75f, serverTextY, whiteColor);
            suisse13.drawString(
               matrices,
               tpsSuffix,
               drawBottomX + suisse13.getStringWidth(tpsValue) - 2.5f,
               serverTextY,
               iconTop
            );
         }
      }

      float totalW = Math.max(wonderfulRectW + 2.0f + rect2W, rectBtmW);
      this.draggable.setWidth(totalW);
      this.draggable.setHeight(showBottom ? wonderfulRectH + 1.0f + rectBtmH : wonderfulRectH);
   }

   public void WaveStyle(EventRender2D eventRender) {
      float x = this.draggable.getX();
      float y = this.draggable.getY();
      MatrixStack matrices = eventRender.getContext().getMatrices();
      FontRenderer waveFont = ObsidianComponent_201.getFont("wave", 30);
      String watermarkText = "Obsidian";
      int indexColor = ObsidianComponent_olorUtil.getThemeColor(90);
      int indexColor2 = ObsidianComponent_olorUtil.getThemeColor(180);
      int indexColor3 = ObsidianComponent_olorUtil.getThemeColor(270);
      int indexColor4 = ObsidianComponent_olorUtil.getColor(360);
      float glowWidth = 95.0f + waveFont.getStringWidth("ful");
      RenderUtil.drawShadow(
         matrices,
         x,
         y,
         glowWidth,
         12.0f,
         10.0f,
         15.0f,
         indexColor4,
         indexColor2,
         indexColor,
         indexColor3
      );
      waveFont.drawGradientStringHorizontal(matrices, watermarkText, x, y, indexColor, indexColor2);
      this.draggable.setWidth(Math.max(glowWidth, waveFont.getStringWidth(watermarkText)));
      this.draggable.setHeight(12.0f);
   }
   public void drawServerNameWithThemeParts(MatrixStack matrices, String serverName, float x, float y, int themeColor, int whiteColor) {
      FontRenderer font = ObsidianComponent_201.getFont("suisse", 13);
      String[] parts = serverName.split("\\.");
      if (parts.length < (2)) {
         font.drawString(matrices, serverName, x, y, whiteColor);
      } else {
         String mainPart = String.join(".", Arrays.copyOf(parts, parts.length - (1)));
         String suffixPart = "." + parts[parts.length - 1];
         font.drawString(matrices, mainPart, x, y, whiteColor);
         float suffixX = x + font.getStringWidth(mainPart) - 2.0f;
         font.drawString(matrices, suffixPart, suffixX, y, themeColor);
      }
   }
   public String formatServerNameForDisplay(String serverName) {
      if (serverName != null && !serverName.isEmpty()) {
         String host = serverName;
         int portIndex = serverName.indexOf(58);
         if (portIndex > 0) {
            host = serverName.substring(0, portIndex);
         }

         String[] parts = host.split("\\.");
         return parts.length >= 3
            ? String.join(".", Arrays.copyOfRange(parts, 1, parts.length))
            : host;
      } else {
         return "";
      }
   }
   public float getServerTps() {
      return Obsidian.INSTANCE != null && Obsidian.INSTANCE.tpsCalc != null
         ? Math.max(
            0.0f,
            Math.min(20.0f, Obsidian.INSTANCE.tpsCalc.getTPS())
         )
         : 20.0f;
   }
   public String formatOneDecimal(float value) {
      int scaled = Math.round(value * 10.0f);
      return scaled / (10) + "." + Math.abs(scaled % (10));
   }
}
