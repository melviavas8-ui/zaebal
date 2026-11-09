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
import java.util.List;
import net.minecraft.client.MinecraftClient;

public class KeyStrokesHudElement extends DraggableHudElement {
   private final MinecraftClient mc = MinecraftClient.getInstance();
   private final List<Long> leftClicks = new ArrayList<>();
   public boolean wasLmbPressed = false;

   public KeyStrokesHudElement(ObsidianComponent_280 draggable) {
      super(draggable);
   }

   @Override
   public void onRender(EventRender2D eventRender) {
      float x = this.draggable.getX();
      float y = this.draggable.getY();
      FontRenderer font = ObsidianComponent_201.getFont("suisse", 15);
      FontRenderer smallFont = ObsidianComponent_201.getFont("suisse", 10);
      float keySize = 20.0f;
      float gap = 2.0f;
      boolean wPressed = this.mc.options.forwardKey.isPressed();
      boolean aPressed = this.mc.options.leftKey.isPressed();
      boolean sPressed = this.mc.options.backKey.isPressed();
      boolean dPressed = this.mc.options.rightKey.isPressed();
      boolean spacePressed = this.mc.options.jumpKey.isPressed();
      boolean lmbPressed = this.mc.options.attackKey.isPressed();
      boolean rmbPressed = this.mc.options.useKey.isPressed();
      long currentTime = System.currentTimeMillis();
      if (lmbPressed && !this.wasLmbPressed) {
         this.leftClicks.add(currentTime);
      }

      this.wasLmbPressed = lmbPressed;
      this.leftClicks
         .removeIf(
            timex -> (currentTime - timex > (1000L))
         );
      int lmbCps = this.leftClicks.size();
      float wX = x + keySize + gap;
      this.drawKey(eventRender, wX, y, keySize, keySize, "W", wPressed, font);
      float aY = y + keySize + gap;
      this.drawKey(eventRender, x, aY, keySize, keySize, "A", aPressed, font);
      float sX = x + keySize + gap;
      float sY = y + keySize + gap;
      this.drawKey(eventRender, sX, sY, keySize, keySize, "S", sPressed, font);
      float dX = x + (keySize + gap) * 2.0f;
      float dY = y + keySize + gap;
      this.drawKey(eventRender, dX, dY, keySize, keySize, "D", dPressed, font);
      float spaceWidth = keySize * 3.0f + gap * 2.0f;
      float spaceHeight = 20.0f;
      float spaceY = y + (keySize + gap) * 2.0f;
      this.drawKey(eventRender, x, spaceY, spaceWidth, spaceHeight, "Space", spacePressed, font);
      float mouseWidth = (spaceWidth - gap) / 2.0f;
      float mouseHeight = 20.0f;
      float lmbY = y + (keySize + gap) * 2.0f + spaceHeight + gap;
      float time = (float)(System.currentTimeMillis() % (2000L))
         / 2000.0f
         * 360.0f;
      int themeColor = ObsidianComponent_olorUtil.getThemeColor((int)time);
      this.drawKeyWithCps(eventRender, x, lmbY, mouseWidth, mouseHeight, "LMB", lmbPressed, font, smallFont, lmbCps, themeColor);
      float rmbX = x + mouseWidth + gap;
      float rmbY = y + (keySize + gap) * 2.0f + spaceHeight + gap;
      this.drawKey(eventRender, rmbX, rmbY, mouseWidth, mouseHeight, "RMB", rmbPressed, font);
      float totalWidth = keySize * 3.0f + gap * 2.0f;
      float totalHeight = keySize * 2.0f + gap + spaceHeight + gap + mouseHeight + gap;
      this.draggable.setWidth(totalWidth);
      this.draggable.setHeight(totalHeight);
      super.onRender(eventRender);
   }
   public void drawKey(EventRender2D eventRender, float x, float y, float width, float height, String text, boolean pressed, Object font) {
      int bgColor = pressed
         ? ObsidianComponent_olorUtil.rgba(180, 180, 180, 200)
         : ObsidianComponent_olorUtil.rgba(
            25, 25, 25, 150
         );
      int textColor = pressed
         ? ObsidianComponent_olorUtil.rgba(
            0,
            0,
            0,
            255
         )
         : ObsidianComponent_olorUtil.rgba(
            255,
            255,
            255,
            255
         );
      RenderUtil.drawKeyStrokeRect(
         eventRender.getContext().getMatrices(), x, y, width, height, 3.0f, bgColor
      );
      FontRenderer f = ObsidianComponent_201.getFont("suisse", 15);
      float textWidth = f.getWidth(text);
      float textHeight = 8.0f;
      float textX = x + (width - textWidth) / 2.0f;
      float textY = y + (height - textHeight) / 2.0f;
      f.draw(
         eventRender.getContext().getMatrices(),
         text,
         textX - 0.5f,
         textY + 2.0f,
         textColor
      );
   }
   public void drawKeyWithCps(
      EventRender2D eventRender, float x, float y, float width, float height, String text, boolean pressed, Object font, Object smallFont, int cps, int themeColor
   ) {
      int bgColor = pressed
         ? ObsidianComponent_olorUtil.rgba(
            180,
            180,
            180,
            200
         )
         : ObsidianComponent_olorUtil.rgba(
            25,
            25,
            25,
            150
         );
      int textColor = pressed
         ? ObsidianComponent_olorUtil.rgba(
            0, 0, 0, 255
         )
         : ObsidianComponent_olorUtil.rgba(
            255,
            255,
            255,
            255
         );
      RenderUtil.drawKeyStrokeRect(
         eventRender.getContext().getMatrices(), x, y, width, height, 3.0f, bgColor
      );
      FontRenderer f = ObsidianComponent_201.getFont("suisse", 15);
      FontRenderer sf = ObsidianComponent_201.getFont("suisse", 12);
      float textWidth = f.getWidth(text);
      float textX = x + (width - textWidth) / 2.0f;
      float textHeight = 8.0f;
      float textY = y + (height - textHeight) / 2.0f;
      f.draw(
         eventRender.getContext().getMatrices(),
         text,
         textX - 0.5f,
         textY + 2.0f,
         textColor
      );
      String cpsText = "cps: " + cps;
      float cpsWidth = sf.getWidth(cpsText);
      float cpsX = x + (width - cpsWidth) / 2.0f;
      float cpsY = textY + 12.0F;
      sf.draw(eventRender.getContext().getMatrices(), cpsText, cpsX, cpsY - 3.0f, themeColor);
   }
}
