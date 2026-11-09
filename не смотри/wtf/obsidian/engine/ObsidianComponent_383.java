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
























import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.MathHelper;

public class ObsidianComponent_383 implements IInstance {
   public static final float WIDTH = 115.0F;
   public static final float HEIGHT = 280.0F;
   public static final float GAP = 14.0F;
   public static final float HEADER_H = 23.0F;
   public static final float ITEM_H = 20.0F;
   public static final float ITEM_GAP = 3.0F;
   public static final float ITEM_PADDING = 5.0F;
   public static final float CONTENT_PAD_T = 4.0F;
   public static final float CONTENT_PAD_B = 4.0F;
   public static final float BTN_H = 16.0F;
   public static final float BTN_GAP = 4.0F;
   public static final float BTN_PAD = 5.0F;
   public static final float FOOTER_H = 44.0F;
   public static final float DEL_BTN_W = 14.0F;
   public static final float DEL_BTN_H = 14.0F;
   public static final float INPUT_H = 16.0F;
   public float panelX;
   public float panelY;
   public float scrollTarget = 0.0f;
   public float scrollCurrent = 0.0f;
   public String hoveredConfig = null;
   public String inputMode = null;
   public String inputText = "";
   public String renameTarget = null;
   public String deleteConfirmTarget = null;

   public void updatePosition(Window window, int categoryCount) {
      float totalW = ObsidianComponent_200.getTotalCategoriesWidth(categoryCount);
      float guiLeft = (float)window.getScaledWidth() / 2.0f
         - totalW / 2.0f;
      this.panelX = guiLeft + totalW + 14.0f;
      this.panelY = (float)window.getScaledHeight() / 2.0f
         - 140.0f
         - 10.0f;
   }

   public float getPanelX() {
      return this.panelX;
   }

   public float getPanelY() {
      return this.panelY;
   }

