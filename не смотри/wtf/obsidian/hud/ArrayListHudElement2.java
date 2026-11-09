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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.MathHelper;

public class ArrayListHudElement2 extends DraggableHudElement {
   private final Map<StatusEffect, AnimationUtil> animations = new LinkedHashMap<>();
   private final Map<StatusEffect, ObsidianComponent_89> snapshots = new HashMap<>();
   private final Map<StatusEffect, Integer> maxDurations = new HashMap<>();
   private final Set<StatusEffect> renderOrderSeen = new HashSet<>();
   private final AnimationUtil widthAnimation = new AnimationUtil(
      70.0f, 10.5f, ObsidianComponent_57.QUAD_OUT
   );

   public ArrayListHudElement2(ObsidianComponent_280 draggable) {
      super(draggable);
   }
   public FontRenderer issue(int size) {
      return ObsidianComponent_201.getFont("suisse", size);
   }
   public FontRenderer icon(int size) {
      return ObsidianComponent_201.getFont("icon", size);
   }
   public AnimationUtil getAnimation(StatusEffect effect) {
      return this.animations
         .computeIfAbsent(
            effect,
            e -> new AnimationUtil(
                  0.0f,
                  10.5f,
                  ObsidianComponent_57.QUAD_OUT
               )
         );
   }
   public static String getLevelSuffix(int level) {
      return String.valueOf(Math.max(1, level));
   }
   public static String formatDuration(StatusEffectInstance effect) {
      return formatDuration(effect.getDuration(), effect.isInfinite());
   }
   public static String formatDuration(int duration, boolean infinite) {
      if (infinite) {
         return "inf";
      } else {
         int seconds = Math.max(0, duration / 20);
         int minutes = seconds / 60;
         int secs = seconds % (60);
         return minutes + ":" + (secs < (10) ? "0" + secs : String.valueOf(secs));
      }
   }
   public void updateSnapshot(StatusEffectInstance effect) {
      StatusEffect type = (StatusEffect)effect.getEffectType().value();
      ObsidianComponent_89 snapshot = this.snapshots.computeIfAbsent(type, e -> new ObsidianComponent_89());
      snapshot.entry = effect.getEffectType();
      snapshot.baseName = I18n.translate(effect.getTranslationKey(), new Object[0]);
      snapshot.amplifier = effect.getAmplifier() + (1);
      snapshot.duration = effect.getDuration();
      snapshot.infinite = effect.isInfinite();
   }
   public List<StatusEffect> buildRenderOrder(Collection<StatusEffectInstance> effects, Set<StatusEffect> active) {
      List<StatusEffect> order = new ArrayList<>();
      this.renderOrderSeen.clear();

      for (StatusEffectInstance effect : effects) {
         StatusEffect type = (StatusEffect)effect.getEffectType().value();
         if (this.renderOrderSeen.add(type)) {
            order.add(type);
         }
      }

      for (StatusEffect type : this.animations.keySet()) {
         if (!active.contains(type)) {
            order.add(type);
         }
      }

      return order;
   }
   public void drawEffectIcon(EventRender2D eventRender, RegistryEntry<StatusEffect> effect, float x, float y, int size, int alpha) {
      Sprite sprite = mc.getStatusEffectSpriteManager().getSprite(effect);
      int color = ObsidianComponent_olorUtil.rgba(255, 255, 255, alpha);
      RenderUtil.drawSprite(eventRender.getContext().getMatrices(), sprite, x, y, (float)size, color);
   }
   public void drawTextWithShadow(EventRender2D eventRender, FontRenderer font, String text, float x, float y, int color) {
      int shadow = ObsidianComponent_olorUtil.rgba(
         20,
         20,
         20,
         145
      );
      font.draw(
         eventRender.getContext().getMatrices(),
         text,
         x + 0.800000011920929f,
         y + 0.800000011920929f,
         shadow
      );
      font.draw(eventRender.getContext().getMatrices(), text, x, y, color);
   }

   @Override
   public void onRender(EventRender2D eventRender) {
      if (ModuleManager.interfaceModule.style.is("Обычный")) {
         this.DefaultStyle(eventRender);
      } else {
         this.WaveStyle(eventRender);
      }

      super.onRender(eventRender);
   }

