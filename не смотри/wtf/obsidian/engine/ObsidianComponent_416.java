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
























import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import lombok.Generated;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public class ObsidianComponent_416 {
   protected static final int IMG_SIZE = 512;
   protected ObsidianComponent_425[] charData = new ObsidianComponent_425[1104];
   protected Font font;
   protected boolean antiAlias;
   protected boolean fractionalMetrics;
   protected int fontHeight = -1;
   protected int charOffset = 0;
   protected Identifier textureId;
   protected int glTextureId;
   public static int textureCounter = 0;

   public ObsidianComponent_416(Font font, boolean antiAlias, boolean fractionalMetrics) {
      this.font = font;
      this.antiAlias = antiAlias;
      this.fractionalMetrics = fractionalMetrics;
      this.setupTexture(font, antiAlias, fractionalMetrics, this.charData);
   }

   protected void setupTexture(Font font, boolean antiAlias, boolean fractionalMetrics, ObsidianComponent_425[] chars) {
      BufferedImage img = this.generateFontImage(font, antiAlias, fractionalMetrics, chars);

      try {
         NativeImage nativeImage = new NativeImage(img.getWidth(), img.getHeight(), false);

         for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
               int argb = img.getRGB(x, y);
               int a = argb >> 24 & (255);
               int r = argb >> (16) & (255);
               int g = argb >> (8) & (255);
               int b = argb & 255;
               nativeImage.setColorArgb(
                  x, y, a << (24) | r << (16) | g << (8) | b
               );
            }
         }

         NativeImageBackedTexture texture = new NativeImageBackedTexture(nativeImage);
         this.glTextureId = texture.getGlId();
         int var10000 = textureCounter;
         textureCounter += 1;
         String name = "cfont_" + var10000;
         this.textureId = Identifier.of("customfont", name);
         MinecraftClient.getInstance().getTextureManager().registerTexture(this.textureId, texture);
      } catch (Exception var14) {
         var14.printStackTrace();
      }
   }

   protected BufferedImage generateFontImage(Font font, boolean antiAlias, boolean fractionalMetrics, ObsidianComponent_425[] chars) {
      BufferedImage bufferedImage = new BufferedImage(
         512, 512, 2
      );
      Graphics2D g = (Graphics2D)bufferedImage.getGraphics();
      g.setFont(font);
      g.setColor(
         new Color(255, 255, 255, 0)
      );
      g.fillRect(0, 0, 512, 512);
      g.setColor(Color.WHITE);
      g.setRenderingHint(
         RenderingHints.KEY_FRACTIONALMETRICS, fractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF
      );
      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antiAlias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
      FontMetrics fontMetrics = g.getFontMetrics();
      int charHeight = 0;
      int positionX = 0;
      int positionY = 1;

      for (int i = 0; i < chars.length; i++) {
         char ch = (char)i;
         if (ch > (1039) && ch < (1104) || ch < 256) {
            ObsidianComponent_425 charData = new ObsidianComponent_425();
            Rectangle2D dimensions = fontMetrics.getStringBounds(String.valueOf(ch), g);
            charData.width = dimensions.getBounds().width + 8;
            charData.height = dimensions.getBounds().height;
            if (positionX + charData.width >= 512) {
               positionX = 0;
               positionY += charHeight;
               charHeight = 0;
            }

            if (charData.height > charHeight) {
               charHeight = charData.height;
            }

            charData.storedX = positionX;
            charData.storedY = positionY;
            if (charData.height > this.fontHeight) {
               this.fontHeight = charData.height;
            }

            chars[i] = charData;
            g.drawString(String.valueOf(ch), positionX + 2, positionY + fontMetrics.getAscent());
            positionX += charData.width;
         }
      }

      return bufferedImage;
   }

   public void drawChar(ObsidianComponent_425[] chars, char c, float x, float y, Matrix4f matrix, BufferBuilder buffer) {
      try {
         if (chars[c] == null) {
            return;
         }

         this.drawQuad(
            x,
            y,
            (float)chars[c].width,
            (float)chars[c].height,
            (float)chars[c].storedX,
            (float)chars[c].storedY,
            (float)chars[c].width,
            (float)chars[c].height,
            matrix,
            buffer
         );
      } catch (Exception var8) {
      }
   }

   protected void drawQuad(
      float x, float y, float width, float height, float srcX, float srcY, float srcWidth, float srcHeight, Matrix4f matrix, BufferBuilder buffer
   ) {
      float renderSRCX = srcX / 512.0f;
      float renderSRCY = srcY / 512.0f;
      float renderSRCWidth = srcWidth / 512.0f;
      float renderSRCHeight = srcHeight / 512.0f;
      buffer.vertex(matrix, x + width, y, 0.0f).texture(renderSRCX + renderSRCWidth, renderSRCY);
      buffer.vertex(matrix, x, y, 0.0f).texture(renderSRCX, renderSRCY);
      buffer.vertex(matrix, x, y + height, 0.0f).texture(renderSRCX, renderSRCY + renderSRCHeight);
      buffer.vertex(matrix, x, y + height, 0.0f).texture(renderSRCX, renderSRCY + renderSRCHeight);
      buffer.vertex(matrix, x + width, y + height, 0.0f)
         .texture(renderSRCX + renderSRCWidth, renderSRCY + renderSRCHeight);
      buffer.vertex(matrix, x + width, y, 0.0f).texture(renderSRCX + renderSRCWidth, renderSRCY);
   }

   public int getStringHeight(String text) {
      return this.getFontHeight();
   }

   public int getFontHeight() {
      return (this.fontHeight - 8) / 2;
   }

   public int getStringWidth(String text) {
      int width = 0;
      char[] var3 = text.toCharArray();
      int var4 = var3.length;

      for (int var5 = 0; var5 < var4; var5++) {
         char c = var3[var5];
         if (c < this.charData.length && this.charData[c] != null) {
            width += this.charData[c].width - 8 + this.charOffset;
         }
      }

      return width / (2);
   }

   public void setAntiAlias(boolean antiAlias) {
      if (this.antiAlias != antiAlias) {
         this.antiAlias = antiAlias;
         this.setupTexture(this.font, antiAlias, this.fractionalMetrics, this.charData);
      }
   }

   public void setFractionalMetrics(boolean fractionalMetrics) {
      if (this.fractionalMetrics != fractionalMetrics) {
         this.fractionalMetrics = fractionalMetrics;
         this.setupTexture(this.font, this.antiAlias, fractionalMetrics, this.charData);
      }
   }

   public void setFont(Font font) {
      this.font = font;
      this.setupTexture(font, this.antiAlias, this.fractionalMetrics, this.charData);
   }

   @Generated
   public Font getFont() {
      return this.font;
   }

   @Generated
   public boolean isAntiAlias() {
      return this.antiAlias;
   }

   @Generated
   public boolean isFractionalMetrics() {
      return this.fractionalMetrics;
   }

   @Generated
   public Identifier getTextureId() {
      return this.textureId;
   }

   @Generated
   public int getGlTextureId() {
      return this.glTextureId;
   }
}
