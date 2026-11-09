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
import java.awt.Color;
import java.awt.Font;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.joml.Matrix4f;

public class ObsidianComponent_240 extends ObsidianComponent_416 {
   public final int[] colorCode = new int[32];
   protected ObsidianComponent_425[] boldChars = new ObsidianComponent_425[1104];
   protected ObsidianComponent_425[] italicChars = new ObsidianComponent_425[1104];
   protected ObsidianComponent_425[] boldItalicChars = new ObsidianComponent_425[1104];
   protected int texBold;
   protected int texItalic;
   protected int texItalicBold;

   public ObsidianComponent_240(Font font, boolean antiAlias, boolean fractionalMetrics) {
      super(font, antiAlias, fractionalMetrics);
      this.setupBoldItalicIDs();

      for (int index = 0; index < (32); index++) {
         int noClue = (index >> 3 & 1) * (85);
         int red = (index >> 2 & (1)) * (170) + noClue;
         int green = (index >> 1 & (1)) * (170) + noClue;
         int blue = (index & (1)) * 170 + noClue;
         if (index == (6)) {
            red += 85;
         }

         if (index >= 16) {
            red /= 4;
            green /= 4;
            blue /= 4;
         }

         this.colorCode[index] = (red & 255) << 16
            | (green & 255) << 8
            | blue & (255);
      }
   }

   public float drawStringWithShadow(String text, double x, double y, int color) {
      float shadowWidth = this.drawString(
         text,
         x + 0.5,
         y + 0.5,
         color,
         true
      );
      return Math.max(shadowWidth, this.drawString(text, x, y, color, false));
   }