   public void DefaultStyle(EventRender2D eventRender) {
      float x = this.draggable.getX();
      float y = this.draggable.getY();
      int colorTheme;
      if (!Obsidian.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")) {
         colorTheme = Obsidian.INSTANCE.themeStorage.getThemes().getTheme().color[0];
      } else {
         colorTheme = ObsidianComponent_olorUtil.getThemeColor();
      }

      float targetWidth = 70.0f;
      float targetHeight = 16.0f;
      int visibleCount = 0;
      Collection<StatusEffectInstance> effects = (Collection<StatusEffectInstance>)(mc != null && mc.player != null ? mc.player.getStatusEffects() : List.of());
      Set<StatusEffect> active = new HashSet<>();

      for (StatusEffectInstance effect : effects) {
         StatusEffect type = (StatusEffect)effect.getEffectType().value();
         active.add(type);
         this.getAnimation(type).update(1.0f);
         this.updateSnapshot(effect);
         int duration = effect.getDuration();
         Integer prevMax = this.maxDurations.get(type);
         if (prevMax == null || duration > prevMax) {
            this.maxDurations.put(type, duration);
         }
      }

      for (Entry<StatusEffect, AnimationUtil> entry : this.animations.entrySet()) {
         if (!active.contains(entry.getKey())) {
            entry.getValue().update(0.0f);
         }
      }

      List<StatusEffect> renderOrder = this.buildRenderOrder(effects, active);

      for (StatusEffect type : renderOrder) {
         AnimationUtil anim = this.getAnimation(type);
         float animValue = anim.getValue();
         ObsidianComponent_89 snapshot = this.snapshots.get(type);
         if (animValue > 0.009999999776482582f && snapshot != null) {
            visibleCount++;
            String baseName = snapshot.baseName != null
               ? snapshot.baseName
               : I18n.translate(type.getTranslationKey(), new Object[0]);
            String levelSuffix = getLevelSuffix(snapshot.amplifier);
            String time = formatDuration(snapshot.duration, snapshot.infinite);
            float nameWidth = this.issue(12).getWidth(baseName);
            if (!levelSuffix.isEmpty()) {
               nameWidth += this.issue(11).getWidth(" LVL")
                  + this.issue(12).getWidth(levelSuffix);
            }

            float timeWidth = this.issue(10).getWidth(time) + 6.0F;
            float rowWidth = nameWidth
               + timeWidth
               + 25.0f
               + 9.0f
               + 9.0f;
            if (rowWidth > targetWidth) {
               targetWidth = rowWidth;
            }

            targetHeight += 12.0f * animValue;
         }
      }

      if (visibleCount > 0) {
         targetHeight += 2.0f;
      }

      this.widthAnimation.update(targetWidth);
      float width = this.widthAnimation.getValue();
      float height = targetHeight;
      RenderUtil.drawDefaultHudElementRects(eventRender.getContext().getMatrices(), x, y, width, targetHeight, colorTheme, this.isUnusualRectType());
      this.issue(14)
         .draw(
            eventRender.getContext().getMatrices(),
            "Effects",
            x + 5.0f,
            y + 6.0f,
            -1
         );
      this.icon(13)
         .draw(
            eventRender.getContext().getMatrices(),
            "d",
            x + width - 12.5f,
            y + 7.5f,
            colorTheme
         );
      float offsetY = 18.0f;

      for (StatusEffect typex : renderOrder) {
         AnimationUtil anim = this.getAnimation(typex);
         float animValue = anim.getValue();
         ObsidianComponent_89 snapshot = this.snapshots.get(typex);
         if (animValue > 0.009999999776482582f && snapshot != null) {
            ObsidianComponent_343.push();
            ObsidianComponent_343.setFromComponentCoordinates((double)x, (double)y, (double)width, (double)height);
            int alpha = (int)(255.0f * animValue);
            int textColor = ObsidianComponent_olorUtil.rgba(255, 255, 255, alpha);
            int grayColor = ObsidianComponent_olorUtil.rgba(55, 55, 55, alpha);
            int darkColor = ObsidianComponent_olorUtil.rgba(
               35, 35, 35, alpha
            );
            float iconSize = 7.0f;
            float iconX = x + 5.0f;
            float iconY = y + offsetY;
            if (snapshot.entry != null) {
               this.drawEffectIcon(eventRender, snapshot.entry, iconX, iconY, (int)iconSize, alpha);
            }

            String baseNamex = snapshot.baseName != null
               ? snapshot.baseName
               : I18n.translate(typex.getTranslationKey(), new Object[0]);
            String levelSuffixx = getLevelSuffix(snapshot.amplifier);
            float textX = iconX + iconSize + 3.0f;
            float textY = y + 2.0f + offsetY;
            this.issue(12).draw(eventRender.getContext().getMatrices(), baseNamex, textX, textY, textColor);
            if (!levelSuffixx.isEmpty()) {
               float baseWidth = this.issue(12).getWidth(baseNamex);
               int levelThemeColor = ObsidianComponent_olorUtil.setAlphaColor(colorTheme, alpha);
               float lvlX = textX + baseWidth;
               this.issue(10)
                  .draw(
                     eventRender.getContext().getMatrices(),
                     " LVL",
                     lvlX,
                     textY + 1.0f,
                     levelThemeColor
                  );
               this.issue(11)
                  .draw(
                     eventRender.getContext().getMatrices(),
                     levelSuffixx,
                     (double)(lvlX + this.issue(11).getWidth(" LVL")),
                     (double)textY + 0.5,
                     levelThemeColor
                  );
            }

            String timex = formatDuration(snapshot.duration, snapshot.infinite);
            float timeBoxWidth = Math.max(
               this.issue(10).getWidth(timex) + 4.0f,
               12.0f
            );
            float ringSize = 6.0f;
            float ringGap = 3.0F;
            float timeBoxX = x + width - timeBoxWidth - 5.0f;
            float ringX = timeBoxX - ringGap - ringSize;
            float ringY = y + offsetY + 0.30000001192092896f;
            RenderUtil.drawDefaultHudInfoBox(eventRender.getContext().getMatrices(), timeBoxX, y + offsetY, timeBoxWidth, grayColor, darkColor);
            this.issue(10)
               .drawCenteredString(
                  eventRender.getContext().getMatrices(),
                  timex,
                  timeBoxX + timeBoxWidth / 2.0f,
                  y + offsetY + 3.0f,
                  textColor
               );
            float progress = 1.0f;
            if (!snapshot.infinite) {
               int currentDuration = snapshot.duration;
               int maxDuration = this.maxDurations.getOrDefault(typex, currentDuration);
               if (maxDuration > 0) {
                  progress = MathHelper.clamp(
                     (float)currentDuration / (float)maxDuration,
                     0.0f,
                     1.0f
                  );
               } else {
                  progress = 0.0f;
               }
            }

            int ringColor = ObsidianComponent_olorUtil.setAlphaColor(colorTheme, alpha);
            float thickness = 1.75f;
            RenderUtil.drawRingArc(
               eventRender.getContext().getMatrices(),
               ringX,
               ringY,
               ringSize,
               thickness,
               -90.0f,
               270.0f,
               grayColor
            );
            if (progress > 0.0f) {
               float endAngle = -90.0f
                  + 360.0f * progress;
               RenderUtil.drawRingArc(
                  eventRender.getContext().getMatrices(),
                  ringX,
                  ringY,
                  ringSize,
                  thickness,
                  -90.0f,
                  endAngle,
                  ringColor
               );
            }

            offsetY += 12.0f * animValue;
            ObsidianComponent_343.pop();
            ObsidianComponent_343.unset();
         }
      }

      this.animations
         .entrySet()
         .removeIf(
            entryx -> (!active.contains(entryx.getKey())
                     && entryx.getValue().getValue() <= 0.009999999776482582f)
         );
      this.snapshots
         .keySet()
         .removeIf(
            typexx -> (!this.animations.containsKey(typexx))
         );
      this.maxDurations
         .keySet()
         .removeIf(typexx -> (!this.animations.containsKey(typexx)));
      this.draggable.setWidth(width);
      this.draggable.setHeight(height);
   }

