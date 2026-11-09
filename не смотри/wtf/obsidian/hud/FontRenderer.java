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
























import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class FontRenderer implements IInstance {
   public static final char FORMATTING_CODE_PREFIX = '§';
   public final ObsidianComponent_81 font;
   public final float size;

   public FontRenderer(ObsidianComponent_81 font, float size) {
      this.font = font;
      this.size = size;
   }

   public FontRenderer(String name, float size) {
      this.font = ObsidianComponent_81.builder().atlas(name).data(name).build();
      this.size = size;
   }

   public void drawString(MatrixStack matrixStack, String text, double x, double y, int color) {
      this.draw(matrixStack, text, (float)x, (float)y, color);
   }

   public void drawString(MatrixStack matrixStack, String text, float x, float y, int color) {
      this.draw(matrixStack, text, x, y, color);
   }

   public void drawString(String text, float x, float y, int color) {
      MatrixStack stack = new MatrixStack();
      this.draw(stack, text, x, y, color);
   }

   public void drawCenteredString(MatrixStack matrixStack, String text, double x, double y, int color) {
      this.draw(
         matrixStack,
         text,
         (float)(x - (double)this.getStringWidth(text) / 2.0),
         (float)y,
         color
      );
   }

   public void drawCenteredString(MatrixStack matrixStack, String text, float x, float y, int color) {
      this.draw(matrixStack, text, x - this.getStringWidth(text) / 2.0f, y, color);
   }

   public void drawRight(MatrixStack matrixStack, String text, double x, double y, int color) {
      this.draw(matrixStack, text, (float)(x - (double)this.getStringWidth(text)), (float)y, color);
   }

   public void drawRight(MatrixStack matrixStack, String text, float x, float y, int color) {
      this.draw(matrixStack, text, x - this.getStringWidth(text), y, color);
   }

   public void draw(MatrixStack stack, String text, double x, double y, int color) {
      this.draw(stack, text, (float)x, (float)y, color);
   }

   public void draw(MatrixStack stack, String text, float x, float y, int color) {
      if (text != null && !text.isEmpty()) {
         float localSize = this.size * 0.5f;
         if (this.hasDrawableGlyphs(text, localSize)) {
            y -= 1.5f;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            ShaderProgram shader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.fontsMsdf);
            if (shader != null) {
               this.setupShaderUniforms(shader, color);
               RenderSystem.setShaderTexture(0, this.font.getTextureId());
               this.font.setFiltered();
               Matrix4f matrix = stack.peek().getPositionMatrix();
               BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
               this.font
                  .applyGlyphs(
                     matrix,
                     buffer,
                     localSize,
                     text,
                     0.0f,
                     x,
                     y + this.font.getBaselineHeight() * localSize,
                     0.0f,
                     255,
                     255,
                     255,
                     255
                  );
               RenderSystem.setShader(ObsidianComponent_257.fontsMsdf);
               BufferRenderer.drawWithGlobalProgram(buffer.end());
               RenderSystem.setShaderTexture(0, 0);
               RenderSystem.enableCull();
               RenderSystem.disableBlend();
            }
         }
      }
   }

   public void drawGradientStringHorizontal(String text, float x, float y, int leftColor, int rightColor) {
      MatrixStack stack = new MatrixStack();
      this.drawGradientStringHorizontal(stack, text, x, y, leftColor, rightColor);
   }

   public void drawGradientStringHorizontal(MatrixStack stack, String text, float x, float y, int leftColor, int rightColor) {
      if (text != null && !text.isEmpty()) {
         float totalWidth = this.getStringWidth(text);
         float currentX = x;

         for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String charStr = String.valueOf(c);
            float charWidth = this.getStringWidth(charStr);
            float progress = totalWidth > 0.0f
               ? (currentX - x) / totalWidth
               : 0.0f;
            int color = interpolateColor(leftColor, rightColor, progress);
            this.draw(stack, charStr, currentX, y, color);
            currentX += charWidth;
         }
      }
   }

   public void drawGradientStringHorizontal(
      MatrixStack stack, String text, float x, float y, int topLeftColor, int topRightColor, int bottomLeftColor, int bottomRightColor
   ) {
      if (text != null && !text.isEmpty()) {
         float totalWidth = this.getStringWidth(text);
         float currentX = x;

         for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String charStr = String.valueOf(c);
            float charWidth = this.getStringWidth(charStr);
            float progress = totalWidth > 0.0f
               ? (currentX - x) / totalWidth
               : 0.0f;
            int topColor = interpolateColor(topLeftColor, topRightColor, progress);
            int bottomColor = interpolateColor(bottomLeftColor, bottomRightColor, progress);
            int color = interpolateColor(topColor, bottomColor, 0.5f);
            this.draw(stack, charStr, currentX, y, color);
            currentX += charWidth;
         }
      }
   }

   public void drawGradientStringVertical(MatrixStack stack, String text, float x, float y, int topColor, int bottomColor) {
      if (text != null && !text.isEmpty()) {
         int color = interpolateColor(topColor, bottomColor, 0.5f);
         this.draw(stack, text, x, y, color);
      }
   }

   public void drawStringWithFade(MatrixStack stack, String text, float x, float y, float maxWidth, int color) {
      if (text != null && !text.isEmpty()) {
         if (!(maxWidth <= 1.0f)) {
            int originalAlpha = color >>> 24 & (255);
            if (originalAlpha == 0) {
               originalAlpha = 255;
            }

            if (originalAlpha > (4)) {
               float localSize = this.size * 0.5f;
               y -= 1.5f;
               RenderSystem.enableBlend();
               RenderSystem.defaultBlendFunc();
               RenderSystem.disableCull();
               ShaderProgram shader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.fontsMsdf);
               if (shader != null) {
                  GlUniform textureSizeUniform = shader.getUniform("TextureSize");
                  GlUniform rangeUniform = shader.getUniform("Range");
                  GlUniform thicknessUniform = shader.getUniform("Thickness");
                  GlUniform edgeStrengthUniform = shader.getUniform("EdgeStrength");
                  GlUniform colorUniform = shader.getUniform("Color");
                  GlUniform outlineUniform = shader.getUniform("Outline");
                  GlUniform outlineThicknessUniform = shader.getUniform("OutlineThickness");
                  GlUniform outlineColorUniform = shader.getUniform("OutlineColor");
                  if (textureSizeUniform != null) {
                     textureSizeUniform.set(this.font.getAtlasWidth(), this.font.getAtlasHeight());
                  }

                  if (rangeUniform != null) {
                     rangeUniform.set(this.font.getRange());
                  }

                  if (thicknessUniform != null) {
                     thicknessUniform.set(0.0F);
                  }

                  if (edgeStrengthUniform != null) {
                     edgeStrengthUniform.set(0.5f);
                  }

                  if (outlineUniform != null) {
                     outlineUniform.set(0);
                  }

                  if (outlineThicknessUniform != null) {
                     outlineThicknessUniform.set(0.0f);
                  }

                  if (outlineColorUniform != null) {
                     outlineColorUniform.set(
                        1.0f,
                        1.0f,
                        1.0f,
                        1.0f
                     );
                  }

                  RenderSystem.setShaderTexture(0, this.font.getTextureId());
                  this.font.setFiltered();
                  float currentX = x;
                  float fadeZoneWidth = 25.0f;
                  float fadeStartX = x + maxWidth - fadeZoneWidth;

                  for (int i = 0; i < text.length(); i++) {
                     String charStr = String.valueOf(text.charAt(i));
                     float charWidth = this.getStringWidth(charStr);
                     if (currentX > x + maxWidth && i > 0) {
                        break;
                     }

                     int finalColor = color;
                     if (currentX > fadeStartX) {
                        float progressIntoFade = (currentX - fadeStartX) / fadeZoneWidth;
                        progressIntoFade = Math.max(
                           0.0f,
                           Math.min(1.0f, progressIntoFade)
                        );
                        float fadeFactor = (float)Math.cos(
                           (double)progressIntoFade
                              * 3.141592653589793
                              / 2.0
                        );
                        int newAlpha = (int)((float)originalAlpha * fadeFactor);
                        finalColor = color & (16777215) | newAlpha << 24;
                     }

                     if ((finalColor >>> (24) & (255)) > (4)) {
                        float[] rgba = this.extractRgba(finalColor);
                        if (colorUniform != null) {
                           colorUniform.set(
                              rgba[0],
                              rgba[1],
                              rgba[2],
                              rgba[3]
                           );
                        }

                        Matrix4f matrix = stack.peek().getPositionMatrix();
                        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                        this.font
                           .applyGlyphs(
                              matrix,
                              buffer,
                              localSize,
                              charStr,
                              0.0f,
                              currentX,
                              y + this.font.getBaselineHeight() * localSize,
                              0.0f,
                              255,
                              255,
                              255,
                              255
                           );
                        RenderSystem.setShader(ObsidianComponent_257.fontsMsdf);
                        BufferRenderer.drawWithGlobalProgram(buffer.end());
                     }

                     currentX += charWidth;
                  }

                  RenderSystem.setShaderTexture(0, 0);
                  RenderSystem.enableCull();
                  RenderSystem.disableBlend();
               }
            }
         }
      }
   }

   public void drawAnimatedGradientStringHorizontal(String text, float x, float y, int leftColor, int rightColor, float speed) {
      MatrixStack stack = new MatrixStack();
      this.drawAnimatedGradientStringHorizontal(
         stack, text, x, y, leftColor, rightColor, speed, 1.149999976158142f
      );
   }

   public void drawAnimatedGradientStringHorizontal(MatrixStack stack, String text, float x, float y, int leftColor, int rightColor, float speed) {
      this.drawAnimatedGradientStringHorizontal(stack, text, x, y, leftColor, rightColor, speed, 1.149999976158142f);
   }

   public void drawAnimatedGradientStringHorizontal(String text, float x, float y, int leftColor, int rightColor, float speed, float waveScale) {
      MatrixStack stack = new MatrixStack();
      this.drawAnimatedGradientStringHorizontal(stack, text, x, y, leftColor, rightColor, speed, waveScale);
   }

   public void drawAnimatedGradientStringHorizontal(
      MatrixStack stack, String text, float x, float y, int leftColor, int rightColor, float speed, float waveScale
   ) {
      if (text != null && !text.isEmpty()) {
         float totalWidth = this.getStringWidth(text);
         float currentX = x;
         double timeOffset = (double)System.currentTimeMillis()
            * 0.001
            * (double)Math.max(0.009999999776482582f, speed)
            % 2.0;
         float safeWaveScale = Math.max(0.009999999776482582f, waveScale);

         for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String charStr = String.valueOf(c);
            float charWidth = this.getStringWidth(charStr);
            float baseProgress = totalWidth > 0.0f
               ? (currentX - x) / totalWidth
               : 0.0f;
            float animatedProgress = pingPong01(baseProgress * safeWaveScale + (float)timeOffset);
            int color = interpolateColor(leftColor, rightColor, animatedProgress);
            this.draw(stack, charStr, currentX, y, color);
            currentX += charWidth;
         }
      }
   }

   public void drawStringWithOutline(MatrixStack stack, String text, float x, float y, int color, int outlineColor) {
      if (text != null && !text.isEmpty()) {
         this.draw(stack, text, x - 1.0f, y, outlineColor);
         this.draw(stack, text, x + 1.0f, y, outlineColor);
         this.draw(stack, text, x, y - 1.0f, outlineColor);
         this.draw(stack, text, x, y + 1.0f, outlineColor);
         this.draw(stack, text, x, y, color);
      }
   }

   public void drawStringWithShadow(MatrixStack stack, String text, float x, float y, int color) {
      if (text != null && !text.isEmpty()) {
         int shadowColor = 1426063360;
         this.draw(
            stack,
            text,
            x + 1.0f,
            y + 1.0f,
            shadowColor
         );
         this.draw(stack, text, x, y, color);
      }
   }

   public void drawParagraph(MatrixStack stack, String text, double x, double y, int defaultColor) {
      this.drawParagraph(stack, text, (float)x, (float)y, defaultColor);
   }

   public void drawParagraph(MatrixStack stack, String text, float x, float y, int defaultColor) {
      if (text != null && !text.isEmpty()) {
         float localSize = this.size * 0.5f;
         y -= 1.5f;
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableCull();
         ShaderProgram shader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.fontsMsdf);
         if (shader != null) {
            GlUniform textureSizeUniform = shader.getUniform("TextureSize");
            GlUniform rangeUniform = shader.getUniform("Range");
            GlUniform thicknessUniform = shader.getUniform("Thickness");
            GlUniform edgeStrengthUniform = shader.getUniform("EdgeStrength");
            GlUniform colorUniform = shader.getUniform("Color");
            if (textureSizeUniform != null) {
               textureSizeUniform.set(this.font.getAtlasWidth(), this.font.getAtlasHeight());
            }

            if (rangeUniform != null) {
               rangeUniform.set(this.font.getRange());
            }

            if (thicknessUniform != null) {
               thicknessUniform.set(0.0f);
            }

            if (edgeStrengthUniform != null) {
               edgeStrengthUniform.set(0.5f);
            }

            RenderSystem.setShaderTexture(0, this.font.getTextureId());
            this.font.setFiltered();
            float currentX = x;
            int currentColor = defaultColor;
            StringBuilder segment = new StringBuilder();

            for (int i = 0; i < text.length(); i++) {
               char c = text.charAt(i);
               if (c == 167 && i + (1) < text.length()) {
                  if (!segment.isEmpty()) {
                     this.drawSegment(stack, colorUniform, segment.toString(), currentX, y + this.font.getBaselineHeight() * localSize, localSize, currentColor);
                     currentX += this.getStringWidth(segment.toString());
                     segment.setLength(0);
                  }

                  char code = text.charAt(i + 1);
                  int newColor = this.getColorFromCode(code, defaultColor);
                  if (newColor != -1) {
                     currentColor = newColor;
                  }

                  i++;
               } else {
                  segment.append(c);
               }
            }

            if (!segment.isEmpty()) {
               this.drawSegment(stack, colorUniform, segment.toString(), currentX, y + this.font.getBaselineHeight() * localSize, localSize, currentColor);
            }

            RenderSystem.setShaderTexture(0, 0);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
         }
      }
   }
   public void drawSegment(MatrixStack stack, GlUniform colorUniform, String text, float x, float y, float size, int color) {
      if (this.hasDrawableGlyphs(text, size)) {
         float[] rgba = this.extractRgba(color);
         if (colorUniform != null) {
            colorUniform.set(
               rgba[0],
               rgba[1],
               rgba[2],
               rgba[3]
            );
         }

         Matrix4f matrix = stack.peek().getPositionMatrix();
         BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
         this.font
            .applyGlyphs(
               matrix,
               buffer,
               size,
               text,
               0.0f,
               x,
               y,
               0.0f,
               255,
               255,
               255,
               255
            );
         RenderSystem.setShader(ObsidianComponent_257.fontsMsdf);
         BufferRenderer.drawWithGlobalProgram(buffer.end());
      }
   }
   public boolean hasDrawableGlyphs(String text, float renderSize) {
      return (text != null && !text.isEmpty() && this.font.getWidth(text, renderSize) > 0.0f);
   }
   public void setupShaderUniforms(ShaderProgram shader, int color) {
      GlUniform textureSizeUniform = shader.getUniform("TextureSize");
      GlUniform rangeUniform = shader.getUniform("Range");
      GlUniform thicknessUniform = shader.getUniform("Thickness");
      GlUniform edgeStrengthUniform = shader.getUniform("EdgeStrength");
      GlUniform colorUniform = shader.getUniform("Color");
      GlUniform outlineUniform = shader.getUniform("Outline");
      GlUniform outlineThicknessUniform = shader.getUniform("OutlineThickness");
      GlUniform outlineColorUniform = shader.getUniform("OutlineColor");
      if (textureSizeUniform != null) {
         textureSizeUniform.set(this.font.getAtlasWidth(), this.font.getAtlasHeight());
      }

      if (rangeUniform != null) {
         rangeUniform.set(this.font.getRange());
      }

      if (thicknessUniform != null) {
         thicknessUniform.set(0.0f);
      }

      if (edgeStrengthUniform != null) {
         edgeStrengthUniform.set(0.5f);
      }

      if (outlineUniform != null) {
         outlineUniform.set(0);
      }

      if (outlineThicknessUniform != null) {
         outlineThicknessUniform.set(0.0f);
      }

      if (outlineColorUniform != null) {
         outlineColorUniform.set(
            0.0f,
            0.0f,
            0.0f,
            1.0f
         );
      }

      float[] rgba = this.extractRgba(color);
      if (colorUniform != null) {
         colorUniform.set(
            rgba[0],
            rgba[1],
            rgba[2],
            rgba[3]
         );
      }
   }
   public int getColorFromCode(char code, int defaultColor) {
      int alpha = defaultColor >> 24 & 255;
      if (alpha == 0) {
         alpha = 255;
      }
      return switch (code) {
         case '0' -> alpha << (24) | 0;
         case '1' -> alpha << (24) | 170;
         case '2' -> alpha << 24 | 43520;
         case '3' -> alpha << 24 | 43690;
         case '4' -> alpha << 24 | 11141120;
         case '5' -> alpha << (24) | 11141290;
         case '6' -> alpha << 24 | 16755200;
         case '7' -> alpha << (24) | 11184810;
         case '8' -> alpha << (24) | 5592405;
         case '9' -> alpha << (24) | 5592575;
         default -> -1;
         case 'A', 'a' -> alpha << 24 | 5635925;
         case 'B', 'b' -> alpha << (24) | 5636095;
         case 'C', 'c' -> alpha << 24 | 16733525;
         case 'D', 'd' -> alpha << 24 | 16733695;
         case 'E', 'e' -> alpha << 24 | 16777045;
         case 'F', 'f' -> alpha << 24 | 16777215;
         case 'R', 'r' -> defaultColor;
      };
   }
   public float[] extractRgba(int color) {
      int a = color >> (24) & (255);
      int r = color >> (16) & (255);
      int g = color >> (8) & (255);
      int b = color & (255);
      if (a == 0) {
         a = 255;
      }

      float[] var10000 = new float[4];
      var10000[0] = (float)r / 255.0f;
      var10000[1] = (float)g / 255.0f;
      var10000[2] = (float)b / 255.0f;
      var10000[3] = (float)a / 255.0f;
      return var10000;
   }

   public static int interpolateColor(int color1, int color2, float progress) {
      progress = Math.max(
         0.0f,
         Math.min(1.0f, progress)
      );
      int a1 = color1 >> 24 & 255;
      int r1 = color1 >> (16) & (255);
      int g1 = color1 >> (8) & (255);
      int b1 = color1 & (255);
      int a2 = color2 >> (24) & 255;
      int r2 = color2 >> 16 & (255);
      int g2 = color2 >> 8 & (255);
      int b2 = color2 & (255);
      if (a1 == 0) {
         a1 = 255;
      }

      if (a2 == 0) {
         a2 = 255;
      }

      int a = (int)((float)a1 + (float)(a2 - a1) * progress);
      int r = (int)((float)r1 + (float)(r2 - r1) * progress);
      int g = (int)((float)g1 + (float)(g2 - g1) * progress);
      int b = (int)((float)b1 + (float)(b2 - b1) * progress);
      return a << (24) | r << (16) | g << (8) | b;
   }
   public static float pingPong01(float value) {
      float wrapped = value % 2.0f;
      if (wrapped < 0.0f) {
         wrapped += 2.0f;
      }

      return wrapped > 1.0f
         ? 2.0f - wrapped
         : wrapped;
   }

   public float getStringWidth(String text) {
      return text == null
         ? 0.0f
         : this.font.getWidth(this.stripFormattingCodes(text), this.size) / 2.0f;
   }

   public float getWidth(String text) {
      return this.getStringWidth(text);
   }

   public float getHeight() {
      return this.size;
   }

   public float getFontHeight() {
      return this.size;
   }

   public ObsidianComponent_81 getFont() {
      return this.font;
   }

   public float getSize() {
      return this.size;
   }
   public String stripFormattingCodes(String text) {
      if (text != null && text.indexOf(167) >= 0) {
         StringBuilder clean = new StringBuilder(text.length());

         for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (current == (167) && i + (1) < text.length()) {
               i++;
            } else {
               clean.append(current);
            }
         }

         return clean.toString();
      } else {
         return text;
      }
   }
}