   public float drawGradientString(String text, float x, float y, int topColor, int bottomColor) {
      if (text == null) {
         return 0.0f;
      } else {
         x -= 1.0f;
         if ((topColor & -67108864) == 0) {
            topColor |= -16777216;
         }

         if ((bottomColor & -67108864) == 0) {
            bottomColor |= -16777216;
         }

         float topAlpha = (float)(topColor >> 24 & 255)
            / 255.0f;
         float topRed = (float)(topColor >> (16) & 255)
            / 255.0f;
         float topGreen = (float)(topColor >> (8) & (255))
            / 255.0f;
         float topBlue = (float)(topColor & 255) / 255.0f;
         float botAlpha = (float)(bottomColor >> 24 & (255))
            / 255.0f;
         float botRed = (float)(bottomColor >> (16) & 255)
            / 255.0f;
         float botGreen = (float)(bottomColor >> (8) & (255))
            / 255.0f;
         float botBlue = (float)(bottomColor & 255) / 255.0f;
         double posX = (double)x * 2.0;
         double posY = ((double)y - 3.0)
            * 2.0;
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.setShaderColor(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
         Matrix4f matrix = new Matrix4f();
         matrix.scale(
            0.5f,
            0.5f,
            0.5f
         );
         ObsidianComponent_425[] currentData = this.charData;
         int size = text.length();

         for (int i = 0; i < size; i++) {
            char character = text.charAt(i);
            if (character < currentData.length && currentData[character] != null) {
               RenderSystem.setShaderTexture(0, this.glTextureId);
               RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
               Tessellator tessellator = Tessellator.getInstance();
               BufferBuilder buffer = tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
               ObsidianComponent_425 cd = currentData[character];
               float charXPos = (float)cd.storedX;
               float charYPos = (float)cd.storedY;
               float width = (float)cd.width;
               float height = (float)cd.height;
               float u0 = charXPos / 512.0f;
               float v0 = charYPos / 512.0f;
               float u1 = (charXPos + width) / 512.0f;
               float v1 = (charYPos + height) / 512.0f;
               buffer.vertex(matrix, (float)posX, (float)posY, 0.0f)
                  .texture(u0, v0)
                  .color(topRed, topGreen, topBlue, topAlpha);
               buffer.vertex(matrix, (float)posX, (float)posY + height, 0.0f)
                  .texture(u0, v1)
                  .color(botRed, botGreen, botBlue, botAlpha);
               buffer.vertex(matrix, (float)posX + width, (float)posY + height, 0.0f)
                  .texture(u1, v1)
                  .color(botRed, botGreen, botBlue, botAlpha);
               buffer.vertex(matrix, (float)posX + width, (float)posY, 0.0f)
                  .texture(u1, v0)
                  .color(topRed, topGreen, topBlue, topAlpha);
               BufferRenderer.drawWithGlobalProgram(buffer.end());
               posX += (double)(cd.width - 8 + this.charOffset);
            } else if (character == 32 || character == (160)) {
               posX += 8.0;
            }
         }

         RenderSystem.setShaderColor(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
         return (float)posX / 2.0f;
      }
   }

   public float drawGradientStringHorizontal(String text, float x, float y, int leftColor, int rightColor) {
      if (text == null) {
         return 0.0f;
      } else {
         x--;
         if ((leftColor & -67108864) == 0) {
            leftColor |= -16777216;
         }

         if ((rightColor & (-67108864)) == 0) {
            rightColor |= -16777216;
         }

         double posX = (double)x * 2.0;
         double posY = ((double)y - 3.0)
            * 2.0;
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.setShaderColor(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
         Matrix4f matrix = new Matrix4f();
         matrix.scale(
            0.5f,
            0.5f,
            0.5f
         );
         ObsidianComponent_425[] currentData = this.charData;
         int size = text.length();
         float totalWidth = (float)this.getStringWidth(text) * 2.0F;
         float currentWidth = 0.0f;

         for (int i = 0; i < size; i++) {
            char character = text.charAt(i);
            if (character < currentData.length && currentData[character] != null) {
               RenderSystem.setShaderTexture(0, this.glTextureId);
               RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
               Tessellator tessellator = Tessellator.getInstance();
               BufferBuilder buffer = tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
               ObsidianComponent_425 cd = currentData[character];
               float charXPos = (float)cd.storedX;
               float charYPos = (float)cd.storedY;
               float width = (float)cd.width;
               float height = (float)cd.height;
               float charWidth = (float)(cd.width - (8) + this.charOffset);
               float u0 = charXPos / 512.0f;
               float v0 = charYPos / 512.0f;
               float u1 = (charXPos + width) / 512.0f;
               float v1 = (charYPos + height) / 512.0f;
               float firstMix = totalWidth <= 0.0f
                  ? 0.0f
                  : currentWidth / totalWidth;
               float lastMix = totalWidth <= 0.0f
                  ? 1.0f
                  : (currentWidth + charWidth) / totalWidth;
               int firstColor = this.colorMix(leftColor, rightColor, firstMix);
               int lastColor = this.colorMix(leftColor, rightColor, lastMix);
               float firstAlpha = (float)(firstColor >> (24) & 255)
                  / 255.0f;
               float firstRed = (float)(firstColor >> (16) & 255)
                  / 255.0f;
               float firstGreen = (float)(firstColor >> (8) & (255))
                  / 255.0f;
               float firstBlue = (float)(firstColor & 255)
                  / 255.0f;
               float lastAlpha = (float)(lastColor >> (24) & 255)
                  / 255.0f;
               float lastRed = (float)(lastColor >> (16) & (255))
                  / 255.0f;
               float lastGreen = (float)(lastColor >> (8) & 255)
                  / 255.0f;
               float lastBlue = (float)(lastColor & (255)) / 255.0f;
               buffer.vertex(matrix, (float)posX, (float)posY, 0.0f)
                  .texture(u0, v0)
                  .color(firstRed, firstGreen, firstBlue, firstAlpha);
               buffer.vertex(matrix, (float)posX, (float)posY + height, 0.0f)
                  .texture(u0, v1)
                  .color(firstRed, firstGreen, firstBlue, firstAlpha);
               buffer.vertex(matrix, (float)posX + width, (float)posY + height, 0.0f)
                  .texture(u1, v1)
                  .color(lastRed, lastGreen, lastBlue, lastAlpha);
               buffer.vertex(matrix, (float)posX + width, (float)posY, 0.0f)
                  .texture(u1, v0)
                  .color(lastRed, lastGreen, lastBlue, lastAlpha);
               BufferRenderer.drawWithGlobalProgram(buffer.end());
               posX += (double)charWidth;
               currentWidth += charWidth;
            } else if (character == (32) || character == 160) {
               posX += 8.0;
               currentWidth += 8.0f;
            }
         }

         RenderSystem.setShaderColor(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
         return (float)posX / 2.0f;
      }
   }
   public int colorMix(int startColor, int endColor, float mix) {
      float startAlpha = (float)(startColor >> 24 & (255))
         / 255.0f;
      float startRed = (float)(startColor >> 16 & 255)
         / 255.0f;
      float startGreen = (float)(startColor >> (8) & 255)
         / 255.0f;
      float startBlue = (float)(startColor & (255)) / 255.0f;
      float endAlpha = (float)(endColor >> 24 & 255)
         / 255.0f;
      float endRed = (float)(endColor >> 16 & (255))
         / 255.0f;
      float endGreen = (float)(endColor >> 8 & (255))
         / 255.0f;
      float endBlue = (float)(endColor & (255)) / 255.0f;
      int mixAlpha = (int)(
         ((1.0f - mix) * startAlpha + mix * endAlpha)
            * 255.0f
      );
      int mixRed = (int)(
         ((1.0f - mix) * startRed + mix * endRed)
            * 255.0f
      );
      int mixGreen = (int)(
         ((1.0f - mix) * startGreen + mix * endGreen) * 255.0f
      );
      int mixBlue = (int)(
         ((1.0f - mix) * startBlue + mix * endBlue)
            * 255.0f
      );
      return mixAlpha << 24
         | mixRed << (16)
         | mixGreen << (8)
         | mixBlue;
   }

   public float drawString(String text, float x, float y, int color) {
      return this.drawString(text, (double)x, (double)y, color, false);
   }

   public float drawCenteredString(String text, float x, float y, int color) {
      return this.drawString(text, x - (float)this.getStringWidth(text) / 2.0f, y, color);
   }

   public float drawCenteredStringWithShadow(String text, float x, float y, int color) {
      return this.drawStringWithShadow(
         text, (double)(x - (float)this.getStringWidth(text) / 2.0f), (double)y, color
      );
   }

   public float drawString(String text, double x, double y, int color, boolean shadow) {
      x -= 1.0;
      if (text == null) {
         return 0.0f;
      } else {
         if (color == (553648127)) {
            color = 16777215;
         }

         if ((color & -67108864) == 0) {
            color |= -16777216;
         }

         if (shadow) {
            color = (color & 16579836) >> (2)
               | color
                  & new Color(20, 20, 20, 200)
                     .getRGB();
         }

         ObsidianComponent_425[] currentData = this.charData;
         float alpha = (float)(color >> (24) & 255)
            / 255.0f;
         boolean bold = false;
         boolean italic = false;
         boolean strikethrough = false;
         boolean underline = false;
         x *= 2.0;
         y = (y - 3.0)
            * 2.0;
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.setShaderColor(
            (float)(color >> 16 & (255)) / 255.0f,
            (float)(color >> 8 & (255)) / 255.0F,
            (float)(color & (255)) / 255.0f,
            alpha
         );
         Matrix4f matrix = new Matrix4f();
         matrix.scale(
            0.5f,
            0.5f,
            0.5f
         );
         int size = text.length();
         int currentTexture = this.glTextureId;
         RenderSystem.setShaderTexture(0, currentTexture);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);

         for (int i = 0; i < size; i++) {
            char character = text.charAt(i);
            if (String.valueOf(character).equals("§") && i < size - 1) {
               int colorIndex = 21;

               try {
                  colorIndex = "0123456789abcdefklmnor".indexOf(text.charAt(i + 1));
               } catch (Exception var21) {
                  var21.printStackTrace();
               }

               if (colorIndex >= 16) {
                  if (colorIndex == 17) {
                     bold = true;
                     if (italic) {
                        currentTexture = this.texItalicBold;
                        currentData = this.boldItalicChars;
                     } else {
                        currentTexture = this.texBold;
                        currentData = this.boldChars;
                     }
                  } else if (colorIndex == 18) {
                     strikethrough = true;
                  } else if (colorIndex == 19) {
                     underline = true;
                  } else if (colorIndex == 20) {
                     italic = true;
                     if (bold) {
                        currentTexture = this.texItalicBold;
                        currentData = this.boldItalicChars;
                     } else {
                        currentTexture = this.texItalic;
                        currentData = this.italicChars;
                     }
                  } else if (colorIndex == (21)) {
                     bold = false;
                     italic = false;
                     underline = false;
                     strikethrough = false;
                     RenderSystem.setShaderColor(
                        (float)(color >> (16) & (255))
                           / 255.0f,
                        (float)(color >> (8) & (255))
                           / 255.0f,
                        (float)(color & (255)) / 255.0f,
                        alpha
                     );
                     currentTexture = this.glTextureId;
                     currentData = this.charData;
                  }
               } else {
                  bold = false;
                  italic = false;
                  underline = false;
                  strikethrough = false;
                  currentTexture = this.glTextureId;
                  currentData = this.charData;
                  if (colorIndex < 0 || colorIndex > 15) {
                     colorIndex = 15;
                  }

                  if (shadow) {
                     colorIndex += 16;
                  }

                  int colorcode = this.colorCode[colorIndex];
                  RenderSystem.setShaderColor(
                     (float)(colorcode >> (16) & (255))
                        / 255.0f,
                     (float)(colorcode >> 8 & 255)
                        / 255.0f,
                     (float)(colorcode & 255) / 255.0f,
                     alpha
                  );
               }

               i++;
            } else if (character < currentData.length && currentData[character] != null) {
               RenderSystem.setShaderTexture(0, currentTexture);
               Tessellator tessellator = Tessellator.getInstance();
               BufferBuilder buffer = tessellator.begin(DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE);
               this.drawChar(currentData, character, (float)x, (float)y, matrix, buffer);
               BufferRenderer.drawWithGlobalProgram(buffer.end());
               if (strikethrough) {
                  this.drawLine(
                     x,
                     y + (double)((float)currentData[character].height / 2.0f),
                     x + (double)currentData[character].width - 8.0,
                     y + (double)((float)currentData[character].height / 2.0f),
                     1.0f,
                     matrix
                  );
               }

               if (underline) {
                  this.drawLine(
                     x,
                     y + (double)currentData[character].height - 2.0,
                     x + (double)currentData[character].width - 8.0,
                     y + (double)currentData[character].height - 2.0,
                     1.0f,
                     matrix
                  );
               }

               x += (double)(currentData[character].width - 8 + this.charOffset);
            }
         }

         RenderSystem.setShaderColor(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
         return (float)x / 2.0f;
      }
   }

   @Override
   public int getStringWidth(String text) {
      int width = 0;
      ObsidianComponent_425[] currentData = this.charData;
      boolean bold = false;
      boolean italic = false;
      int size = text.length();

      for (int i = 0; i < size; i++) {
         char character = text.charAt(i);
         if (String.valueOf(character).equals("§") && i < size - (1)) {
            int colorIndex = "0123456789abcdefklmnor".indexOf(text.charAt(i + (1)));
            if (colorIndex < 16) {
               bold = false;
               italic = false;
            } else if (colorIndex == (17)) {
               bold = true;
               currentData = italic ? this.boldItalicChars : this.boldChars;
            } else if (colorIndex == 20) {
               italic = true;
               currentData = bold ? this.boldItalicChars : this.italicChars;
            } else if (colorIndex == 21) {
               bold = false;
               italic = false;
               currentData = this.charData;
            }

            i++;
         } else if (character < currentData.length && currentData[character] != null) {
            width += currentData[character].width - (8) + this.charOffset;
         }
      }

      return width / 2;
   }

   @Override
   public void setFont(Font font) {
      super.setFont(font);
      this.setupBoldItalicIDs();
   }

   @Override
   public void setAntiAlias(boolean antiAlias) {
      super.setAntiAlias(antiAlias);
      this.setupBoldItalicIDs();
   }

   @Override
   public void setFractionalMetrics(boolean fractionalMetrics) {
      super.setFractionalMetrics(fractionalMetrics);
      this.setupBoldItalicIDs();
   }
   public void setupBoldItalicIDs() {
      ObsidianComponent_416 boldFont = new ObsidianComponent_416(this.font.deriveFont(1), this.antiAlias, this.fractionalMetrics);
      this.texBold = boldFont.getGlTextureId();
      this.boldChars = boldFont.charData;
      ObsidianComponent_416 italicFont = new ObsidianComponent_416(this.font.deriveFont(2), this.antiAlias, this.fractionalMetrics);
      this.texItalic = italicFont.getGlTextureId();
      this.italicChars = italicFont.charData;
      ObsidianComponent_416 boldItalicFont = new ObsidianComponent_416(this.font.deriveFont(3), this.antiAlias, this.fractionalMetrics);
      this.texItalicBold = boldItalicFont.getGlTextureId();
      this.boldItalicChars = boldItalicFont.charData;
   }
   public void drawLine(double x, double y, double x1, double y1, float width, Matrix4f matrix) {
      RenderSystem.setShader(ShaderProgramKeys.POSITION);
      RenderSystem.lineWidth(width);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.begin(DrawMode.LINES, VertexFormats.POSITION);
      buffer.vertex(matrix, (float)x, (float)y, 0.0f);
      buffer.vertex(matrix, (float)x1, (float)y1, 0.0f);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
   }

   public void drawStringWithOutline(String text, double x, double y, int color) {
      this.drawString(
         text,
         x - 0.5,
         y,
         Color.BLACK.getRGB(),
         false
      );
      this.drawString(
         text,
         x + 0.5,
         y,
         Color.BLACK.getRGB(),
         false
      );
      this.drawString(
         text,
         x,
         y - 0.5,
         Color.BLACK.getRGB(),
         false
      );
      this.drawString(
         text,
         x,
         y + 0.5,
         Color.BLACK.getRGB(),
         false
      );
      this.drawString(text, x, y, color, false);
   }

   public void drawCenteredStringWithOutline(String text, float x, float y, int color) {
      this.drawCenteredString(text, x - 0.5f, y, Color.BLACK.getRGB());
      this.drawCenteredString(text, x + 0.5f, y, Color.BLACK.getRGB());
      this.drawCenteredString(text, x, y - 0.5f, Color.BLACK.getRGB());
      this.drawCenteredString(text, x, y + 0.5f, Color.BLACK.getRGB());
      this.drawCenteredString(text, x, y, color);
   }
}