   public void WaveStyle(EventRender2D eventRender) {
      float x = this.draggable.getX();
      float y = this.draggable.getY();
      int time = (int)(
         (float)(System.currentTimeMillis() % (2000L))
            / 2000.0f
            * 360.0f
      );
      int leftTop = ObsidianComponent_olorUtil.getThemeColor(time);
      int leftBottom = ObsidianComponent_olorUtil.getThemeColor(time + (30));
      int centerTop = ObsidianComponent_olorUtil.getThemeColor(time + (90));
      int centerBottom = ObsidianComponent_olorUtil.getThemeColor(time + (120));
      int rightTop = ObsidianComponent_olorUtil.getThemeColor(time + (180));
      int rightBottom = ObsidianComponent_olorUtil.getThemeColor(time + 210);
      Collection<StatusEffectInstance> effects = (Collection<StatusEffectInstance>)(mc != null && mc.player != null ? mc.player.getStatusEffects() : List.of());
      Set<StatusEffect> active = new HashSet<>();

      for (StatusEffectInstance effect : effects) {
         StatusEffect type = (StatusEffect)effect.getEffectType().value();
         active.add(type);
         this.getAnimation(type).update(1.0f);
      }

      for (Entry<StatusEffect, AnimationUtil> entry : this.animations.entrySet()) {
         if (!active.contains(entry.getKey())) {
            entry.getValue().update(0.0f);
         }
      }

      float width = 84.0f;
      float height = 18.0f;
      int visibleEffects = 0;

      for (StatusEffectInstance effect : effects) {
         AnimationUtil anim = this.getAnimation((StatusEffect)effect.getEffectType().value());
         float animValue = anim.getValue();
         if (!(animValue <= 0.009999999776482582f)) {
            visibleEffects++;
            String baseName = I18n.translate(effect.getTranslationKey(), new Object[0]);
            String levelSuffix = getLevelSuffix(effect.getAmplifier() + (1));
            String line = baseName + (levelSuffix.isEmpty() ? "" : " > " + levelSuffix);
            width = Math.max(
               width, this.issue(16).getWidth(line) + 38.0f
            );
            width = Math.max(
               width,
               this.issue(15).getWidth(formatDuration(effect)) + 38.0f
            );
            height += 18.0f * animValue;
         }
      }

      if (visibleEffects == 0) {
         float headerHeight = 18.0f;
         RenderUtil.drawWaveHudHeader(
            eventRender.getContext().getMatrices(),
            x,
            y,
            width,
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
         String title = "potions";
         float titleX = x
            + (width - this.issue(16).getWidth(title)) / 2.0f;
         this.drawTextWithShadow(
            eventRender, this.issue(16), title, titleX, y + 5.0F, -1
         );
         this.draggable.setWidth(width);
         this.draggable.setHeight(headerHeight);
      } else {
         RenderUtil.drawWaveHudPanel(
            eventRender.getContext().getMatrices(),
            x,
            y,
            width,
            height,
            ObsidianComponent_olorUtil.rgba(
               25,
               25,
               25,
               150
            ),
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
         String title = "potions";
         float titleX = x
            + (width - this.issue(16).getWidth(title))
               / 1.899999976158142f;
         this.drawTextWithShadow(
            eventRender,
            this.issue(16),
            title,
            titleX,
            y + 5.0f,
            -1
         );
         float yOffset = 20.0f;

         for (StatusEffectInstance effectx : effects) {
            AnimationUtil anim = this.getAnimation((StatusEffect)effectx.getEffectType().value());
            float animValue = anim.getValue();
            if (!(animValue <= 0.009999999776482582f)) {
               ObsidianComponent_343.push();
               ObsidianComponent_343.setFromComponentCoordinates((double)x, (double)y, (double)width, (double)height);
               int alpha = (int)(255.0f * animValue);
               int textColor = ObsidianComponent_olorUtil.rgba(255, 255, 255, alpha);
               int levelColor = ObsidianComponent_olorUtil.rgba(
                  20, 185, 45, alpha
               );
               float iconX = x + 5.0f;
               float iconY = y + yOffset;
               this.drawEffectIcon(eventRender, effectx.getEffectType(), iconX, iconY, 11, alpha);
               String baseName = I18n.translate(effectx.getTranslationKey(), new Object[0]).toLowerCase();
               String levelSuffix = getLevelSuffix(effectx.getAmplifier() + 1);
               float textX = iconX + 14.0f;
               this.issue(15)
                  .draw(
                     eventRender.getContext().getMatrices(),
                     baseName + " >",
                     textX,
                     y + yOffset - 1.0f,
                     textColor
                  );
               if (!levelSuffix.isEmpty()) {
                  float nameW = this.issue(14).getWidth(baseName + " >");
                  this.issue(14)
                     .draw(
                        eventRender.getContext().getMatrices(),
                        " " + levelSuffix,
                        (double)(textX + nameW + 2.0f),
                        (double)(y + yOffset) - 0.5,
                        levelColor
                     );
               }

               this.issue(14)
                  .draw(
                     eventRender.getContext().getMatrices(),
                     formatDuration(effectx),
                     (double)textX,
                     (double)(y + yOffset) + 7.5,
                     textColor
                  );
               yOffset += 18.0f * animValue;
               ObsidianComponent_343.pop();
               ObsidianComponent_343.unset();
            }
         }

         this.draggable.setWidth(width);
         this.draggable.setHeight(height);
      }
   }
}
