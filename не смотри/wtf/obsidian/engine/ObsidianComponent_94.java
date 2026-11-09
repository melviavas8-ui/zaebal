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
























import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;

public class ObsidianComponent_94 extends DraggableHudElement {
   public static final float DEFAULT_PAD_X = 7.0F;
   public static final float DEFAULT_ICON_TEXT_GAP = 1.0F;
   public static final float PREVIEW_ICON_TEXT_GAP = 2.0F;
   private final Map<FontTextureAtlas, AnimationUtil> appearAnimations = new HashMap<>();
   private final Map<FontTextureAtlas, Float> currentYPositions = new HashMap<>();
   private final Set<FontTextureAtlas> activeEntriesScratch = new HashSet<>();
   private long lastRenderTime = System.currentTimeMillis();
   public float previewAlpha = 0.0f;

   public ObsidianComponent_94(ObsidianComponent_280 draggable) {
      super(draggable);
   }
   public FontRenderer issue(int size) {
      return ObsidianComponent_201.getFont("suisse", size);
   }
   public FontRenderer icons(int size) {
      return ObsidianComponent_201.getFont("icon", size);
   }
   public FontRenderer iconNew(int size) {
      return ObsidianComponent_201.getFont("icon", size);
   }
   public String getEntryText(FontTextureAtlas entry) {
      if (entry.isCustom()) {
         return entry.customText;
      } else {
         String state = entry.enabled ? "Включен!" : "Выключен!";
         return entry.moduleName + " " + state;
      }
   }
   public String getWaveBodyText(FontTextureAtlas entry) {
      return entry.isCustom() ? entry.customText : "Module '" + entry.moduleName + "' is " + (entry.enabled ? "enabled." : "disabled.");
   }
   public float getDefaultEntryWidth(FontTextureAtlas entry, float padX) {
      String text = this.getEntryText(entry);
      String iconGlyph = entry.categoryIcon != null && !entry.categoryIcon.isEmpty() ? entry.categoryIcon : "?";
      return this.issue(13).getWidth(text)
         + this.icons(14).getWidth(iconGlyph)
         + padX * 2.0f
         + 1.0f;
   }
   public float getPreviewWidth(String previewText, String previewIconGlyph, float padX) {
      return this.issue(13).getWidth(previewText)
         + this.icons(16).getWidth(previewIconGlyph)
         + padX * 2.0f
         + 2.0f;
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
      if (mc != null) {
         long currentTime = System.currentTimeMillis();
         float deltaTime = (float)(currentTime - this.lastRenderTime) / 1000.0f;
         this.lastRenderTime = currentTime;
         List<FontTextureAtlas> entries = ObsidianComponent_283.getActive();
         boolean isChatOpen = mc.currentScreen instanceof ChatScreen;
         boolean shouldRender = (!(entries.isEmpty() && !isChatOpen));
         float targetPreviewAlpha = isChatOpen ? 0.7F : 0.0f;
         float alphaSpeed = 8.0f;
         this.previewAlpha = this.previewAlpha
            + (targetPreviewAlpha - this.previewAlpha) * Math.min(1.0f, alphaSpeed * deltaTime);
         if (!shouldRender && this.previewAlpha < 0.009999999776482582f) {
            this.appearAnimations.clear();
            this.currentYPositions.clear();
            this.previewAlpha = 0.0F;
         } else {
            float baseX = this.draggable.getX();
            float baseY = this.draggable.getY();
            int colorTheme;
            if (!Obsidian.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")) {
               colorTheme = Obsidian.INSTANCE.themeStorage.getThemes().getTheme().color[0];
            } else {
               colorTheme = ObsidianComponent_olorUtil.getThemeColor();
            }

            boolean drawSquares = this.isUnusualRectType();
            long now = System.currentTimeMillis();
            float height = 16.0f;
            float spacing = 3.0f;
            float lerpSpeed = 12.0f;
            float padX = 7.0f;
            String previewText = "Кликни на меня для открытия настроек!";
            String previewIconGlyph = "A";
            float previewIconW = this.icons(16).getWidth(previewIconGlyph);
            float previewWidth = this.getPreviewWidth(previewText, previewIconGlyph, padX);
            float maxWidth = previewWidth;

            for (FontTextureAtlas entry : entries) {
               float width = this.getDefaultEntryWidth(entry, padX);
               if (width > maxWidth) {
                  maxWidth = width;
               }
            }

            float targetY = baseY;
            if (this.previewAlpha > 0.009999999776482582f) {
               float x = baseX + (maxWidth - previewWidth) * 0.5f;
               float alpha = this.previewAlpha;
               float scale = 0.8600000143051147f + 0.14000000059604645f * alpha;
               int base = ObsidianComponent_olorUtil.setAlphaColor(
                  ObsidianComponent_olorUtil.rgba(50, 50, 50, 255),
                  (int)(255.0f * alpha)
               );
               int top = ObsidianComponent_olorUtil.setAlphaColor(
                  ObsidianComponent_olorUtil.darken(colorTheme, 0.15000000596046448f),
                  (int)(255.0f * alpha)
               );
               int bottom = ObsidianComponent_olorUtil.setAlphaColor(
                  ObsidianComponent_olorUtil.darken(colorTheme, 0.05000000074505806f),
                  (int)(255.0f * alpha)
               );
               float cx = x + previewWidth * 0.5f;
               float cy = baseY + height * 0.5f;
               MatrixStack ms = eventRender.getContext().getMatrices();
               ms.push();
               ms.translate(cx, cy, 0.0f);
               ms.scale(scale, scale, 1.0f);
               ms.translate(-cx, -cy, 0.0F);
               RenderUtil.drawDefaultHudPanel(
                  ms,
                  x,
                  baseY,
                  previewWidth,
                  height,
                  3.0f,
                  3.5f,
                  base,
                  top,
                  bottom
               );
               float squareAlpha = alpha
                  * alpha
                  * (
                     3.0f
                        - 2.0f * alpha
                  );
               if (drawSquares && squareAlpha > 0.07999999821186066f) {
                  RenderUtil.drawHudSquarePattern(
                     ms,
                     x,
                     baseY,
                     previewWidth,
                     height,
                     ObsidianComponent_olorUtil.setAlphaColor(colorTheme, (int)(255.0f * squareAlpha))
                  );
               }

               int textColor = ObsidianComponent_olorUtil.setAlphaColor(
                  -1, (int)(255.0f * alpha)
               );
               int iconColor = ObsidianComponent_olorUtil.setAlphaColor(colorTheme, (int)(255.0f * alpha));
               this.icons(16)
                  .draw(
                     ms,
                     previewIconGlyph,
                     x + padX - 3.5f,
                     baseY + 6.599999904632568f,
                     iconColor
                  );
               this.issue(13)
                  .draw(
                     ms,
                     previewText,
                     x + padX + previewIconW + 5.5f,
                     baseY + 6.599999904632568f,
                     textColor
                  );
               ms.pop();
               targetY = baseY + height + spacing;
            }

            for (FontTextureAtlas entryx : entries) {
               AnimationUtil anim = this.appearAnimations
                  .computeIfAbsent(
                     entryx,
                     e -> new AnimationUtil(
                           0.0f,
                           12.0f,
                           ObsidianComponent_57.QUAD_OUT
                        )
                  );
               long age = now - entryx.startTime;
               anim.update(1.0f);
               float appear = anim.getValue();
               float alpha = appear;
               if (age > (2300L)) {
                  alpha = (
                        1.0f
                           - (float)(age - (2300L))
                              / 200.0f
                     )
                     * appear;
               }

               if (alpha <= 0.0f) {
                  targetY += height + spacing;
               } else {
                  Float currentY = this.currentYPositions.get(entryx);
                  if (currentY == null) {
                     currentY = targetY;
                  }

                  float diff = targetY - currentY;
                  if (Math.abs(diff) > 0.009999999776482582f) {
                     currentY = currentY + diff * Math.min(1.0f, lerpSpeed * deltaTime);
                  } else {
                     currentY = targetY;
                  }

                  this.currentYPositions.put(entryx, currentY);
                  String text = this.getEntryText(entryx);
                  String iconGlyph = entryx.categoryIcon != null && !entryx.categoryIcon.isEmpty() ? entryx.categoryIcon : "?";
                  float iconW = this.icons(14).getWidth(iconGlyph);
                  float width = this.getDefaultEntryWidth(entryx, padX);
                  float x = baseX + (maxWidth - width) * 0.5f;
                  float slide = 6.0f * (1.0f - appear);
                  float renderY = currentY + slide;
                  float scale = 0.8600000143051147f + 0.14000000059604645f * alpha;
                  boolean disabled = (!entryx.isCustom() && !entryx.enabled);
                  int disabledRed = ObsidianComponent_olorUtil.rgba(
                     200, 55, 55, 255
                  );
                  int base = ObsidianComponent_olorUtil.setAlphaColor(
                     ObsidianComponent_olorUtil.rgba(
                        50,
                        50,
                        50,
                        255
                     ),
                     (int)(255.0f * alpha)
                  );
                  int top = ObsidianComponent_olorUtil.setAlphaColor(
                     ObsidianComponent_olorUtil.darken(colorTheme, 0.15000000596046448f),
                     (int)(255.0f * alpha)
                  );
                  int bottom = ObsidianComponent_olorUtil.setAlphaColor(
                     ObsidianComponent_olorUtil.darken(colorTheme, 0.05000000074505806f),
                     (int)(255.0f * alpha)
                  );
                  float cx = x + width * 0.5f;
                  float cy = renderY + height * 0.5F;
                  MatrixStack ms = eventRender.getContext().getMatrices();
                  ms.push();
                  ms.translate(cx, cy, 0.0f);
                  ms.scale(scale, scale, 1.0f);
                  ms.translate(-cx, -cy, 0.0f);
                  RenderUtil.drawDefaultHudPanel(
                     ms,
                     x,
                     renderY,
                     width,
                     height,
                     3.0f,
                     3.5f,
                     base,
                     top,
                     bottom
                  );
                  float squareAlpha = alpha * alpha * (3.0F - 2.0f * alpha);
                  if (drawSquares && squareAlpha > 0.07999999821186066f) {
                     RenderUtil.drawHudSquarePattern(
                        ms,
                        x,
                        renderY,
                        width,
                        height,
                        ObsidianComponent_olorUtil.setAlphaColor(colorTheme, (int)(255.0f * squareAlpha))
                     );
                  }

                  int textColor = ObsidianComponent_olorUtil.setAlphaColor(
                     -1, (int)(255.0f * alpha)
                  );
                  int iconColor = ObsidianComponent_olorUtil.setAlphaColor(colorTheme, (int)(255.0f * alpha));
                  this.icons(14)
                     .draw(
                        ms,
                        iconGlyph,
                        x + padX - 1.5f,
                        renderY + 7.300000190734863f,
                        iconColor
                     );
                  float textX = x + padX + iconW + 1.0f;
                  if (!entryx.isCustom()) {
                     String modulePart = entryx.moduleName + " ";
                     String statePart = text.length() > modulePart.length() ? text.substring(modulePart.length()) : "";
                     int stateColor = disabled ? disabledRed : iconColor;
                     this.issue(13)
                        .draw(ms, modulePart, textX + 2.0F, renderY + 6.800000190734863f, textColor);
                     this.issue(13)
                        .draw(
                           ms,
                           statePart,
                           textX
                              + this.issue(13).getWidth(modulePart)
                              - 0.5f
                              + 2.0F,
                           renderY + 7.0f,
                           stateColor
                        );
                  } else {
                     this.issue(13)
                        .draw(ms, text, textX, renderY + 6.800000190734863f, textColor);
                  }

                  ms.pop();
                  targetY += height + spacing;
               }
            }

            this.activeEntriesScratch.clear();
            this.activeEntriesScratch.addAll(entries);
            this.appearAnimations
               .keySet()
               .removeIf(
                  entryx -> (!this.activeEntriesScratch.contains(entryx))
               );
            this.currentYPositions
               .keySet()
               .removeIf(
                  entryx -> (!this.activeEntriesScratch.contains(entryx))
               );
            this.draggable.setWidth(maxWidth);
            this.draggable.setHeight(Math.max(1.0f, targetY - baseY));
         }
      }
   }
   public void WaveStyle(EventRender2D eventRender) {
      if (mc != null) {
         long currentTime = System.currentTimeMillis();
         float deltaTime = (float)(currentTime - this.lastRenderTime) / 1000.0f;
         this.lastRenderTime = currentTime;
         List<FontTextureAtlas> entries = ObsidianComponent_283.getActive();
         if (entries.isEmpty()) {
            this.appearAnimations.clear();
            this.currentYPositions.clear();
         } else {
            int time = (int)(
               (float)(System.currentTimeMillis() % (2000L))
                  / 2000.0f
                  * 360.0f
            );
            int leftTop = ObsidianComponent_olorUtil.getThemeColor(time);
            int leftBottom = ObsidianComponent_olorUtil.getThemeColor(time + 30);
            int centerTop = ObsidianComponent_olorUtil.getThemeColor(time + 90);
            int centerBottom = ObsidianComponent_olorUtil.getThemeColor(time + 120);
            int rightTop = ObsidianComponent_olorUtil.getThemeColor(time + (180));
            int rightBottom = ObsidianComponent_olorUtil.getThemeColor(time + 210);
            long now = System.currentTimeMillis();
            float spacing = 4.0f;
            float lerpSpeed = 14.0f;
            float screenW = (float)mc.getWindow().getScaledWidth();
            float screenH = (float)mc.getWindow().getScaledHeight();
            float rightPadding = 5.0f;
            float bottomPadding = 5.0f;
            float stackOffset = 0.0f;
            float maxWidth = 120.0f;

            for (FontTextureAtlas entry : entries) {
               String title = "Notify";
               String body = this.getWaveBodyText(entry);
               float iconW = this.iconNew(14).getWidth("j");
               float titleW = this.issue(15).getWidth(title);
               float bodyW = this.issue(13).getWidth(body);
               float width = Math.max(
                  120.0f,
                  Math.max(
                     titleW + iconW + 18.0f,
                     bodyW + 14.0f
                  )
               );
               maxWidth = Math.max(maxWidth, width);
            }

            for (FontTextureAtlas entry : entries) {
               AnimationUtil anim = this.appearAnimations
                  .computeIfAbsent(
                     entry,
                     e -> new AnimationUtil(0.0f, 12.0f, ObsidianComponent_57.QUAD_OUT)
                  );
               anim.update(1.0f);
               float appear = anim.getValue();
               long age = now - entry.startTime;
               float alphaMul = appear;
               if (age > (2300L)) {
                  alphaMul = (
                        1.0f
                           - (float)(age - (2300L))
                              / 200.0f
                     )
                     * appear;
               }

               if (!(alphaMul <= 0.009999999776482582f)) {
                  String title = "Notify";
                  String body = this.getWaveBodyText(entry);
                  String warningGlyph = "j";
                  float iconW = this.iconNew(14).getWidth(warningGlyph);
                  float titleW = this.issue(15).getWidth(title);
                  float bodyW = this.issue(13).getWidth(body);
                  float width = Math.max(
                     120.0f,
                     Math.max(
                        titleW + iconW + 18.0f,
                        bodyW + 14.0f
                     )
                  );
                  float height = 24.0f;
                  float x = screenW - width - rightPadding;
                  float targetY = screenH - bottomPadding - height - stackOffset;
                  Float currentY = this.currentYPositions.get(entry);
                  if (currentY == null) {
                     currentY = targetY;
                  }

                  currentY = currentY + (targetY - currentY) * Math.min(1.0f, lerpSpeed * deltaTime);
                  this.currentYPositions.put(entry, currentY);
                  float y = currentY;
                  float scale = 0.86F + 0.14000000059604645f * alphaMul;
                  int bg = ObsidianComponent_olorUtil.rgba(
                     25,
                     25,
                     25,
                     (int)(150.0f * alphaMul)
                  );
                  int txt = ObsidianComponent_olorUtil.setAlphaColor(
                     -1, (int)(255.0f * alphaMul)
                  );
                  int iconCol = ObsidianComponent_olorUtil.setAlphaColor(
                     ObsidianComponent_olorUtil.rgba(
                        235,
                        0,
                        0,
                        255
                     ),
                     (int)(255.0f * alphaMul)
                  );
                  float cx = x + width * 0.5f;
                  float cy = y + height * 0.5f;
                  MatrixStack ms = eventRender.getContext().getMatrices();
                  ms.push();
                  ms.translate(cx, cy, 0.0f);
                  ms.scale(scale, scale, 1.0f);
                  ms.translate(-cx, -cy, 0.0f);
                  RenderUtil.drawWaveHudPanel(
                     ms,
                     x,
                     y,
                     width,
                     height - 1.5f,
                     bg,
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
                  this.iconNew(28)
                     .draw(
                        ms,
                        warningGlyph,
                        x + 3.0f,
                        y + 8.0f,
                        iconCol
                     );
                  this.issue(15)
                     .draw(ms, title, x + 19.0f, y + 6.5f, txt);
                  this.issue(13)
                     .draw(
                        ms,
                        body,
                        x + 19.0f,
                        y + 15.0f,
                        txt
                     );
                  ms.pop();
                  stackOffset += (height + spacing) * appear;
               }
            }

            this.appearAnimations
               .keySet()
               .removeIf(
                  entryx -> (!entries.contains(entryx))
               );
            this.currentYPositions
               .keySet()
               .removeIf(
                  entryx -> (!entries.contains(entryx))
               );
            this.draggable.setWidth(0.0f);
            this.draggable.setHeight(0.0f);
         }
      }
   }
}
