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
























import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.util.math.MatrixStack;

public class KeybindsHudElement extends DraggableHudElement {
   private final Map<Module, AnimationUtil> animations = new HashMap<>();
   private final AnimationUtil widthAnimation = new AnimationUtil(
      60.0f, 10.5f, ObsidianComponent_57.QUAD_OUT
   );
   private static final Map<Character, Character> RU_TO_EN = new HashMap<>();

   public KeybindsHudElement(ObsidianComponent_280 draggable) {
      super(draggable);
   }
   public FontRenderer issue(int size) {
      return ObsidianComponent_201.getFont("suisse", size);
   }
   public FontRenderer icon(int size) {
      return ObsidianComponent_201.getFont("icon1", size);
   }
   public AnimationUtil getAnimation(Module module) {
      return this.animations
         .computeIfAbsent(
            module,
            m -> new AnimationUtil(
                  0.0f,
                  10.5f,
                  ObsidianComponent_57.QUAD_OUT
               )
         );
   }
   public String toEnglish(String text) {
      StringBuilder result = new StringBuilder();
      char[] var3 = text.toCharArray();
      int var4 = var3.length;

      for (int var5 = 0; var5 < var4; var5++) {
         char c = var3[var5];
         result.append(RU_TO_EN.getOrDefault(c, c));
      }

      return result.toString();
   }
   public int getStaticThemeColor() {
      int[] colors = Obsidian.INSTANCE.themeStorage.getThemes().getTheme().getColor();
      if (colors != null && colors.length != 0) {
         int color = colors[0];
         if ((color >> (24) & 255) == 0) {
            color = color & (16777215) | -16777216;
         }

         return color;
      } else {
         return -1;
      }
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
      float baseX = this.draggable.getX();
      float y = this.draggable.getY();
      int colorTheme;
      if (!Obsidian.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")) {
         colorTheme = Obsidian.INSTANCE.themeStorage.getThemes().getTheme().color[0];
      } else {
         colorTheme = ObsidianComponent_olorUtil.getThemeColor();
      }

      int staticAccentColor = this.getStaticThemeColor();
      float targetWidth = 64.0f;
      float targetHeight = 16.0f;
      int visibleCount = 0;
      ObjectListIterator width = ModuleManager.INSTANCE.getObject().iterator();

      while (width.hasNext()) {
         Module module = (Module)width.next();
         if (module.getKey() != (-1)) {
            AnimationUtil anim = this.getAnimation(module);
            anim.update(
               module.isEnable()
                  ? 1.0f
                  : 0.0f
            );
         }
      }

      width = ModuleManager.INSTANCE.getObject().iterator();

      while (width.hasNext()) {
         Module module = (Module)width.next();
         if (module.getKey() != -1) {
            AnimationUtil anim = this.getAnimation(module);
            float animValue = anim.getValue();
            if (animValue > 0.009999999776482582f) {
               visibleCount++;
               String keyName = this.toEnglish(ObsidianComponent_349.getKeyName(module.getKey()));
               float keyWidth = this.issue(10).getWidth(keyName);
               float moduleWidth = this.issue(12).getWidth(module.getDisplayName())
                  + keyWidth
                  + 25.0f;
               if (moduleWidth > targetWidth) {
                  targetWidth = moduleWidth;
               }

               targetHeight += 12.0f * animValue;
            }
         }
      }

      if (visibleCount > 0) {
         targetHeight += 2.0f;
      }

      this.widthAnimation.update(targetWidth);
      float widthx = this.widthAnimation.getValue() + 7.0f;
      float height = targetHeight;
      float rightEdge = baseX + 60.0f;
      float x = rightEdge - widthx;
      RenderUtil.drawDefaultHudElementRects(eventRender.getContext().getMatrices(), x, y, widthx, targetHeight, colorTheme, this.isUnusualRectType());
      this.issue(14)
         .draw(
            eventRender.getContext().getMatrices(),
            "Binds",
            x + 5.0f,
            y + 6.0f,
            -1
         );
      this.icon(13)
         .draw(
            eventRender.getContext().getMatrices(),
            "f",
            rightEdge - 13.0f,
            y + 7.5f,
            colorTheme
         );
      float offsetY = 18.0f;
      ObjectListIterator var34 = ModuleManager.INSTANCE.getObject().iterator();

      while (var34.hasNext()) {
         Module module = (Module)var34.next();
         if (module.getKey() != (-1)) {
            AnimationUtil anim = this.getAnimation(module);
            float animValue = anim.getValue();
            if (animValue > 0.009999999776482582f) {
               ObsidianComponent_343.push();
               ObsidianComponent_343.setFromComponentCoordinates((double)x, (double)y, (double)widthx, (double)height);
               String keyName = this.toEnglish(ObsidianComponent_349.getBindName(module.getKey()));
               float keyBoxWidth = Math.max(
                  this.issue(10).getWidth(keyName) + 4.0f,
                  9.0f
               );
               int alpha = (int)(255.0f * animValue);
               int textColor = ObsidianComponent_olorUtil.rgba(
                  255, 255, 255, alpha
               );
               int accentColor = ObsidianComponent_olorUtil.setAlphaColor(this.getStableThemeColor(), alpha);
               int grayColor = ObsidianComponent_olorUtil.rgba(
                  55, 55, 55, alpha
               );
               int darkColor = ObsidianComponent_olorUtil.rgba(35, 35, 35, alpha);
               this.issue(12)
                  .draw(
                     eventRender.getContext().getMatrices(),
                     module.getDisplayName(),
                     x + 12.0f,
                     y + 2.0F + offsetY,
                     textColor
                  );
               RenderUtil.drawRoundedRect(
                  eventRender.getContext().getMatrices(),
                  x + 5.199999809265137f,
                  y + offsetY + 0.30000001192092896f,
                  2.549999952316284f,
                  5.699999809265137f,
                  0.15000000596046448f,
                  accentColor
               );
               float keyBoxX = rightEdge - keyBoxWidth - 5.0f;
               RenderUtil.drawDefaultHudInfoBox(eventRender.getContext().getMatrices(), keyBoxX, y + offsetY, keyBoxWidth, grayColor, darkColor);
               this.issue(10)
                  .drawCenteredString(
                     eventRender.getContext().getMatrices(),
                     keyName,
                     keyBoxX + keyBoxWidth / 2.0f,
                     y + offsetY + 2.799999952316284f,
                     textColor
                  );
               offsetY += 12.0f * animValue;
               ObsidianComponent_343.pop();
               ObsidianComponent_343.unset();
            }
         }
      }

      this.draggable.setWidth(60.0f);
      this.draggable.setHeight(height);
   }
   public int getStableThemeColor() {
      return !Obsidian.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")
         ? Obsidian.INSTANCE.themeStorage.getThemes().getTheme().color[0]
         : ObsidianComponent_olorUtil.getThemeColor();
   }

