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
























import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.MathHelper;

public class ObsidianComponent_325 implements IInstance {
   public static final float WIDTH = 115.0F;
   public static final float HEIGHT = 280.0F;
   public static final float GAP = 10.0F;
   public static final float HEADER_H = 23.0F;
   public static final float ITEM_H = 20.0F;
   public static final float ITEM_GAP = 3.0F;
   public static final float ITEM_PADDING = 5.0F;
   public static final float CONTENT_PAD_T = 4.0F;
   public static final float CONTENT_PAD_B = 4.0F;
   public static final float COLOR_BOX_SIZE = 10.0F;
   public static final float BTN_H = 16.0F;
   public static final float BTN_GAP = 4.0F;
   public static final float BTN_PAD = 5.0F;
   public static final float FOOTER_H = 44.0F;
   public float panelX;
   public float panelY;
   public float scrollTarget = 0.0f;
   public float scrollCurrent = 0.0f;
   public Theme hoveredTheme = null;

   public void updatePosition(Window window, int categoryCount) {
      float totalW = ObsidianComponent_200.getTotalCategoriesWidth(categoryCount);
      float guiLeft = (float)window.getScaledWidth() / 2.0f
         - totalW / 2.0f;
      this.panelX = guiLeft - 115.0f - 10.0f;
      this.panelY = ObsidianComponent_200.getCenteredY((float)window.getScaledHeight()) - 7.0f;
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
         ObsidianComponent_olorUtil.rgba(
            25, 25, 25, 150
         )
      );
      int time = (int)(
         (float)(System.currentTimeMillis() % (2000L))
            / 2000.0f
            * 360.0f
      );
      int leftTop = ObsidianComponent_olorUtil.getThemeColor(time);
      int leftBottom = ObsidianComponent_olorUtil.getThemeColor(time + 30);
      int centerTop = ObsidianComponent_olorUtil.getThemeColor(time + (90));
      int centerBottom = ObsidianComponent_olorUtil.getThemeColor(time + (120));
      int rightTop = ObsidianComponent_olorUtil.getThemeColor(time + 180);
      int rightBottom = ObsidianComponent_olorUtil.getThemeColor(time + (210));
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
      if ((shadeColor >> 24 & 255) > 0) {
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

      this.renderHeader(context, py, alphaMul);
      float contentY = py + 23.0f + 1.0f;
      float footerY = py + 280.0f - 44.0f;
      float contentH = footerY
         - contentY
         - 4.0f
         - 4.0f;
      this.renderThemeList(context, mouseX, mouseY, contentY, contentH, offsetY, alphaMul, colorTheme, shadeColor);
      this.renderFooter(context, mouseX, mouseY, footerY, offsetY, alphaMul, colorTheme, shadeColor);
   }
   public void renderHeader(DrawContext context, float py, float alphaMul) {
      FontRenderer title = this.issue(15);
      String titleText = "Themes";
      float titleW = title.getWidth(titleText);
      float titleX = this.panelX
         + (115.0f - titleW) / 2.0f;
      float titleY = py + 8.5f;
      title.draw(context.getMatrices(), titleText, titleX, titleY, this.alpha(-1, alphaMul));
   }
   public void renderThemeList(
      DrawContext context, int mouseX, int mouseY, float contentY, float contentH, float offsetY, float alphaMul, int colorTheme, int shadeColor
   ) {
      List<Theme> themes = Obsidian.INSTANCE.themeStorage.getThemeList();
      if (themes != null && !themes.isEmpty()) {
         Theme current = Obsidian.INSTANCE.themeStorage.getThemes();
         float totalItemsH = (float)themes.size() * 23.0f;
         float maxScroll = Math.min(
            0.0f,
            contentH - totalItemsH - 4.0f - 4.0f
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
         this.hoveredTheme = null;

         for (Theme theme : themes) {
            boolean isCurrent = (theme == current);
            boolean hovered = MathUtil.isHovered(
               (double)mouseX,
               (double)mouseY,
               (double)(this.panelX + 5.0f),
               (double)itemY,
               105.0,
               20.0
            );
            if (hovered) {
               this.hoveredTheme = theme;
            }

            this.renderThemeItem(context, theme, isCurrent, hovered, itemY, alphaMul, colorTheme, shadeColor);
            itemY += 23.0f;
         }

         ObsidianComponent_343.pop();
      }
   }
   public void renderThemeItem(
      DrawContext context, Theme theme, boolean isCurrent, boolean hovered, float itemY, float alphaMul, int colorTheme, int shadeColor
   ) {
      if (isCurrent) {
         float alpha = 0.25f;
         int alphaInt = (int)(alpha * 255.0f) << (24);
         int time = (int)(
            (float)(System.currentTimeMillis() % (2000L))
               / 2000.0f
               * 360.0f
         );
         int leftTop = ObsidianComponent_olorUtil.getThemeColor(time) & (16777215) | alphaInt;
         int leftBottom = ObsidianComponent_olorUtil.getThemeColor(time + (30)) & 16777215 | alphaInt;
         int centerTop = ObsidianComponent_olorUtil.getThemeColor(time + (90)) & 16777215 | alphaInt;
         int centerBottom = ObsidianComponent_olorUtil.getThemeColor(time + (120)) & (16777215) | alphaInt;
         int rightTop = ObsidianComponent_olorUtil.getThemeColor(time + (180)) & 16777215 | alphaInt;
         int rightBottom = ObsidianComponent_olorUtil.getThemeColor(time + (210)) & 16777215 | alphaInt;
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

      float boxX = this.panelX + 5.0f + 1.5f;
      float boxY = itemY + 5.0f - 4.5f;
      int themeColor = this.getThemeDisplayColor(theme);
      RenderUtil.drawRoundedRect(
         context.getMatrices(),
         boxX,
         boxY,
         10.0f,
         10.0f,
         2.0f,
         ObsidianComponent_olorUtil.applyAlpha(themeColor, Math.max(0.550000011920929f, alphaMul))
      );
      if (isCurrent) {
         RenderUtil.drawRoundedRect(
            context.getMatrices(),
            boxX,
            boxY,
            10.0f,
            10.0f,
            2.0f,
            ObsidianComponent_olorUtil.applyAlpha(themeColor, Math.max(0.550000011920929f, alphaMul))
         );
      }

      FontRenderer font = this.issue(13);
      String themeName = theme.name().substring(0, 1).toUpperCase()
         + theme.name().substring(1).toLowerCase();
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
         (double)(boxX + 10.0f + 3.0f),
         (double)(itemY - 5.0f),
         85.0,
         20.0
      );
      font.draw(
         context.getMatrices(),
         themeName,
         boxX + 10.0f + 3.0f,
         itemY + 7.399999618530273f - 3.5f,
         nameColor
      );
      ObsidianComponent_343.pop();
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
      int leftBottom = ObsidianComponent_olorUtil.getThemeColor(time + (30)) & 16777215 | alphaInt;
      int centerTop = ObsidianComponent_olorUtil.getThemeColor(time + 90) & (16777215) | alphaInt;
      int centerBottom = ObsidianComponent_olorUtil.getThemeColor(time + (120)) & 16777215 | alphaInt;
      int rightTop = ObsidianComponent_olorUtil.getThemeColor(time + 180) & 16777215 | alphaInt;
      int rightBottom = ObsidianComponent_olorUtil.getThemeColor(time + 210) & 16777215 | alphaInt;
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
      FontRenderer btnFont = this.issue(12);
      btnFont.draw(
         context.getMatrices(),
         "По умолч.",
         this.panelX + 13.0f,
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
      btnFont.draw(
         context.getMatrices(),
         "Случайная",
         this.panelX + 66.0f,
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
      btnFont.draw(
         context.getMatrices(),
         "Сбросить тему",
         this.panelX + 34.0f,
         row2Y + 6.0f,
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

   public boolean mouseClicked(double mouseX, double mouseY, int button, float offsetY) {
      float py = this.panelY + offsetY;
      List<Theme> themes = Obsidian.INSTANCE.themeStorage.getThemeList();
      if (themes != null && !themes.isEmpty()) {
         float contentY = py + 23.0f + 1.0f;
         float footerY = py + 280.0f - 44.0f;
         float contentH = footerY
            - contentY
            - 4.0f
            - 4.0f;
         float itemY = contentY + 4.0f + this.scrollCurrent;

         for (Theme theme : themes) {
            if (MathUtil.isHovered(
                  mouseX,
                  mouseY,
                  (double)(this.panelX + 5.0f),
                  (double)itemY,
                  105.0,
                  20.0
               )
               && button == 0) {
               Obsidian.INSTANCE.themeStorage.setThemes(theme);
               return true;
            }

            itemY += 23.0f;
         }

         float btnY = py
            + 280.0f
            - 44.0f
            + 4.0f;
         float halfW = 50.5f;
         float leftBtnX = this.panelX + 5.0f;
         float rightBtnX = this.panelX
            + 5.0f
            + halfW
            + 4.0f;
         float row2Y = btnY + 16.0f + 4.0f;
         if (button == 0) {
            if (MathUtil.isHovered(
               mouseX,
               mouseY,
               (double)leftBtnX,
               (double)btnY,
               (double)halfW,
               16.0
            )) {
               if (!themes.isEmpty()) {
                  Obsidian.INSTANCE.themeStorage.setThemes(themes.get(0));
               }

               return true;
            }

            if (MathUtil.isHovered(
               mouseX,
               mouseY,
               (double)rightBtnX,
               (double)btnY,
               (double)halfW,
               16.0
            )) {
               if (!themes.isEmpty()) {
                  int randomIndex = (int)(Math.random() * (double)themes.size());
                  Obsidian.INSTANCE.themeStorage.setThemes(themes.get(randomIndex));
               }

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
               if (!themes.isEmpty()) {
                  Obsidian.INSTANCE.themeStorage.setThemes(themes.get(0));
               }

               return true;
            }
         }

         return this.isHovered(mouseX, mouseY, offsetY);
      } else {
         return false;
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
         List<Theme> themes = Obsidian.INSTANCE.themeStorage.getThemeList();
         if (themes == null) {
            return false;
         } else {
            float totalItemsH = (float)themes.size() * 23.0f;
            float maxScroll = Math.min(
               0.0f,
               contentH
                  - totalItemsH
                  - 4.0f
                  - 4.0f
            );
            this.scrollTarget = MathHelper.clamp(
               this.scrollTarget + (float)(amount * 16.0),
               maxScroll,
               0.0f
            );
            return true;
         }
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
   public int getThemeDisplayColor(Theme theme) {
      int color = theme.getTheme().getColor(0);
      return ObsidianComponent_olorUtil.alpha(color) == 0
         ? ObsidianComponent_olorUtil.rgba(
            220,
            220,
            220,
            180
         )
         : color;
   }
   public FontRenderer issue(int size) {
      return ObsidianComponent_201.getFont("suisse", size);
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
