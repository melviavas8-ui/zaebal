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
























import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Font;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.joml.Matrix4f;

public class ObsidianComponent_405 extends ObsidianComponent_240 {
   public ObsidianComponent_405(Font font, boolean antiAlias, boolean fractionalMetrics) {
      super(font, antiAlias, fractionalMetrics);
   }

   public int drawGradientString(String text, float x, float y, int topColor, int bottomColor, boolean dropShadow, boolean horizontal) {
      int i;
      if (dropShadow) {
         i = this.renderGradientString(
            text,
            x + 1.0f,
            y + 1.0f,
            topColor,
            bottomColor,
            true,
            horizontal
         );
         i = Math.max(i, this.renderGradientString(text, x, y, topColor, bottomColor, false, horizontal));
      } else {
         i = this.renderGradientString(text, x, y, topColor, bottomColor, false, horizontal);
      }

      return i;
   }
   public int renderGradientString(String text, float x, float y, int startColor, int endColor, boolean dropShadow, boolean horizontal) {
      if (text == null) {
         return 0;
      } else {
         if ((startColor & (-67108864)) == 0) {
            startColor |= -16777216;
         }

         if ((endColor & -67108864) == 0) {
            endColor |= -16777216;
         }

         if (dropShadow) {
            startColor = (startColor & 16579836) >> 2 | startColor & (-16777216);
            endColor = (endColor & 16579836) >> (2) | endColor & (-16777216);
         }

         return this.renderGradientStringAtPos(text, x, y, dropShadow, startColor, endColor, horizontal);
      }
   }
   public int renderGradientStringAtPos(String text, float posX, float posY, boolean shadow, int startColor, int endColor, boolean horizontal) {
      float totalWidth = (float)this.getStringWidth(text);
      float currentCountWidth = 0.0f;
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      Matrix4f matrix = new Matrix4f();

      for (int i = 0; i < text.length(); i++) {
         char c0 = text.charAt(i);
         if (c0 == (32) || c0 == 160) {
            posX += 4.0f;
         } else if (c0 < this.charData.length && this.charData[c0] != null) {
            float charWidth = (float)(this.charData[c0].width - 8 + this.charOffset);
            if (horizontal) {
               float firstMix = currentCountWidth / totalWidth;
               float lastMix = (currentCountWidth + charWidth) / totalWidth;
               int firstColor = this.colorMix(startColor, endColor, firstMix);
               int lastColor = this.colorMix(startColor, endColor, lastMix);
               this.renderGradientChar(c0, posX, posY, firstColor, lastColor, true, matrix);
               currentCountWidth += charWidth;
            } else {
               this.renderGradientChar(c0, posX, posY, startColor, endColor, false, matrix);
            }

            posX += charWidth;
         }
      }

      RenderSystem.setShaderColor(
         1.0f,
         1.0f,
         1.0f,
         1.0f
      );
      return (int)posX;
   }
   public int colorMix(int startColor, int endColor, float mix) {
      float startAlpha = (float)(startColor >> 24 & 255)
         / 255.0f;
      float startRed = (float)(startColor >> 16 & 255)
         / 255.0f;
      float startGreen = (float)(startColor >> 8 & 255)
         / 255.0f;
      float startBlue = (float)(startColor & 255) / 255.0f;
      float endAlpha = (float)(endColor >> 24 & (255))
         / 255.0f;
      float endRed = (float)(endColor >> (16) & (255))
         / 255.0f;
      float endGreen = (float)(endColor >> (8) & 255)
         / 255.0f;
      float endBlue = (float)(endColor & 255) / 255.0f;
      int mixAlpha = (int)(
         ((1.0f - mix) * startAlpha + mix * endAlpha) * 255.0f
      );
      int mixRed = (int)(
         ((1.0f - mix) * startRed + mix * endRed) * 255.0f
      );
      int mixGreen = (int)(
         ((1.0f - mix) * startGreen + mix * endGreen)
            * 255.0f
      );
      int mixBlue = (int)(
         ((1.0f - mix) * startBlue + mix * endBlue)
            * 255.0f
      );
      return mixAlpha << 24
         | mixRed << (16)
         | mixGreen << 8
         | mixBlue;
   }
   public void renderGradientChar(char ch, float posX, float posY, int startColor, int endColor, boolean horizontal, Matrix4f matrix) {
      if (ch < this.charData.length && this.charData[ch] != null) {
         float startAlpha = (float)(startColor >> (24) & 255)
            / 255.0f;
         float startRed = (float)(startColor >> 16 & (255))
            / 255.0f;
         float startGreen = (float)(startColor >> (8) & 255)
            / 255.0f;
         float startBlue = (float)(startColor & 255) / 255.0f;
         float endAlpha = (float)(endColor >> (24) & (255))
            / 255.0f;
         float endRed = (float)(endColor >> (16) & 255)
            / 255.0f;
         float endGreen = (float)(endColor >> 8 & (255))
            / 255.0f;
         float endBlue = (float)(endColor & (255)) / 255.0f;
         ObsidianComponent_425 charData = this.charData[ch];
         float charXPos = (float)charData.storedX;
         float charYPos = (float)charData.storedY;
         int charWidth = charData.width;
         float width = (float)charWidth - 0.009999999776482582f;
         float u0 = charXPos / 512.0f;
         float v0 = charYPos / 512.0f;
         float u1 = (charXPos + width - 1.0f)
            / 512.0f;
         float v1 = (charYPos + 7.989999771118164f)
            / 512.0f;
         RenderSystem.setShaderTexture(0, this.glTextureId);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
         if (horizontal) {
            buffer.vertex(matrix, posX, posY, 0.0f)
               .texture(u0, v0)
               .color(startRed, startGreen, startBlue, startAlpha);
            buffer.vertex(
                  matrix, posX, posY + 7.989999771118164f, 0.0f
               )
               .texture(u0, v1)
               .color(startRed, startGreen, startBlue, startAlpha);
            buffer.vertex(
                  matrix,
                  posX + width - 1.0f,
                  posY + 7.989999771118164f,
                  0.0f
               )
               .texture(u1, v1)
               .color(endRed, endGreen, endBlue, endAlpha);
            buffer.vertex(
                  matrix,
                  posX + width - 1.0f,
                  posY,
                  0.0f
               )
               .texture(u1, v0)
               .color(endRed, endGreen, endBlue, endAlpha);
         } else {
            buffer.vertex(matrix, posX, posY, 0.0f)
               .texture(u0, v0)
               .color(startRed, startGreen, startBlue, startAlpha);
            buffer.vertex(
                  matrix, posX, posY + 7.989999771118164f, 0.0f
               )
               .texture(u0, v1)
               .color(endRed, endGreen, endBlue, endAlpha);
            buffer.vertex(
                  matrix,
                  posX + width - 1.0f,
                  posY + 7.989999771118164f,
                  0.0f
               )
               .texture(u1, v1)
               .color(endRed, endGreen, endBlue, endAlpha);
            buffer.vertex(
                  matrix, posX + width - 1.0f, posY, 0.0f
               )
               .texture(u1, v0)
               .color(startRed, startGreen, startBlue, startAlpha);
         }

         BufferRenderer.drawWithGlobalProgram(buffer.end());
      }
   }
}