   public void WaveStyle(EventRender2D eventRender) {
      MatrixStack context = eventRender.getContext().getMatrices();
      float x = this.draggable.getX();
      float y = this.draggable.getY();
      int time = (int)(
         (float)(System.currentTimeMillis() % (2000L))
            / 2000.0f
            * 360.0f
      );
      int leftTop = ObsidianComponent_olorUtil.getThemeColor(time);
      int leftBottom = ObsidianComponent_olorUtil.getThemeColor(time + 30);
      int centerTop = ObsidianComponent_olorUtil.getThemeColor(time + 90);
      int centerBottom = ObsidianComponent_olorUtil.getThemeColor(time + 120);
      int rightTop = ObsidianComponent_olorUtil.getThemeColor(time + 180);
      int rightBottom = ObsidianComponent_olorUtil.getThemeColor(time + 210);
      List<Module> activeModules = new ArrayList<>();
      ObjectListIterator targetWidth = ModuleManager.INSTANCE.getObject().iterator();

      while (targetWidth.hasNext()) {
         Module module = (Module)targetWidth.next();
         if (module.getKey() <= 0) {
            module.getAnimka().update(0.0f);
         } else {
            module.getAnimka()
               .update(
                  module.isEnable()
                     ? 1.0f
                     : 0.0f
               );
            if (module.getAnimka().getValue() > 0.009999999776482582f) {
               activeModules.add(module);
            }
         }
      }

      float targetWidthx = 84.0f;
      float height = 18.0f;
      int visibleModules = 0;

      for (Module module : activeModules) {
         float animValue = module.getAnimka().getValue();
         if (!(animValue <= 0.009999999776482582f)) {
            visibleModules++;
            String line = module.getDisplayName().toLowerCase() + " >> toggle";
            targetWidthx = Math.max(
               targetWidthx, this.issue(14).getWidth(line) + 7.0f
            );
            height += 12.0f * animValue;
         }
      }

      this.widthAnimation.update(targetWidthx);
      float animatedWidth = this.widthAnimation.getValue();
      if (visibleModules == 0) {
         float headerHeight = 18.0f;
         RenderUtil.drawWaveHudHeader(
            context,
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
         String title = "keybinds";
         float titleX = x
            + (animatedWidth - this.issue(15).getWidth(title)) / 2.0f;
         this.issue(15)
            .drawStringWithShadow(
               eventRender.getContext().getMatrices(),
               title,
               titleX,
               y + 5.0f,
               -1
            );
         this.draggable.setWidth(animatedWidth);
         this.draggable.setHeight(headerHeight);
      } else {
         RenderUtil.drawWaveHudPanel(
            context,
            x,
            y,
            animatedWidth,
            height,
            ObsidianComponent_olorUtil.rgba(25, 25, 25, 150),
            15.0F,
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
         String title = "keybinds";
         float titleX = x
            + (animatedWidth - this.issue(15).getWidth(title)) / 2.0f;
         this.issue(15)
            .drawStringWithShadow(
               eventRender.getContext().getMatrices(), title, titleX, y + 5.0f, -1
            );
         float yOffset = 18.0f;

         for (Module modulex : activeModules) {
            float animValue = modulex.getAnimka().getValue();
            if (!(animValue <= 0.009999999776482582f)) {
               ObsidianComponent_343.push();
               ObsidianComponent_343.setFromComponentCoordinates((double)x, (double)y, (double)animatedWidth, (double)height);
               int alpha = (int)(255.0f * animValue);
               int textColor = ObsidianComponent_olorUtil.rgba(
                  255, 255, 255, alpha
               );
               String text = modulex.getDisplayName().toLowerCase() + " >> toggle";
               float textX = x + 5.5f;
               this.issue(14)
                  .draw(context, text, textX, y + yOffset + 2.0f, textColor);
               yOffset += 12.0f * animValue;
               ObsidianComponent_343.unset();
               ObsidianComponent_343.pop();
            }
         }

         this.draggable.setWidth(animatedWidth);
         this.draggable.setHeight(height);
      }
   }

   static {
      String ru = "йцукенгшщзхъфывапролджэячсмитьбюЙЦУКЕНГШЩЗХЪФЫВАПРОЛДЖЭЯЧСМИТЬБЮ";
      String en = "qwertyuiop[]asdfghjkl;'zxcvbnm,.QWERTYUIOP[]ASDFGHJKL;'ZXCVBNM,.";
      int length = Math.min(ru.length(), en.length());

      for (int i = 0; i < length; i++) {
         RU_TO_EN.put(ru.charAt(i), en.charAt(i));
      }
   }
}
