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
import net.minecraft.client.util.math.MatrixStack;

public class ObsidianComponent_190 {
   public static final float HOVER_SCROLL_OVERFLOW_THRESHOLD = 6.0F;

   public void render(
      DrawContext context, Module module, float panelX, float moduleY, float openProgress, int colorTheme, double mouseX, double mouseY, ObsidianComponent_271 state
   ) {
      List<Setting> settings = module.getSettings();
      if (settings != null && !settings.isEmpty() && !(openProgress <= 0.009999999776482582f)) {
         float maxSettingHeight = ObsidianComponent_200.calculateSettingsHeight(module);
         float settingsClipY = moduleY + 20.0f;
         float settingsClipHeight = maxSettingHeight * openProgress;
         ObsidianComponent_343.push();
         ObsidianComponent_343.setFromComponentCoordinates(
            (double)(panelX + 0.0f),
            (double)settingsClipY,
            115.0,
            (double)settingsClipHeight
         );
         float settingYoffset = 20.0f;

         for (Setting setting : settings) {
            if (setting != null && setting.visible()) {
               float settingY = moduleY + settingYoffset + 4.0f;
               int alpha = (int)(255.0f * openProgress);
               if (setting instanceof BooleanSetting booleanSetting) {
                  this.renderBooleanSetting(context, panelX, settingY, alpha, colorTheme, mouseX, mouseY, booleanSetting, state);
                  settingYoffset += 12.0f;
               } else if (setting instanceof ObsidianComponent_341 textSetting) {
                  this.renderTextSetting(context, panelX, settingY, alpha, colorTheme, mouseX, mouseY, textSetting, state);
                  settingYoffset += 22.0f;
               } else if (setting instanceof SliderSetting floatSetting) {
                  this.renderFloatSetting(context, panelX, settingY, alpha, colorTheme, mouseX, mouseY, floatSetting, state);
                  settingYoffset += 22.0f;
               } else if (setting instanceof ModeSetting modeSetting) {
                  this.renderModeSetting(context, panelX, settingY, alpha, colorTheme, mouseX, mouseY, modeSetting, state);
                  settingYoffset += ObsidianComponent_200.calculateModeSettingHeight(modeSetting);
               } else if (setting instanceof MultiBooleanSetting listSetting) {
                  this.renderListSetting(context, panelX, settingY, alpha, colorTheme, mouseX, mouseY, listSetting, state);
                  settingYoffset += ObsidianComponent_200.calculateListSettingHeight(listSetting);
               } else if (setting instanceof ObsidianComponent_261 bindSetting) {
                  this.renderBindSetting(context, panelX, settingY, alpha, colorTheme, mouseX, mouseY, bindSetting, state);
                  settingYoffset += 12.0f;
               }
            }
         }

         ObsidianComponent_343.pop();
      }
   }
   public void renderBooleanSetting(
      DrawContext context, float panelX, float settingY, int alpha, int colorTheme, double mouseX, double mouseY, BooleanSetting booleanSetting, ObsidianComponent_271 state
   ) {
      AnimationUtil backgroundAnimation = state.getBooleanBackgroundAnimation(booleanSetting);
      AnimationUtil circleAnimation = state.getBooleanCircleAnimation(booleanSetting);
      backgroundAnimation.update(
         booleanSetting.isState() ? 1.0f : 0.0f
      );
      circleAnimation.update(
         booleanSetting.isState() ? 1.0f : 0.0f
      );
      float backgroundProgress = backgroundAnimation.getValue();
      float circleProgress = circleAnimation.getValue();
      int offColor = ObsidianComponent_olorUtil.darken(colorTheme, 0.05000000074505806f);
      int r = (int)(
         (float)(offColor >> 16 & (255))
            + (float)(
                  (colorTheme >> 16 & 255)
                     - (offColor >> 16 & (255))
               )
               * backgroundProgress
      );
      int g = (int)(
         (float)(offColor >> 8 & (255))
            + (float)(
                  (colorTheme >> 8 & 255)
                     - (offColor >> 8 & (255))
               )
               * backgroundProgress
      );
      int b = (int)(
         (float)(offColor & 255)
            + (float)((colorTheme & 255) - (offColor & 255)) * backgroundProgress
      );
      int a = (int)(
         (float)(offColor >> 24 & (255))
            + (float)(
                  (colorTheme >> (24) & (255))
                     - (offColor >> 24 & 255)
               )
               * backgroundProgress
      );
      int interpolatedColor = a << (24) | r << 16 | g << (8) | b;
      float maxWidth = panelX + 73.0f - (panelX + 10.0f);
      this.drawStringWithHoverScroll(
         this.issue(13),
         context.getMatrices(),
         booleanSetting.name(),
         panelX + 10.0f - 7.0f,
         settingY,
         maxWidth,
         this.getPrimarySettingColor(alpha),
         mouseX,
         mouseY,
         state,
         this.getSettingTextKey(booleanSetting)
      );
      RenderUtil.drawRoundedRect(
         context.getMatrices(),
         panelX + 95.0f,
         settingY - 2.0f,
         16.0f,
         9.0f,
         3.5f,
         ObsidianComponent_olorUtil.rgba(
            interpolatedColor >> (16) & (255),
            interpolatedColor >> 8 & (255),
            interpolatedColor & (255),
            alpha
         )
      );
      float circleX = panelX
         + 79.5f
         + circleProgress * 6.199999809265137f;
      RenderUtil.drawRoundCircle(
         context.getMatrices(),
         circleX + 20.5f,
         settingY + 2.5f,
         7.0f,
         ObsidianComponent_olorUtil.rgba(255, 255, 255, alpha)
      );
   }
   public void renderFloatSetting(
      DrawContext context, float panelX, float settingY, int alpha, int colorTheme, double mouseX, double mouseY, SliderSetting floatSetting, ObsidianComponent_271 state
   ) {
      if (floatSetting.isActive()) {
         floatSetting.setValue(state.updateActiveSliderValue(floatSetting, mouseX));
      }

      AnimationUtil sliderAnimation = state.getSliderAnimation(floatSetting);
      sliderAnimation.update(state.getSliderPos(floatSetting));
      float animatedPos = sliderAnimation.getValue();
      String valueString = this.formatSliderValue(floatSetting);
      float valueX = panelX
         + 89.0f
         - this.issue(12).getWidth(valueString);
      float nameMaxWidth = valueX
         - 4.0f
         - (panelX + 10.0f);
      this.drawStringWithHoverScroll(
         this.issue(12),
         context.getMatrices(),
         floatSetting.name(),
         panelX + 10.0f - 6.0f,
         settingY + 1.0f,
         nameMaxWidth,
         this.getPrimarySettingColor(alpha),
         mouseX,
         mouseY,
         state,
         this.getSettingTextKey(floatSetting)
      );
      this.issue(12)
         .drawString(
            context.getMatrices(),
            valueString,
            valueX + 22.0f,
            settingY + 1.0f,
            ObsidianComponent_olorUtil.setAlphaColor(colorTheme, alpha)
         );
      int sliderBackgroundColor = ObsidianComponent_olorUtil.setAlphaColor(ObsidianComponent_olorUtil.darken(colorTheme, 0.20000000298023224f), alpha);
      RenderUtil.drawRoundedRect(
         context.getMatrices(),
         panelX + 10.0f,
         settingY + 9.0f,
         104.0f,
         4.5f,
         1.25f,
         sliderBackgroundColor
      );
      int sliderFillColor = ObsidianComponent_olorUtil.setAlphaColor(colorTheme, alpha);
      RenderUtil.drawRoundedRect(
         context.getMatrices(),
         panelX + 10.0f - 6.0f,
         settingY + 9.0f,
         animatedPos * 84.0f,
         4.5f,
         1.25f,
         sliderFillColor
      );
      RenderUtil.drawRoundCircle(
         context.getMatrices(),
         panelX
            + 10.0f
            + animatedPos * 84.0f
            - 6.0f,
         settingY + 11.25f,
         6.0f,
         ObsidianComponent_olorUtil.setAlphaColor(-1, alpha)
      );
   }
   public void renderTextSetting(
      DrawContext context, float panelX, float settingY, int alpha, int colorTheme, double mouseX, double mouseY, ObsidianComponent_341 textSetting, ObsidianComponent_271 state
   ) {
      String value = textSetting.get();
      boolean editing = (state.getEditingTextSetting() == textSetting);
      String preview = value != null && !value.isEmpty() ? value : "...";
      String boxText = editing ? preview + "_" : preview;
      float boxWidth = 42.0f;
      float boxX = panelX + 49.0f;
      this.drawStringWithHoverScroll(
         this.issue(13),
         context.getMatrices(),
         textSetting.name(),
         panelX + 10.0f,
         settingY,
         boxX - 1.0f - (panelX + 10.0f),
         this.getPrimarySettingColor(alpha),
         mouseX,
         mouseY,
         state,
         this.getSettingTextKey(textSetting)
      );
      int background = ObsidianComponent_olorUtil.setAlphaColor(editing ? colorTheme : ObsidianComponent_olorUtil.darken(colorTheme, 0.15F), alpha);
      int textColor = ObsidianComponent_olorUtil.setAlphaColor(-1, alpha);
      float boxY = settingY - 2.5f;
      RenderUtil.drawRoundedRect(
         context.getMatrices(),
         boxX,
         boxY,
         boxWidth,
         9.0f,
         1.5f,
         background
      );
      ObsidianComponent_343.push();
      ObsidianComponent_343.setFromComponentCoordinates(
         (double)(boxX + 2.0f),
         (double)(boxY + 1.0f),
         (double)(boxWidth - 4.0f),
         7.0
      );
      this.issue(12)
         .drawString(
            context.getMatrices(),
            boxText,
            boxX + 3.0f,
            settingY + 1.0f,
            textColor
         );
      ObsidianComponent_343.pop();
   }
   public void renderModeSetting(
      DrawContext context, float panelX, float settingY, int alpha, int colorTheme, double mouseX, double mouseY, ModeSetting modeSetting, ObsidianComponent_271 state
   ) {
      this.drawStringWithHoverScroll(
         this.issue(12),
         context.getMatrices(),
         modeSetting.name(),
         panelX + 10.0f - 6.0f,
         settingY + 1.0f,
         79.0f,
         this.getPrimarySettingColor(alpha),
         mouseX,
         mouseY,
         state,
         this.getSettingTextKey(modeSetting)
      );
      float modeY = settingY + 10.0f;

      for (String mode : modeSetting.getMods()) {
         boolean selected = modeSetting.getCurrent().equals(mode);
         AnimationUtil animation = state.getModeAnimation(this.getModeKey(modeSetting, mode), selected);
         animation.update(selected ? 1.0f : 0.0f);
         float progress = animation.getValue();
         int outerColor = ObsidianComponent_olorUtil.setAlphaColor(
            colorTheme,
            (int)(
               (float)alpha
                  * (
                     0.30000001192092896f
                        + 0.699999988079071f * progress
                  )
            )
         );
         int innerColor = selected
            ? ObsidianComponent_olorUtil.setAlphaColor(ObsidianComponent_olorUtil.darken(colorTheme, 0.4000000059604645f), alpha)
            : ObsidianComponent_olorUtil.rgba(
               255, 255, 255, alpha
            );
         this.issue(13)
            .draw(
               context.getMatrices(),
               mode,
               panelX + 10.0f - 6.0f,
               modeY,
               this.getSecondarySettingColor(alpha)
            );
         RenderUtil.drawRoundCircle(
            context.getMatrices(),
            panelX + 106.0f,
            modeY + 2.0f,
            9.0f,
            outerColor
         );
         RenderUtil.drawRoundCircle(
            context.getMatrices(),
            panelX + 106.0f,
            modeY + 2.0f,
            6.0f
               - progress * 2.0f
               + 3.0f,
            innerColor
         );
         modeY += 10.0f;
      }
   }
   public void renderListSetting(
      DrawContext context, float panelX, float settingY, int alpha, int colorTheme, double mouseX, double mouseY, MultiBooleanSetting listSetting, ObsidianComponent_271 state
   ) {
      this.drawStringWithHoverScroll(
         this.issue(12),
         context.getMatrices(),
         listSetting.name(),
         panelX + 10.0f - 6.0f,
         settingY + 1.0f,
         79.0f,
         this.getPrimarySettingColor(alpha),
         mouseX,
         mouseY,
         state,
         this.getSettingTextKey(listSetting)
      );
      float listY = settingY + 10.0f;

      for (BooleanSetting entry : listSetting.getSettings()) {
         if (entry.visible()) {
            boolean selected = entry.isState();
            AnimationUtil animation = state.getListAnimation(this.getListKey(listSetting, entry), selected);
            animation.update(selected ? 1.0f : 0.0f);
            float progress = animation.getValue();
            int outerColor = ObsidianComponent_olorUtil.setAlphaColor(
               colorTheme,
               (int)(
                  (float)alpha
                     * (0.30000001192092896f + 0.699999988079071f * progress)
               )
            );
            int innerColor = selected
               ? ObsidianComponent_olorUtil.setAlphaColor(ObsidianComponent_olorUtil.darken(colorTheme, 0.4F), alpha)
               : ObsidianComponent_olorUtil.rgba(255, 255, 255, alpha);
            this.drawStringWithHoverScroll(
               this.issue(13),
               context.getMatrices(),
               entry.name(),
               panelX + 10.0f - 6.0f,
               listY,
               panelX + 73.0f - (panelX + 10.0f),
               this.getSecondarySettingColor(alpha),
               mouseX,
               mouseY,
               state,
               this.getListKey(listSetting, entry) + "_text"
            );
            RenderUtil.drawRoundCircle(
               context.getMatrices(),
               panelX + 106.0f,
               listY + 2.0f,
               9.0f,
               outerColor
            );
            RenderUtil.drawRoundCircle(
               context.getMatrices(),
               panelX + 106.0f,
               listY + 2.0f,
               6.0f
                  - progress * 2.0f
                  + 3.0f,
               innerColor
            );
            listY += 10.0f;
         }
      }
   }
   public void renderBindSetting(
      DrawContext context, float panelX, float settingY, int alpha, int colorTheme, double mouseX, double mouseY, ObsidianComponent_261 bindSetting, ObsidianComponent_271 state
   ) {
      boolean binding = (state.getBindingSetting() == bindSetting);
      AnimationUtil bindAnimation = state.getBindAnimation(this.getBindKey(bindSetting), binding);
      bindAnimation.update(
         binding ? 1.0f : 0.0f
      );
      float progress = bindAnimation.getValue();
      String bindString = binding ? "..." : state.toEnglish(ObsidianComponent_349.getBindName(bindSetting.getKey()));
      float bindTextWidth = this.issue(12).getWidth(bindString);
      float bindWidth = bindTextWidth + 6.0f;
      float bindX = panelX + 89.0f - bindWidth;
      int bindBackgroundColor = ObsidianComponent_olorUtil.setAlphaColor(
         ObsidianComponent_olorUtil.interpolateColor(ObsidianComponent_olorUtil.darken(colorTheme, 0.15000000596046448f), colorTheme, progress), alpha
      );
      int bindTextColor = ObsidianComponent_olorUtil.setAlphaColor(
         ObsidianComponent_olorUtil.interpolateColor(
            ObsidianComponent_olorUtil.rgb(140, 139, 145),
            -1,
            progress
         ),
         alpha
      );
      RenderUtil.drawRoundedRect(
         context.getMatrices(),
         bindX,
         settingY - 2.5f,
         bindWidth,
         9.0f,
         1.5f,
         bindBackgroundColor
      );
      this.issue(12)
         .drawString(
            context.getMatrices(),
            bindString,
            bindX + 3.0f,
            settingY + 1.0f,
            bindTextColor
         );
      this.drawStringWithHoverScroll(
         this.issue(12),
         context.getMatrices(),
         bindSetting.name(),
         panelX + 10.0f,
         settingY + 1.0f,
         bindX - 4.0f - (panelX + 10.0f),
         this.getPrimarySettingColor(alpha),
         mouseX,
         mouseY,
         state,
         this.getSettingTextKey(bindSetting)
      );
   }
   public String getModeKey(ModeSetting setting, String mode) {
      return System.identityHashCode(setting) + "_mode_" + mode;
   }
   public String getListKey(MultiBooleanSetting setting, BooleanSetting entry) {
      return setting.hashCode() + "_list_" + entry.name();
   }
   public String getBindKey(ObsidianComponent_261 setting) {
      return setting.hashCode() + "_bind";
   }
   public String formatSliderValue(SliderSetting setting) {
      float value = setting.get();
      float increment = setting.getIncrement();
      if (increment >= 1.0f) {
         return String.valueOf((int)value);
      } else if (increment >= 0.10000000149011612f) {
         Object[] var4 = new Object[1];
         var4[0] = value;
         return String.format("%.1f", var4);
      } else {
         Object[] var10001 = new Object[1];
         var10001[0] = value;
         return String.format("%.2f", var10001);
      }
   }
   public void drawStringWithHoverScroll(
      FontRenderer font,
      MatrixStack matrix,
      String text,
      float x,
      float y,
      float maxWidth,
      int color,
      double mouseX,
      double mouseY,
      ObsidianComponent_271 state,
      String animationKey
   ) {
      if (text != null && !text.isEmpty() && !(maxWidth <= 0.0f)) {
         float totalWidth = font.getWidth(text);
         float overflow = totalWidth - maxWidth;
         if (overflow <= 6.0f) {
            font.draw(matrix, text, x, y, color);
         } else {
            boolean hovered = this.isTextHovered(x, y, maxWidth, font.getHeight(), mouseX, mouseY);
            float scrollPhase = state.advanceTextScrollPhase(animationKey, hovered);
            boolean scrollActive = state.isTextScrollActive(animationKey, hovered);
            AnimationUtil hoverAnimation = state.getTextHoverAnimation(animationKey, scrollActive);
            hoverAnimation.update(
               scrollActive ? 1.0f : 0.0f
            );
            float hoverProgress = hoverAnimation.getValue();
            float scrollOffset = this.getHoverScrollOffset(overflow, scrollPhase) * hoverProgress;
            ObsidianComponent_343.push();
            ObsidianComponent_343.setFromComponentCoordinates(
               (double)x,
               (double)(y - 2.0f),
               (double)maxWidth,
               (double)(font.getHeight() + 4.0f)
            );
            font.draw(matrix, text, x - scrollOffset, y, color);
            ObsidianComponent_343.pop();
         }
      }
   }
   public int getPrimarySettingColor(int alpha) {
      return ObsidianComponent_olorUtil.rgba(
         245, 245, 248, alpha
      );
   }
   public int getSecondarySettingColor(int alpha) {
      return ObsidianComponent_olorUtil.rgba(186, 186, 194, alpha);
   }
   public boolean isTextHovered(float x, float y, float width, float height, double mouseX, double mouseY) {
      return (mouseX >= (double)x
            && mouseX <= (double)(x + width)
            && mouseY >= (double)(y - 2.0f)
            && mouseY <= (double)(y + height + 2.0f));
   }
   public float getHoverScrollOffset(float maxOffset, float phase) {
      if (maxOffset <= 0.0f) {
         return 0.0f;
      } else {
         float pingPong = phase < 0.5f
            ? phase * 2.0f
            : 2.0f - phase * 2.0f;
         float eased = pingPong
            * pingPong
            * (3.0f - 2.0f * pingPong);
         return maxOffset * eased;
      }
   }
   public String getSettingTextKey(Setting setting) {
      return "setting_text_" + System.identityHashCode(setting);
   }
   public FontRenderer issue(int size) {
      return ObsidianComponent_201.getFont("suisse", size);
   }
}