   public void render(DrawContext context, int mouseX, int mouseY, float offsetY, float alphaMul, int colorTheme) {
      float py = this.panelY + offsetY;
      int shadeColor = this.getFadeShadeColor(alphaMul, 120);
      RenderUtil.drawRoundedRect(
         context.getMatrices(),
         this.panelX,
         py,
         115.0f,
         280.0f,
         0.0f,
         ObsidianComponent_olorUtil.rgba(25, 25, 25, 150)
      );
      int time = (int)(
         (float)(System.currentTimeMillis() % (2000L))
            / 2000.0f
            * 360.0f
      );
      int leftTop = ObsidianComponent_olorUtil.getThemeColor(time);
      int leftBottom = ObsidianComponent_olorUtil.getThemeColor(time + (30));
      int centerTop = ObsidianComponent_olorUtil.getThemeColor(time + (90));
      int centerBottom = ObsidianComponent_olorUtil.getThemeColor(time + (120));
      int rightTop = ObsidianComponent_olorUtil.getThemeColor(time + 180);
      int rightBottom = ObsidianComponent_olorUtil.getThemeColor(time + 210);
      RenderUtil.drawWaveHudHeader(
         context.getMatrices(),
         this.panelX,
         py,
         115.0f,
         20.0f,
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
      if ((shadeColor >> 24 & (255)) > 0) {
         RenderUtil.drawRoundedRect(
            context.getMatrices(),
            this.panelX,
            py,
            115.0f,
            280.0f,
            8.0f,
            shadeColor
         );
      }

      this.renderHeader(context, py, alphaMul, colorTheme, shadeColor);
      float contentY = py + 23.0f + 1.0f;
      float footerY = py + 280.0f - 44.0f;
      float contentH = footerY
         - contentY
         - 4.0f
         - 4.0f;
      this.renderConfigList(context, mouseX, mouseY, contentY, contentH, offsetY, alphaMul, colorTheme, shadeColor);
      this.renderFooter(context, mouseX, mouseY, footerY, offsetY, alphaMul, colorTheme, shadeColor);
      if (this.inputMode != null) {
         this.renderInputOverlay(context, mouseX, mouseY, py, alphaMul, colorTheme);
      }
   }
   public void renderHeader(DrawContext context, float py, float alphaMul, int colorTheme, int shadeColor) {
      FontRenderer title = this.issue(15);
      String titleText = "Manager";
      float titleW = title.getWidth(titleText);
      float titleX = this.panelX + (115.0f - titleW) / 2.0f;
      float titleY = py + 8.5f;
      title.draw(context.getMatrices(), titleText, titleX, titleY, this.alpha(-1, alphaMul));
   }
   public void renderConfigList(
      DrawContext context, int mouseX, int mouseY, float contentY, float contentH, float offsetY, float alphaMul, int colorTheme, int shadeColor
   ) {
      List<String> configs = this.getConfigs();
      String current = Obsidian.INSTANCE.configStorage.currentConfig;
      float totalItemsH = (float)configs.size() * 23.0f;
      float maxScroll = Math.min(
         0.0f,
         contentH
            - totalItemsH
            - 4.0f
            - 4.0f
      );
      this.scrollTarget = MathHelper.clamp(this.scrollTarget, maxScroll, 0.0f);
      this.scrollCurrent = this.scrollCurrent + (this.scrollTarget - this.scrollCurrent) * 0.18000000715255737f;
      ObsidianComponent_343.push();
      ObsidianComponent_343.setFromComponentCoordinates(
         (double)(this.panelX + 2.0f),
         (double)contentY,
         111.0,
         (double)contentH
      );
      float itemY = contentY + 4.0f + this.scrollCurrent;
      this.hoveredConfig = null;

      for (String config : configs) {
         boolean isCurrent = config.equals(current);
         boolean hovered = MathUtil.isHovered(
            (double)mouseX,
            (double)mouseY,
            (double)(this.panelX + 5.0f),
            (double)itemY,
            105.0,
            20.0
         );
         if (hovered) {
            this.hoveredConfig = config;
         }

         this.renderConfigItem(context, config, isCurrent, hovered, itemY, alphaMul, colorTheme, shadeColor);
         itemY += 23.0f;
      }

      ObsidianComponent_343.pop();
   }
   public void renderConfigItem(
      DrawContext context, String name, boolean isCurrent, boolean hovered, float itemY, float alphaMul, int colorTheme, int shadeColor
   ) {
      int bgColor;
      if (isCurrent) {
         bgColor = ObsidianComponent_olorUtil.darken(colorTheme, 0.15000000596046448f);
      } else if (hovered) {
         bgColor = ObsidianComponent_olorUtil.darken(colorTheme, 0.11999999731779099f);
      } else {
         bgColor = ObsidianComponent_olorUtil.darken(colorTheme, 0.09000000357627869f);
      }

      bgColor = ObsidianComponent_olorUtil.setAlphaColor(bgColor, (int)(255.0f * alphaMul));
      if ((shadeColor >> (24) & (255)) > 0) {
      }

      if (isCurrent) {
         float alpha = 0.25f;
         int alphaInt = (int)(alpha * 255.0f) << 24;
         int time = (int)(
            (float)(System.currentTimeMillis() % (2000L))
               / 2000.0f
               * 360.0f
         );
         int leftTop = ObsidianComponent_olorUtil.getThemeColor(time) & 16777215 | alphaInt;
         int leftBottom = ObsidianComponent_olorUtil.getThemeColor(time + (30)) & 16777215 | alphaInt;
         int centerTop = ObsidianComponent_olorUtil.getThemeColor(time + 90) & (16777215) | alphaInt;
         int centerBottom = ObsidianComponent_olorUtil.getThemeColor(time + (120)) & 16777215 | alphaInt;
         int rightTop = ObsidianComponent_olorUtil.getThemeColor(time + 180) & (16777215) | alphaInt;
         int rightBottom = ObsidianComponent_olorUtil.getThemeColor(time + 210) & 16777215 | alphaInt;
         RenderUtil.drawWaveHudHeader(
            context.getMatrices(),
            this.panelX,
            itemY - 5.0f,
            135.0f,
            20.0f,
            0.0f,
            5.0f,
            5.0f,
            leftTop,
            leftBottom,
            centerTop,
            centerBottom,
            rightTop,
            rightBottom
         );
      }

      FontRenderer font = this.issue(13);
      int nameColor = isCurrent
         ? this.alpha(-1, alphaMul)
         : this.alpha(
            ObsidianComponent_olorUtil.rgba(
               200,
               200,
               205,
               255
            ),
            alphaMul
         );
      ObsidianComponent_343.push();
      ObsidianComponent_343.setFromComponentCoordinates(
         (double)(this.panelX + 5.0f + 1.0f),
         (double)(itemY - 5.0f),
         83.0,
         20.0
      );
      font.draw(
         context.getMatrices(),
         name,
         this.panelX + 5.0f + 1.0f,
         itemY + 7.399999618530273f - 3.5f,
         nameColor
      );
      ObsidianComponent_343.pop();
      if (hovered && !isCurrent) {
         float delX = this.panelX
            + 115.0f
            - 5.0f
            - 14.0f
            + 3.0f;
         float delY = itemY + 3.0f - 4.0f;
         RenderUtil.drawRoundedRect(
            context.getMatrices(),
            delX,
            delY,
            12.0f,
            12.0f,
            1.0f,
            ObsidianComponent_olorUtil.setAlphaColor(
               ObsidianComponent_olorUtil.rgba(
                  180,
                  40,
                  40,
                  255
               ),
               (int)(180.0f * alphaMul)
            )
         );
         FontRenderer delFont = this.issue(12);
         String delChar = "x";
         float charW = delFont.getWidth(delChar);
         float charX = delX
            + (14.0f - charW) / 2.0f;
         float charY = delY + 4.800000190734863f;
         delFont.draw(
            context.getMatrices(),
            delChar,
            charX - 1.0f,
            charY - 0.30000001192092896f,
            this.alpha(
               ObsidianComponent_olorUtil.rgba(255, 180, 180, 255),
               alphaMul
            )
         );
      }
   }
   public void renderFooter(DrawContext context, int mouseX, int mouseY, float footerY, float offsetY, float alphaMul, int colorTheme, int shadeColor) {
      float alpha = 0.25f;
      int alphaInt = (int)(alpha * 255.0f) << 24;
      int time = (int)(
         (float)(System.currentTimeMillis() % (2000L))
            / 2000.0f
            * 360.0f
      );
      int leftTop = ObsidianComponent_olorUtil.getThemeColor(time) & 16777215 | alphaInt;
      int leftBottom = ObsidianComponent_olorUtil.getThemeColor(time + 30) & 16777215 | alphaInt;
      int centerTop = ObsidianComponent_olorUtil.getThemeColor(time + (90)) & 16777215 | alphaInt;
      int centerBottom = ObsidianComponent_olorUtil.getThemeColor(time + (120)) & 16777215 | alphaInt;
      int rightTop = ObsidianComponent_olorUtil.getThemeColor(time + (180)) & (16777215) | alphaInt;
      int rightBottom = ObsidianComponent_olorUtil.getThemeColor(time + (210)) & (16777215) | alphaInt;
      float row1Y = footerY + 4.0f;
      float halfW = 50.5f;
      RenderUtil.drawWaveHudHeader(
         context.getMatrices(),
         this.panelX + 5.0f,
         row1Y,
         halfW,
         16.0f,
         0.0f,
         5.0f,
         5.0f,
         leftTop,
         leftBottom,
         centerTop,
         centerBottom,
         rightTop,
         rightBottom
      );
      RenderUtil.drawWaveHudHeader(
         context.getMatrices(),
         this.panelX + 5.0f + halfW + 4.0f,
         row1Y,
         halfW,
         16.0f,
         0.0f,
         5.0f,
         5.0f,
         leftTop,
         leftBottom,
         centerTop,
         centerBottom,
         rightTop,
         rightBottom
      );
      float row2Y = row1Y + 16.0f + 4.0f;
      RenderUtil.drawWaveHudHeader(
         context.getMatrices(),
         this.panelX + 5.0f,
         row2Y,
         105.0f,
         16.0F,
         0.0f,
         5.0f,
         5.0f,
         leftTop,
         leftBottom,
         centerTop,
         centerBottom,
         rightTop,
         rightBottom
      );
      FontRenderer delFont = this.issue(13);
      delFont.draw(
         context.getMatrices(),
         "Новый",
         this.panelX + 19.0f,
         row1Y + 6.0f,
         this.alpha(
            ObsidianComponent_olorUtil.rgba(
               255, 255, 255, 200
            ),
            alphaMul
         )
      );
      delFont.draw(
         context.getMatrices(),
         "Сохранить",
         this.panelX + 67.0F,
         row1Y + 6.0f,
         this.alpha(
            ObsidianComponent_olorUtil.rgba(
               255,
               255,
               255,
               200
            ),
            alphaMul
         )
      );
      delFont.draw(
         context.getMatrices(),
         "Открыть папку",
         this.panelX + 34.0f,
         row1Y + 26.5f,
         this.alpha(
            ObsidianComponent_olorUtil.rgba(
               255,
               255,
               255,
               200
            ),
            alphaMul
         )
      );
   }
   public void renderButton(
      DrawContext context,
      int mouseX,
      int mouseY,
      float bx,
      float by,
      float bw,
      float bh,
      String label,
      float alphaMul,
      int colorTheme,
      int shadeColor,
      boolean active
   ) {
      boolean hovered = MathUtil.isHovered((double)mouseX, (double)mouseY, (double)bx, (double)by, (double)bw, (double)bh);
      int bgColor = hovered
         ? ObsidianComponent_olorUtil.setAlphaColor(
            ObsidianComponent_olorUtil.darken(colorTheme, 0.11999999731779099f),
            (int)(255.0f * alphaMul)
         )
         : ObsidianComponent_olorUtil.setAlphaColor(
            ObsidianComponent_olorUtil.darken(colorTheme, 0.09000000357627869f),
            (int)(255.0f * alphaMul)
         );
      RenderUtil.drawRoundedRect(context.getMatrices(), bx, by, bw, bh, 4.0f, bgColor);
      if ((shadeColor >> 24 & (255)) > 0) {
         RenderUtil.drawRoundedRect(context.getMatrices(), bx, by, bw, bh, 4.0f, shadeColor);
      }

      FontRenderer font = this.issue(12);
      float charW = font.getWidth(label);
      float textX = bx + (bw - charW) / 2.0f;
      float textY = by + (bh - 4.800000190734863f) / 2.0f;
      font.draw(
         context.getMatrices(),
         label,
         textX,
         textY,
         this.alpha(
            hovered
               ? -1
               : ObsidianComponent_olorUtil.rgba(
                  200,
                  200,
                  205,
                  255
               ),
            alphaMul
         )
      );
   }
   public void renderInputOverlay(DrawContext context, int mouseX, int mouseY, float py, float alphaMul, int colorTheme) {
      RenderUtil.drawRoundedRect(
         context.getMatrices(),
         this.panelX,
         py,
         115.0f,
         280.0f,
         8.0f,
         ObsidianComponent_olorUtil.rgba(
            0,
            0,
            0,
            (int)(160.0f * alphaMul)
         )
      );
      float boxW = 95.0f;
      float boxH = 60.0f;
      float boxX = this.panelX + 10.0f;
      float boxY = py + 140.0f - boxH / 2.0f;
      RenderUtil.drawRoundedRect(
         context.getMatrices(),
         boxX,
         boxY,
         boxW,
         boxH,
         6.0f,
         ObsidianComponent_olorUtil.setAlphaColor(
            ObsidianComponent_olorUtil.darken(colorTheme, 0.05000000074505806f),
            (int)(245.0f * alphaMul)
         )
      );
      String title = this.inputMode.equals("create") ? "Новый конфиг" : "Переименовать";
      FontRenderer titleFont = this.issue(13);
      float titleW = titleFont.getWidth(title);
      titleFont.draw(
         context.getMatrices(),
         title,
         boxX + (boxW - titleW) / 2.0f,
         boxY + 7.0f,
         this.alpha(-1, alphaMul)
      );
      float fieldX = boxX + 6.0f;
      float fieldY = boxY + 20.0f;
      float fieldW = boxW - 12.0f;
      RenderUtil.drawRoundedRect(
         context.getMatrices(),
         fieldX,
         fieldY,
         fieldW,
         16.0f,
         3.0f,
         ObsidianComponent_olorUtil.setAlphaColor(
            ObsidianComponent_olorUtil.darken(colorTheme, 0.18000000715255737f),
            (int)(255.0f * alphaMul)
         )
      );
      String display = this.inputText
         + (
            System.currentTimeMillis()
                     / (500L)
                     % (2L)
                  == (0L)
               ? "_"
               : ""
         );
      ObsidianComponent_343.push();
      ObsidianComponent_343.setFromComponentCoordinates(
         (double)(fieldX + 3.0f),
         (double)fieldY,
         (double)(fieldW - 6.0f),
         16.0
      );
      this.issue(12)
         .draw(
            context.getMatrices(),
            display,
            fieldX + 4.0F,
            fieldY + 4.0f,
            this.alpha(-1, alphaMul)
         );
      ObsidianComponent_343.pop();
      float btnY = boxY + boxH - 16.0f - 6.0f;
      float btnW = (boxW - 12.0f) / 2.0f;
      this.renderButton(
         context,
         mouseX,
         mouseY,
         fieldX,
         btnY,
         btnW,
         16.0f,
         "OK",
         alphaMul,
         colorTheme,
         0,
         false
      );
      this.renderButton(
         context,
         mouseX,
         mouseY,
         fieldX + btnW + 6.0f,
         btnY,
         btnW,
         16.0f,
         "Отмена",
         alphaMul,
         colorTheme,
         0,
         false
      );
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button, float offsetY) {
      float py = this.panelY + offsetY;
      if (this.inputMode != null) {
         return this.handleInputClick(mouseX, mouseY, button, py);
      } else {
         List<String> configs = this.getConfigs();
         String current = Obsidian.INSTANCE.configStorage.currentConfig;
         float contentY = py + 23.0f + 1.0f;
         float footerY = py + 280.0f - 44.0f;
         float contentH = footerY - contentY - 4.0f - 4.0f;
         float itemY = contentY + 4.0f + this.scrollCurrent;

         for (String config : configs) {
            if (MathUtil.isHovered(
               mouseX,
               mouseY,
               (double)(this.panelX + 5.0f),
               (double)itemY,
               105.0,
               20.0
            )) {
               if (button == 0) {
                  float delX = this.panelX
                     + 115.0f
                     - 5.0f
                     - 14.0f
                     - 1.0f;
                  float delY = itemY + 3.0f;
                  if (!config.equals(current)
                     && MathUtil.isHovered(
                        mouseX,
                        mouseY,
                        (double)delX,
                        (double)delY,
                        14.0,
                        14.0
                     )) {
                     this.deleteConfig(config);
                     return true;
                  }

                  this.loadConfig(config);
                  return true;
               }

               if (button == (1)) {
                  this.openRename(config);
                  return true;
               }
            }

            itemY += 23.0f;
         }

         float btnY = py
            + 280.0f
            - 44.0f
            + 4.0f;
         float halfW = 50.5f;
         float newBtnX = this.panelX + 5.0f;
         float saveBtnX = this.panelX + 5.0f + halfW + 4.0f;
         float row2Y = btnY + 16.0f + 4.0f;
         if (button == 0) {
            if (MathUtil.isHovered(
               mouseX,
               mouseY,
               (double)newBtnX,
               (double)btnY,
               (double)halfW,
               16.0
            )) {
               this.openCreate();
               return true;
            }

            if (MathUtil.isHovered(
               mouseX,
               mouseY,
               (double)saveBtnX,
               (double)btnY,
               (double)halfW,
               16.0
            )) {
               this.saveCurrentConfig();
               return true;
            }

            if (MathUtil.isHovered(
               mouseX,
               mouseY,
               (double)(this.panelX + 5.0f),
               (double)row2Y,
               105.0,
               16.0
            )) {
               this.openFolder();
               return true;
            }
         }

         return this.isHovered(mouseX, mouseY, offsetY);
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount, float offsetY) {
      float py = this.panelY + offsetY;
      float contentY = py + 23.0f + 1.0f;
      float footerY = py + 280.0f - 44.0f;
      float contentH = footerY - contentY;
      if (!MathUtil.isHovered(
         mouseX,
         mouseY,
         (double)this.panelX,
         (double)contentY,
         115.0,
         (double)contentH
      )) {
         return false;
      } else {
         List<String> configs = this.getConfigs();
         float totalItemsH = (float)configs.size() * 23.0f;
         float maxScroll = Math.min(
            0.0f,
            contentH - totalItemsH - 4.0f - 4.0f
         );
         this.scrollTarget = MathHelper.clamp(
            this.scrollTarget + (float)(amount * 16.0),
            maxScroll,
            0.0f
         );
         return true;
      }
   }

   public boolean keyPressed(int keyCode, int modifiers) {
      if (this.inputMode == null) {
         return false;
      } else if (keyCode == 256) {
         this.closeInput();
         return true;
      } else if (keyCode == 257 || keyCode == 335) {
         this.confirmInput();
         return true;
      } else if (keyCode == 259) {
         if (!this.inputText.isEmpty()) {
            this.inputText = this.inputText.substring(0, this.inputText.length() - (1));
         }

         return true;
      } else {
         return true;
      }
   }

   public boolean charTyped(char chr) {
      if (this.inputMode == null) {
         return false;
      } else {
         if (!Character.isISOControl(chr)
            && this.inputText.length() < 32
            && chr != 47
            && chr != 92
            && chr != 58
            && chr != 42
            && chr != 63
            && chr != (34)
            && chr != 60
            && chr != 62
            && chr != 124) {
            this.inputText = this.inputText + chr;
         }

         return true;
      }
   }

   public boolean isHovered(double mouseX, double mouseY, float offsetY) {
      return MathUtil.isHovered(
         mouseX,
         mouseY,
         (double)this.panelX,
         (double)(this.panelY + offsetY),
         115.0,
         280.0
      );
   }
   public boolean handleInputClick(double mouseX, double mouseY, int button, float py) {
      if (button != 0) {
         return true;
      } else {
         float boxW = 95.0f;
         float boxH = 60.0f;
         float boxX = this.panelX + 10.0f;
         float boxY = py + 140.0f - boxH / 2.0f;
         float fieldX = boxX + 6.0f;
         float btnY = boxY + boxH - 16.0f - 6.0f;
         float btnW = (boxW - 12.0f) / 2.0f;
         if (MathUtil.isHovered(
            mouseX,
            mouseY,
            (double)fieldX,
            (double)btnY,
            (double)btnW,
            16.0
         )) {
            this.confirmInput();
            return true;
         } else if (MathUtil.isHovered(
            mouseX,
            mouseY,
            (double)(fieldX + btnW + 6.0f),
            (double)btnY,
            (double)btnW,
            16.0
         )) {
            this.closeInput();
            return true;
         } else {
            return true;
         }
      }
   }
   public void openCreate() {
      this.inputMode = "create";
      this.inputText = "";
      this.renameTarget = null;
   }
   public void openRename(String config) {
      this.inputMode = "rename";
      this.inputText = config;
      this.renameTarget = config;
   }
   public void closeInput() {
      this.inputMode = null;
      this.inputText = "";
      this.renameTarget = null;
   }
   public void confirmInput() {
      String name = this.inputText.trim();
      if (name.isEmpty()) {
         this.closeInput();
      } else {
         if ("create".equals(this.inputMode)) {
            this.createConfig(name);
         } else if ("rename".equals(this.inputMode) && this.renameTarget != null) {
            this.renameConfig(this.renameTarget, name);
         }

         this.closeInput();
      }
   }
   public void loadConfig(String name) {
      try {
         Obsidian.INSTANCE.configStorage.saveConfig(Obsidian.INSTANCE.configStorage.currentConfig);
         Obsidian.INSTANCE.configStorage.loadConfig(name);
      } catch (Exception var3) {
         var3.printStackTrace();
      }
   }
   public void saveCurrentConfig() {
      try {
         Obsidian.INSTANCE.configStorage.saveConfig(Obsidian.INSTANCE.configStorage.currentConfig);
      } catch (Exception var2) {
         var2.printStackTrace();
      }
   }
   public void createConfig(String name) {
      try {
         Obsidian.INSTANCE.configStorage.saveConfig(name);
      } catch (Exception var3) {
         var3.printStackTrace();
      }
   }
   public void renameConfig(String oldName, String newName) {
      try {
         File configsDir = Obsidian.INSTANCE.configsDir;
         File oldFile = new File(configsDir, oldName + ".wonder");
         File newFile = new File(configsDir, newName + ".wonder");
         if (oldFile.exists() && !newFile.exists()) {
            oldFile.renameTo(newFile);
            if (Obsidian.INSTANCE.configStorage.currentConfig.equals(oldName)) {
               Obsidian.INSTANCE.configStorage.currentConfig = newName;
            }
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }
   }
   public void deleteConfig(String name) {
      try {
         File file = new File(Obsidian.INSTANCE.configsDir, name + ".wonder");
         if (file.exists()) {
            file.delete();
         }
      } catch (Exception var3) {
         var3.printStackTrace();
      }
   }
   public void openFolder() {
      try {
         File dir = Obsidian.INSTANCE.configsDir;
         if (dir.exists()) {
            Desktop.getDesktop().open(dir);
         }
      } catch (Exception var2) {
         var2.printStackTrace();
      }
   }
   public List<String> getConfigs() {
      List<String> list = new ArrayList<>();
      File dir = Obsidian.INSTANCE.configsDir;
      if (dir != null && dir.exists()) {
         File[] files = dir.listFiles((d, n) -> n.endsWith(".wonder"));
         if (files == null) {
            return list;
         } else {
            File[] var4 = files;
            int var5 = files.length;

            for (int var6 = 0; var6 < var5; var6++) {
               File f = var4[var6];
               list.add(f.getName().replace(".wonder", ""));
            }

            list.sort(String::compareToIgnoreCase);
            return list;
         }
      } else {
         return list;
      }
   }
   public FontRenderer issue(int size) {
      return ObsidianComponent_201.getFont("suisse", size);
   }
   public FontRenderer iconsNew(int size) {
      return ObsidianComponent_201.getFont("icon1", size);
   }
   public int alpha(int color, float alphaMul) {
      return ObsidianComponent_olorUtil.applyAlpha(color, alphaMul);
   }
   public int getFadeShadeColor(float alphaMul, int maxAlpha) {
      int a = MathHelper.clamp(
         (int)((1.0f - alphaMul) * (float)maxAlpha),
         0,
         255
      );
      return ObsidianComponent_olorUtil.rgba(0, 0, 0, a);
   }
}
