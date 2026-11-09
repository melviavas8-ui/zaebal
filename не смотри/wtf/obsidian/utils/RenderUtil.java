package wtf.obsidian.utils;

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
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public final class RenderUtil implements IInstance {
   private static final ConcurrentHashMap<String, Identifier> skinCache = new ConcurrentHashMap<>();
   private static final UUID DEFAULT_SKIN_UUID = new UUID(
      0L, 0L
   );

   public static void drawHudItem(DrawContext context, ItemStack stack, float x, float y, float scale, float z) {
      if (context != null && stack != null && !stack.isEmpty()) {
         MatrixStack matrices = context.getMatrices();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         matrices.push();
         matrices.translate(x, y, z);
         matrices.scale(scale, scale, 1.0f);
         context.drawItem(stack, 0, 0);
         matrices.pop();
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(true);
      }
   }

   public static void drawGradient6Rect(
      MatrixStack matrices,
      float x,
      float y,
      float width,
      float height,
      float radius,
      int leftTopColor,
      int leftBottomColor,
      int centerTopColor,
      int centerBottomColor,
      int rightTopColor,
      int rightBottomColor
   ) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      ShaderProgram shader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.gradient6Rect);
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      GlUniform sizeUniform = shader.getUniform("Size");
      GlUniform radiusUniform = shader.getUniform("Radius");
      GlUniform smoothnessUniform = shader.getUniform("Smoothness");
      GlUniform leftTopColorUniform = shader.getUniform("LeftTopColor");
      GlUniform leftBottomColorUniform = shader.getUniform("LeftBottomColor");
      GlUniform centerTopColorUniform = shader.getUniform("CenterTopColor");
      GlUniform centerBottomColorUniform = shader.getUniform("CenterBottomColor");
      GlUniform rightTopColorUniform = shader.getUniform("RightTopColor");
      GlUniform rightBottomColorUniform = shader.getUniform("RightBottomColor");
      if (sizeUniform != null) {
         sizeUniform.set(width, height);
      }

      if (radiusUniform != null) {
         radiusUniform.set(radius, radius, radius, radius);
      }

      if (smoothnessUniform != null) {
         smoothnessUniform.set(1.0f);
      }

      if (leftTopColorUniform != null) {
         int a = leftTopColor >> 24 & 255;
         if (a == 0) {
            a = 255;
         }

         leftTopColorUniform.set(
            (float)(leftTopColor >> (16) & (255)) / 255.0f,
            (float)(leftTopColor >> (8) & (255))
               / 255.0f,
            (float)(leftTopColor & 255) / 255.0f,
            (float)a / 255.0f
         );
      }

      if (leftBottomColorUniform != null) {
         int a = leftBottomColor >> 24 & (255);
         if (a == 0) {
            a = 255;
         }

         leftBottomColorUniform.set(
            (float)(leftBottomColor >> 16 & 255)
               / 255.0f,
            (float)(leftBottomColor >> (8) & 255)
               / 255.0f,
            (float)(leftBottomColor & (255)) / 255.0f,
            (float)a / 255.0f
         );
      }

      if (centerTopColorUniform != null) {
         int a = centerTopColor >> (24) & (255);
         if (a == 0) {
            a = 255;
         }

         centerTopColorUniform.set(
            (float)(centerTopColor >> 16 & (255))
               / 255.0f,
            (float)(centerTopColor >> 8 & 255)
               / 255.0f,
            (float)(centerTopColor & (255)) / 255.0f,
            (float)a / 255.0f
         );
      }

      if (centerBottomColorUniform != null) {
         int a = centerBottomColor >> (24) & 255;
         if (a == 0) {
            a = 255;
         }

         centerBottomColorUniform.set(
            (float)(centerBottomColor >> (16) & 255)
               / 255.0f,
            (float)(centerBottomColor >> (8) & (255))
               / 255.0f,
            (float)(centerBottomColor & 255) / 255.0f,
            (float)a / 255.0f
         );
      }

      if (rightTopColorUniform != null) {
         int a = rightTopColor >> (24) & 255;
         if (a == 0) {
            a = 255;
         }

         rightTopColorUniform.set(
            (float)(rightTopColor >> (16) & (255)) / 255.0f,
            (float)(rightTopColor >> 8 & 255)
               / 255.0f,
            (float)(rightTopColor & 255) / 255.0f,
            (float)a / 255.0f
         );
      }

      if (rightBottomColorUniform != null) {
         int a = rightBottomColor >> (24) & 255;
         if (a == 0) {
            a = 255;
         }

         rightBottomColorUniform.set(
            (float)(rightBottomColor >> (16) & (255)) / 255.0f,
            (float)(rightBottomColor >> 8 & 255)
               / 255.0f,
            (float)(rightBottomColor & (255)) / 255.0f,
            (float)a / 255.0f
         );
      }

      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      buffer.vertex(matrix, x, y, 0.0f)
         .color(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
      buffer.vertex(matrix, x, y + height, 0.0f)
         .color(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
      buffer.vertex(matrix, x + width, y + height, 0.0f)
         .color(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
      buffer.vertex(matrix, x + width, y, 0.0f)
         .color(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
      RenderSystem.setShader(ObsidianComponent_257.gradient6Rect);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      RenderSystem.disableBlend();
   }

   public static void drawShadow(
      MatrixStack matrices,
      float x,
      float y,
      float width,
      float height,
      float radius,
      float softness,
      int topLeftColor,
      int topRightColor,
      int bottomLeftColor,
      int bottomRightColor
   ) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      ShaderProgram shader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.shadowRect);
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      float extendedWidth = width + softness * 2.0f;
      float extendedHeight = height + softness * 2.0f;
      float drawX = x - softness;
      float drawY = y - softness;
      GlUniform sizeUniform = shader.getUniform("Size");
      GlUniform softnessUniform = shader.getUniform("Softness");
      GlUniform radiusUniform = shader.getUniform("Radius");
      GlUniform topLeftColorUniform = shader.getUniform("TopLeftColor");
      GlUniform topRightColorUniform = shader.getUniform("TopRightColor");
      GlUniform bottomLeftColorUniform = shader.getUniform("BottomLeftColor");
      GlUniform bottomRightColorUniform = shader.getUniform("BottomRightColor");
      if (sizeUniform != null) {
         sizeUniform.set(extendedWidth, extendedHeight);
      }

      if (softnessUniform != null) {
         softnessUniform.set(softness);
      }

      if (radiusUniform != null) {
         radiusUniform.set(radius);
      }

      if (topLeftColorUniform != null) {
         int a = topLeftColor >> 24 & (255);
         if (a == 0) {
            a = 255;
         }

         topLeftColorUniform.set(
            (float)(topLeftColor >> (16) & 255)
               / 255.0f,
            (float)(topLeftColor >> 8 & 0xFF) / 255.0f,
            (float)(topLeftColor & (255)) / 255.0f,
            (float)a / 255.0f
         );
      }

      if (topRightColorUniform != null) {
         int a = topRightColor >> 24 & (255);
         if (a == 0) {
            a = 255;
         }

         topRightColorUniform.set(
            (float)(topRightColor >> 16 & 255)
               / 255.0f,
            (float)(topRightColor >> 8 & (255))
               / 255.0f,
            (float)(topRightColor & 255) / 255.0f,
            (float)a / 255.0f
         );
      }

      if (bottomLeftColorUniform != null) {
         int a = bottomLeftColor >> (24) & (255);
         if (a == 0) {
            a = 255;
         }

         bottomLeftColorUniform.set(
            (float)(bottomLeftColor >> (16) & 255)
               / 255.0f,
            (float)(bottomLeftColor >> (8) & 255)
               / 255.0f,
            (float)(bottomLeftColor & 255) / 255.0f,
            (float)a / 255.0f
         );
      }

      if (bottomRightColorUniform != null) {
         int a = bottomRightColor >> 24 & 255;
         if (a == 0) {
            a = 255;
         }

         bottomRightColorUniform.set(
            (float)(bottomRightColor >> 16 & (255))
               / 255.0f,
            (float)(bottomRightColor >> (8) & 255)
               / 255.0f,
            (float)(bottomRightColor & 0xFF) / 255.0f,
            (float)a / 255.0f
         );
      }

      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
      buffer.vertex(matrix, drawX, drawY, 0.0f)
         .texture(0.0f, 0.0f);
      buffer.vertex(matrix, drawX, drawY + extendedHeight, 0.0f)
         .texture(0.0f, 1.0f);
      buffer.vertex(matrix, drawX + extendedWidth, drawY + extendedHeight, 0.0f)
         .texture(1.0f, 1.0f);
      buffer.vertex(matrix, drawX + extendedWidth, drawY, 0.0f)
         .texture(1.0f, 0.0f);
      RenderSystem.setShader(ObsidianComponent_257.shadowRect);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      RenderSystem.disableBlend();
   }

   public static void drawShadow(
      MatrixStack matrices,
      float x,
      float y,
      float width,
      float height,
      float radius,
      int topLeftColor,
      int topRightColor,
      int bottomLeftColor,
      int bottomRightColor
   ) {
      drawShadow(
         matrices,
         x,
         y,
         width,
         height,
         radius,
         10.0f,
         topLeftColor,
         topRightColor,
         bottomLeftColor,
         bottomRightColor
      );
   }

   public static void drawShadow(MatrixStack matrices, float x, float y, float width, float height, float radius, float softness, int color) {
      drawShadow(matrices, x, y, width, height, radius, softness, color, color, color, color);
   }

   public static void drawShadow(MatrixStack matrices, float x, float y, float width, float height, float radius, int color) {
      drawShadow(matrices, x, y, width, height, radius, 10.0f, color, color, color, color);
   }

   public static void drawShadow(MatrixStack matrices, float x, float y, float width, float height, int color) {
      drawShadow(
         matrices,
         x,
         y,
         width,
         height,
         0.0f,
         10.0f,
         color,
         color,
         color,
         color
      );
   }

   public static void drawShadow(MatrixStack matrices, float x, float y, float width, float height, float radius, float softness, int topColor, int bottomColor) {
      drawShadow(matrices, x, y, width, height, radius, softness, topColor, topColor, bottomColor, bottomColor);
   }

   public static void drawShadowHorizontal(
      MatrixStack matrices, float x, float y, float width, float height, float radius, float softness, int leftColor, int rightColor
   ) {
      drawShadow(matrices, x, y, width, height, radius, softness, leftColor, rightColor, leftColor, rightColor);
   }

   public static void drawShadow(
      MatrixStack matrices, float x, float y, float width, float height, float radius, float softness, float offsetX, float offsetY, int color
   ) {
      drawShadow(matrices, x + offsetX, y + offsetY, width, height, radius, softness, color, color, color, color);
   }

   public static void drawShadow(
      MatrixStack matrices,
      float x,
      float y,
      float width,
      float height,
      float radius,
      float softness,
      float offsetX,
      float offsetY,
      int topLeftColor,
      int topRightColor,
      int bottomLeftColor,
      int bottomRightColor
   ) {
      drawShadow(matrices, x + offsetX, y + offsetY, width, height, radius, softness, topLeftColor, topRightColor, bottomLeftColor, bottomRightColor);
   }

   public static void drawShadow6(
      MatrixStack matrices,
      float x,
      float y,
      float width,
      float height,
      float radius,
      float softness,
      int leftTopColor,
      int leftBottomColor,
      int centerTopColor,
      int centerBottomColor,
      int rightTopColor,
      int rightBottomColor
   ) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      ShaderProgram shader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.shadow6Rect);
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      float extendedWidth = width + softness * 2.0f;
      float extendedHeight = height + softness * 2.0f;
      float drawX = x - softness;
      float drawY = y - softness;
      GlUniform sizeUniform = shader.getUniform("Size");
      GlUniform softnessUniform = shader.getUniform("Softness");
      GlUniform radiusUniform = shader.getUniform("Radius");
      GlUniform leftTopColorUniform = shader.getUniform("LeftTopColor");
      GlUniform leftBottomColorUniform = shader.getUniform("LeftBottomColor");
      GlUniform centerTopColorUniform = shader.getUniform("CenterTopColor");
      GlUniform centerBottomColorUniform = shader.getUniform("CenterBottomColor");
      GlUniform rightTopColorUniform = shader.getUniform("RightTopColor");
      GlUniform rightBottomColorUniform = shader.getUniform("RightBottomColor");
      if (sizeUniform != null) {
         sizeUniform.set(extendedWidth, extendedHeight);
      }

      if (softnessUniform != null) {
         softnessUniform.set(softness);
      }

      if (radiusUniform != null) {
         radiusUniform.set(radius);
      }

      if (leftTopColorUniform != null) {
         int a = leftTopColor >> (24) & (255);
         if (a == 0) {
            a = 255;
         }

         leftTopColorUniform.set(
            (float)(leftTopColor >> 16 & 255)
               / 255.0f,
            (float)(leftTopColor >> (8) & (255)) / 255.0f,
            (float)(leftTopColor & 255) / 255.0f,
            (float)a / 255.0f
         );
      }

      if (leftBottomColorUniform != null) {
         int a = leftBottomColor >> (24) & (255);
         if (a == 0) {
            a = 255;
         }

         leftBottomColorUniform.set(
            (float)(leftBottomColor >> (16) & (255))
               / 255.0f,
            (float)(leftBottomColor >> 8 & 255)
               / 255.0f,
            (float)(leftBottomColor & 255) / 255.0f,
            (float)a / 255.0f
         );
      }

      if (centerTopColorUniform != null) {
         int a = centerTopColor >> 24 & (255);
         if (a == 0) {
            a = 255;
         }

         centerTopColorUniform.set(
            (float)(centerTopColor >> 16 & 255)
               / 255.0f,
            (float)(centerTopColor >> 8 & (255))
               / 255.0f,
            (float)(centerTopColor & (255)) / 255.0f,
            (float)a / 255.0f
         );
      }

      if (centerBottomColorUniform != null) {
         int a = centerBottomColor >> 24 & (255);
         if (a == 0) {
            a = 255;
         }

         centerBottomColorUniform.set(
            (float)(centerBottomColor >> (16) & 255)
               / 255.0f,
            (float)(centerBottomColor >> (8) & (255))
               / 255.0f,
            (float)(centerBottomColor & 255) / 255.0f,
            (float)a / 255.0f
         );
      }

      if (rightTopColorUniform != null) {
         int a = rightTopColor >> (24) & (255);
         if (a == 0) {
            a = 255;
         }

         rightTopColorUniform.set(
            (float)(rightTopColor >> 16 & (255))
               / 255.0f,
            (float)(rightTopColor >> 8 & (255))
               / 255.0f,
            (float)(rightTopColor & 255) / 255.0f,
            (float)a / 255.0f
         );
      }

      if (rightBottomColorUniform != null) {
         int a = rightBottomColor >> 24 & 255;
         if (a == 0) {
            a = 255;
         }

         rightBottomColorUniform.set(
            (float)(rightBottomColor >> 16 & (255))
               / 255.0f,
            (float)(rightBottomColor >> 8 & 255)
               / 255.0f,
            (float)(rightBottomColor & (255)) / 255.0f,
            (float)a / 255.0f
         );
      }

      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
      buffer.vertex(matrix, drawX, drawY, 0.0f)
         .texture(0.0f, 0.0f);
      buffer.vertex(matrix, drawX, drawY + extendedHeight, 0.0f)
         .texture(0.0f, 1.0f);
      buffer.vertex(matrix, drawX + extendedWidth, drawY + extendedHeight, 0.0f)
         .texture(1.0f, 1.0f);
      buffer.vertex(matrix, drawX + extendedWidth, drawY, 0.0f)
         .texture(1.0f, 0.0f);
      RenderSystem.setShader(ObsidianComponent_257.shadow6Rect);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      RenderSystem.disableBlend();
   }

   public static void drawTexture(
      MatrixStack matrices, Identifier texture, float x, float y, float width, float height, float u1, float v1, float u2, float v2, int color
   ) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShaderTexture(0, texture);
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      int alpha = color >> 24 & (255);
      if (alpha == 0) {
         alpha = 255;
      }

      float r = (float)(color >> (16) & 255)
         / 255.0f;
      float g = (float)(color >> (8) & 255)
         / 255.0f;
      float b = (float)(color & 255) / 255.0f;
      float a = (float)alpha / 255.0f;
      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      buffer.vertex(matrix, x, y, 0.0f).texture(u1, v1).color(r, g, b, a);
      buffer.vertex(matrix, x, y + height, 0.0f).texture(u1, v2).color(r, g, b, a);
      buffer.vertex(matrix, x + width, y + height, 0.0f).texture(u2, v2).color(r, g, b, a);
      buffer.vertex(matrix, x + width, y, 0.0f).texture(u2, v1).color(r, g, b, a);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      RenderSystem.setShaderTexture(0, 0);
      RenderSystem.disableBlend();
   }

   public static void drawImage(MatrixStack matrices, Identifier texture, float x, float y, float width, float height, int color) {
      drawTexture(
         matrices,
         texture,
         x,
         y,
         width,
         height,
         0.0f,
         0.0f,
         1.0f,
         1.0f,
         color
      );
   }

   public static void drawImage(MatrixStack matrices, String namespace, String path, float x, float y, float width, float height, int color) {
      drawImage(matrices, Identifier.of(namespace, path), x, y, width, height, color);
   }

   public static void drawSprite(MatrixStack matrices, Sprite sprite, float x, float y, float size, int color) {
      drawTexture(matrices, sprite.getAtlasId(), x, y, size, size, sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV(), color);
   }

   public static void drawPlayerHead(MatrixStack matrices, PlayerEntity player, float x, float y, float size, float radius, float hurtPercent) {
      if (player != null) {
         Identifier skinTexture = getSkinTexture(player);
         drawHeadInternal(matrices, skinTexture, x, y, size, radius, 1.0f, hurtPercent);
      }
   }

   public static void drawPlayerHead(MatrixStack matrices, String username, float x, float y, float size, float radius) {
      drawPlayerHead(
         matrices, username, x, y, size, radius, 1.0f, 0.0f
      );
   }

   public static void drawPlayerHead(MatrixStack matrices, String username, float x, float y, float size, float radius, float alpha, float hurtPercent) {
      if (username != null && !username.isEmpty()) {
         Identifier skinTexture = getSkinTextureByName(username);
         drawHeadInternal(matrices, skinTexture, x, y, size, radius, alpha, hurtPercent);
      }
   }

   public static void drawPlayerHead(MatrixStack matrices, UUID uuid, float x, float y, float size, float radius) {
      drawPlayerHead(
         matrices,
         uuid,
         x,
         y,
         size,
         radius,
         1.0f,
         0.0f
      );
   }

   public static void drawPlayerHead(MatrixStack matrices, UUID uuid, float x, float y, float size, float radius, float alpha, float hurtPercent) {
      if (uuid != null) {
         Identifier skinTexture = getSkinTextureByUUID(uuid);
         drawHeadInternal(matrices, skinTexture, x, y, size, radius, alpha, hurtPercent);
      }
   }

   public static void drawPlayerHead(MatrixStack matrices, PlayerListEntry entry, float x, float y, float size, float radius) {
      drawPlayerHead(
         matrices,
         entry,
         x,
         y,
         size,
         radius,
         1.0f,
         0.0f
      );
   }

   public static void drawPlayerHead(MatrixStack matrices, PlayerListEntry entry, float x, float y, float size, float radius, float alpha, float hurtPercent) {
      if (entry != null) {
         Identifier skinTexture = entry.getSkinTextures().texture();
         if (skinTexture == null) {
            skinTexture = DefaultSkinHelper.getSkinTextures(entry.getProfile().getId()).texture();
         }

         drawHeadInternal(matrices, skinTexture, x, y, size, radius, alpha, hurtPercent);
      }
   }

   public static void drawPlayerHead(MatrixStack matrices, Identifier skinTexture, float x, float y, float size, float radius) {
      drawHeadInternal(
         matrices, skinTexture, x, y, size, radius, 1.0f, 0.0f
      );
   }
   public static void drawHeadInternal(
      MatrixStack matrices, Identifier skinTexture, float x, float y, float size, float radius, float alpha, float hurtPercent
   ) {
      if (skinTexture == null) {
         skinTexture = DefaultSkinHelper.getSkinTextures(DEFAULT_SKIN_UUID).texture();
      }

      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShaderTexture(0, skinTexture);
      ShaderProgram shader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.face);
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      GlUniform locationUniform = shader.getUniform("location");
      GlUniform sizeUniform = shader.getUniform("size");
      GlUniform radiusUniform = shader.getUniform("radius");
      GlUniform alphaUniform = shader.getUniform("alpha");
      GlUniform uUniform = shader.getUniform("u");
      GlUniform vUniform = shader.getUniform("v");
      GlUniform wUniform = shader.getUniform("w");
      GlUniform hUniform = shader.getUniform("h");
      GlUniform hurtPercentUniform = shader.getUniform("hurtPercent");
      if (locationUniform != null) {
         locationUniform.set(x, y);
      }

      if (sizeUniform != null) {
         sizeUniform.set(size, size);
      }

      if (radiusUniform != null) {
         radiusUniform.set(radius);
      }

      if (alphaUniform != null) {
         alphaUniform.set(alpha);
      }

      if (uUniform != null) {
         uUniform.set(0.125f);
      }

      if (vUniform != null) {
         vUniform.set(0.125f);
      }

      if (wUniform != null) {
         wUniform.set(0.125f);
      }

      if (hUniform != null) {
         hUniform.set(0.125f);
      }

      if (hurtPercentUniform != null) {
         hurtPercentUniform.set(hurtPercent);
      }

      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
      buffer.vertex(matrix, x, y, 0.0f)
         .texture(0.0f, 0.0f);
      buffer.vertex(matrix, x, y + size, 0.0f)
         .texture(0.0f, 1.0F);
      buffer.vertex(matrix, x + size, y + size, 0.0f)
         .texture(1.0f, 1.0f);
      buffer.vertex(matrix, x + size, y, 0.0f)
         .texture(1.0f, 0.0f);
      RenderSystem.setShader(ObsidianComponent_257.face);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      drawHeadOverlay(matrices, skinTexture, x, y, size, radius, alpha, hurtPercent);
      RenderSystem.setShaderTexture(0, 0);
      RenderSystem.disableBlend();
   }
   public static void drawHeadOverlay(MatrixStack matrices, Identifier skinTexture, float x, float y, float size, float radius, float alpha, float hurtPercent) {
      RenderSystem.setShaderTexture(0, skinTexture);
      ShaderProgram shader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.face);
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      GlUniform locationUniform = shader.getUniform("location");
      GlUniform sizeUniform = shader.getUniform("size");
      GlUniform radiusUniform = shader.getUniform("radius");
      GlUniform alphaUniform = shader.getUniform("alpha");
      GlUniform uUniform = shader.getUniform("u");
      GlUniform vUniform = shader.getUniform("v");
      GlUniform wUniform = shader.getUniform("w");
      GlUniform hUniform = shader.getUniform("h");
      GlUniform hurtPercentUniform = shader.getUniform("hurtPercent");
      if (locationUniform != null) {
         locationUniform.set(x, y);
      }

      if (sizeUniform != null) {
         sizeUniform.set(size, size);
      }

      if (radiusUniform != null) {
         radiusUniform.set(radius);
      }

      if (alphaUniform != null) {
         alphaUniform.set(alpha);
      }

      if (uUniform != null) {
         uUniform.set(0.625f);
      }

      if (vUniform != null) {
         vUniform.set(0.125f);
      }

      if (wUniform != null) {
         wUniform.set(0.125f);
      }

      if (hUniform != null) {
         hUniform.set(0.125f);
      }

      if (hurtPercentUniform != null) {
         hurtPercentUniform.set(hurtPercent);
      }

      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
      buffer.vertex(matrix, x, y, 0.0f)
         .texture(0.0f, 0.0f);
      buffer.vertex(matrix, x, y + size, 0.0f)
         .texture(0.0f, 1.0f);
      buffer.vertex(matrix, x + size, y + size, 0.0f)
         .texture(1.0f, 1.0f);
      buffer.vertex(matrix, x + size, y, 0.0f)
         .texture(1.0f, 0.0f);
      RenderSystem.setShader(ObsidianComponent_257.face);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
   }
   public static Identifier getSkinTexture(PlayerEntity player) {
      if (mc.getNetworkHandler() == null) {
         return DefaultSkinHelper.getSkinTextures(player.getUuid()).texture();
      } else {
         PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
         return entry != null ? entry.getSkinTextures().texture() : DefaultSkinHelper.getSkinTextures(player.getUuid()).texture();
      }
   }
   public static Identifier getSkinTextureByName(String username) {
      String key = username.toLowerCase(Locale.ROOT);
      Identifier cachedTexture = skinCache.get(key);
      if (cachedTexture != null) {
         return cachedTexture;
      } else {
         if (mc.getNetworkHandler() != null) {
            for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
               if (entry.getProfile().getName().equalsIgnoreCase(username)) {
                  Identifier texture = entry.getSkinTextures().texture();
                  skinCache.put(key, texture);
                  return texture;
               }
            }
         }

         if (mc.world != null) {
            for (PlayerEntity player : mc.world.getPlayers()) {
               if (player.getName().getString().equalsIgnoreCase(username)) {
                  Identifier texture = getSkinTexture(player);
                  skinCache.put(key, texture);
                  return texture;
               }
            }
         }

         Identifier texture = DefaultSkinHelper.getSkinTextures(UUID.nameUUIDFromBytes(username.getBytes())).texture();
         skinCache.put(key, texture);
         return texture;
      }
   }
   public static Identifier getSkinTextureByUUID(UUID uuid) {
      String key = uuid.toString();
      if (skinCache.containsKey(key)) {
         return skinCache.get(key);
      } else {
         if (mc.getNetworkHandler() != null) {
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(uuid);
            if (entry != null) {
               Identifier texture = entry.getSkinTextures().texture();
               skinCache.put(key, texture);
               return texture;
            }
         }

         if (mc.world != null) {
            PlayerEntity player = mc.world.getPlayerByUuid(uuid);
            if (player != null) {
               Identifier texture = getSkinTexture(player);
               skinCache.put(key, texture);
               return texture;
            }
         }

         return DefaultSkinHelper.getSkinTextures(uuid).texture();
      }
   }

   public static void clearSkinCache() {
      skinCache.clear();
   }

   public static void removeSkinFromCache(String username) {
      skinCache.remove(username.toLowerCase(Locale.ROOT));
   }

   public static void drawRoundedRect(MatrixStack matrices, float x, float y, float width, float height, float radius, int color) {
      drawRoundedRect(matrices, x, y, width, height, radius, radius, radius, radius, color);
   }

   public static void drawDefaultHudElementRects(MatrixStack matrices, float x, float y, float width, float height, int themeColor) {
      drawDefaultHudElementRects(matrices, x, y, width, height, themeColor, true);
   }

   public static void drawDefaultHudElementRects(MatrixStack matrices, float x, float y, float width, float height, int themeColor, boolean drawPattern) {
      drawDefaultHudThemedPanel(
         matrices,
         x,
         y,
         width,
         height,
         3.0f,
         3.5f,
         themeColor
      );
      if (drawPattern) {
         drawHudSquarePattern(matrices, x, y, width, height, themeColor);
      }

      drawRoundedRect(
         matrices,
         x + width - 14.5f,
         y + 3.0f,
         10.0f,
         10.0f,
         2.0f,
         ObsidianComponent_olorUtil.darken(themeColor, 0.4000000059604645f)
      );
   }

   public static void drawHudSquarePattern(MatrixStack matrices, float x, float y, float width, float height, int themeColor) {
      if (!(width <= 6.0f) && !(height <= 6.0f)) {
         float clipX = x - 1.0f;
         float clipY = y + 1.0f;
         float clipW = Math.max(1.0f, width - 2.0f);
         float clipH = Math.max(
            1.0f, height - 2.0f
         );
         float themeAlphaMul = (float)(themeColor >>> 24 & 255) / 255.0F;
         if (!(themeAlphaMul <= 0.0010000000474974513f)) {
            if (clipH <= 20.0f) {
               float[][] var50 = new float[14][];
               int var73 = 0;
               float[] var107 = new float[3];
               var107[0] = 0.05000000074505806f;
               var107[1] = 0.07999999821186066f;
               var107[2] = 8.600000381469727f;
               var50[var73] = var107;
               var107 = new float[3];
               var107[0] = 0.9200000166893005f;
               var107[1] = 0.10000000149011612f;
               var107[2] = 8.800000190734863f;
               var50[1] = var107;
               var73 = 2;
               var107 = new float[3];
               var107[0] = 0.1599999964237213f;
               var107[1] = 0.7799999713897705f;
               var107[2] = 6.300000190734863f;
               var50[var73] = var107;
               var73 = 3;
               var107 = new float[3];
               var107[0] = 0.7699999809265137f;
               var107[1] = 0.800000011920929f;
               var107[2] = 6.5f;
               var50[var73] = var107;
               var73 = 4;
               var107 = new float[3];
               var107[0] = 0.3100000023841858f;
               var107[1] = 0.18000000715255737f;
               var107[2] = 6.0f;
               var50[var73] = var107;
               var107 = new float[]{0.5799999833106995f, 0.0F, 0.0F};
               var107[1] = 0.7400000095367432f;
               var107[2] = 5.800000190734863f;
               var50[5] = var107;
               var73 = 6;
               var107 = new float[3];
               var107[0] = 0.44999998807907104f;
               var107[1] = 0.44999998807907104f;
               var107[2] = 5.099999904632568f;
               var50[var73] = var107;
               var107 = new float[3];
               var107[0] = 0.8600000143051147f;
               var107[1] = 0.46000000834465027f;
               var107[2] = 5.3F;
               var50[7] = var107;
               var73 = 8;
               var107 = new float[3];
               var107[0] = 0.23000000417232513f;
               var107[1] = 0.5199999809265137f;
               var107[2] = 4.900000095367432f;
               var50[var73] = var107;
               var73 = 9;
               var107 = new float[3];
               var107[0] = 0.6700000166893005f;
               var107[1] = 0.3F;
               var107[2] = 5.0f;
               var50[var73] = var107;
               var73 = 10;
               var107 = new float[3];
               var107[0] = 0.10999999940395355f;
               var107[1] = 0.3400000035762787f;
               var107[2] = 5.5f;
               var50[var73] = var107;
               var73 = 11;
               var107 = new float[3];
               var107[0] = 0.3799999952316284f;
               var107[1] = 0.699999988079071f;
               var107[2] = 5.199999809265137f;
               var50[var73] = var107;
               var73 = 12;
               var107 = new float[3];
               var107[0] = 0.7200000286102295f;
               var107[1] = 0.1599999964237213f;
               var107[2] = 5.699999809265137f;
               var50[var73] = var107;
               var73 = 13;
               var107 = new float[3];
               var107[0] = 0.949999988079071f;
               var107[1] = 0.6800000071525574f;
               var107[2] = 5.099999904632568f;
               var50[var73] = var107;
               float[][] compactSlots = var50;
               float desiredCount = Math.min(
                  (float)compactSlots.length,
                  3.700000047683716f
                     + Math.max(0.0f, (clipW - 84.0F) / 32.0F)
               );
               int outlineColorBase = ObsidianComponent_olorUtil.setAlphaColor(
                  ObsidianComponent_olorUtil.darken(themeColor, 0.6200000047683716f),
                  Math.max(
                     0,
                     Math.min(255, (int)(82.0f * themeAlphaMul))
                  )
               );
               ObsidianComponent_343.push();
               ObsidianComponent_343.setFromComponentCoordinates((double)clipX, (double)clipY, (double)clipW, (double)clipH);

               try {
                  for (int i = 0; i < compactSlots.length; i++) {
                     float reveal = desiredCount - (float)i;
                     if (!(reveal <= 0.0f)) {
                        float alphaMul = Math.max(0.0F, Math.min(1.0f, reveal));
                        alphaMul = alphaMul * alphaMul * (3.0F - 2.0F * alphaMul);
                        if (!(alphaMul <= 0.019999999552965164f)) {
                           float size = compactSlots[i][2];
                           float px = clipX
                              + 0.800000011920929f
                              + compactSlots[i][0]
                                 * Math.max(
                                    1.0f,
                                    clipW - size + 1.600000023841858f
                                 );
                           float py = clipY
                              - 1.2000000476837158f
                              + compactSlots[i][1]
                                 * Math.max(
                                    1.0f,
                                    clipH - size + 2.4000000953674316f
                                 );
                           int outlineAlpha = Math.max(
                              0,
                              Math.min(
                                 255,
                                 (int)(86.0f * alphaMul * themeAlphaMul)
                              )
                           );
                           if (outlineAlpha > 0) {
                              int outlineColor = ObsidianComponent_olorUtil.setAlphaColor(outlineColorBase, outlineAlpha);
                              drawRoundedRectOutline(
                                 matrices,
                                 px,
                                 py,
                                 size,
                                 size,
                                 0.0f,
                                 0.5f,
                                 outlineColor,
                                 outlineColor,
                                 outlineColor,
                                 outlineColor
                              );
                           }
                        }
                     }
                  }
               } finally {
                  ObsidianComponent_343.unset();
                  ObsidianComponent_343.pop();
               }
            } else {
               float[][] var10000 = new float[24][];
               int var10002 = 0;
               float[] var10003 = new float[3];
               var10003[0] = 0.05000000074505806f;
               var10003[1] = 4.0F;
               var10003[2] = 9.6F;
               var10000[var10002] = var10003;
               var10002 = 1;
               var10003 = new float[3];
               var10003[0] = 0.8700000047683716f;
               var10003[1] = 4.0f;
               var10003[2] = 9.199999809265137f;
               var10000[var10002] = var10003;
               var10003 = new float[3];
               var10003[0] = 0.5f;
               var10003[1] = 8.0f;
               var10003[2] = 7.400000095367432f;
               var10000[2] = var10003;
               var10002 = 3;
               var10003 = new float[3];
               var10003[0] = 0.18000000715255737f;
               var10003[1] = 13.0f;
               var10003[2] = 6.199999809265137f;
               var10000[var10002] = var10003;
               var10002 = 4;
               var10003 = new float[3];
               var10003[0] = 0.7200000286102295f;
               var10003[1] = 13.0f;
               var10003[2] = 6.0f;
               var10000[var10002] = var10003;
               var10002 = 5;
               var10003 = new float[3];
               var10003[0] = 0.07000000029802322f;
               var10003[1] = 21.0f;
               var10003[2] = 5.599999904632568f;
               var10000[var10002] = var10003;
               var10002 = 6;
               var10003 = new float[3];
               var10003[0] = 0.9100000262260437f;
               var10003[1] = 21.0f;
               var10003[2] = 5.800000190734863f;
               var10000[var10002] = var10003;
               var10002 = 7;
               var10003 = new float[3];
               var10003[0] = 0.23999999463558197f;
               var10003[1] = 30.0f;
               var10003[2] = 5.4F;
               var10000[var10002] = var10003;
               var10002 = 8;
               var10003 = new float[3];
               var10003[0] = 0.6600000262260437f;
               var10003[1] = 30.0f;
               var10003[2] = 5.5f;
               var10000[var10002] = var10003;
               var10002 = 9;
               var10003 = new float[3];
               var10003[0] = 0.04F;
               var10003[1] = 38.0F;
               var10003[2] = 6.800000190734863f;
               var10000[var10002] = var10003;
               var10002 = 10;
               var10003 = new float[3];
               var10003[0] = 0.8999999761581421f;
               var10003[1] = 38.0f;
               var10003[2] = 7.0f;
               var10000[var10002] = var10003;
               var10002 = 11;
               var10003 = new float[3];
               var10003[0] = 0.15F;
               var10003[1] = 47.0f;
               var10003[2] = 5.400000095367432f;
               var10000[var10002] = var10003;
               var10002 = 12;
               var10003 = new float[3];
               var10003[0] = 0.7799999713897705f;
               var10003[1] = 47.0f;
               var10003[2] = 5.5f;
               var10000[var10002] = var10003;
               var10002 = 13;
               var10003 = new float[3];
               var10003[0] = 0.08F;
               var10003[1] = 56.0f;
               var10003[2] = 5.099999904632568f;
               var10000[var10002] = var10003;
               var10002 = 14;
               var10003 = new float[3];
               var10003[0] = 0.9200000166893005f;
               var10003[1] = 56.0f;
               var10003[2] = 5.199999809265137f;
               var10000[var10002] = var10003;
               var10002 = 15;
               var10003 = new float[3];
               var10003[0] = 0.23000000417232513f;
               var10003[1] = 65.0f;
               var10003[2] = 5.800000190734863f;
               var10000[var10002] = var10003;
               var10002 = 16;
               var10003 = new float[3];
               var10003[0] = 0.6899999976158142f;
               var10003[1] = 65.0F;
               var10003[2] = 5.900000095367432f;
               var10000[var10002] = var10003;
               var10002 = 17;
               var10003 = new float[3];
               var10003[0] = 0.5199999809265137f;
               var10003[1] = 71.0f;
               var10003[2] = 7.199999809265137f;
               var10000[var10002] = var10003;
               var10002 = 18;
               var10003 = new float[3];
               var10003[0] = 0.05999999865889549f;
               var10003[1] = 74.0f;
               var10003[2] = 7.599999904632568f;
               var10000[var10002] = var10003;
               var10002 = 19;
               var10003 = new float[3];
               var10003[0] = 0.8799999952316284f;
               var10003[1] = 74.0f;
               var10003[2] = 7.4F;
               var10000[var10002] = var10003;
               var10002 = 20;
               var10003 = new float[3];
               var10003[0] = 0.14000000059604645f;
               var10003[1] = 85.0f;
               var10003[2] = 5.699999809265137f;
               var10000[var10002] = var10003;
               var10002 = 21;
               var10003 = new float[3];
               var10003[0] = 0.8199999928474426f;
               var10003[1] = 85.0f;
               var10003[2] = 5.800000190734863f;
               var10000[var10002] = var10003;
               var10002 = 22;
               var10003 = new float[3];
               var10003[0] = 0.09000000357627869f;
               var10003[1] = 97.0f;
               var10003[2] = 6.5F;
               var10000[var10002] = var10003;
               var10002 = 23;
               var10003 = new float[3];
               var10003[0] = 0.8999999761581421f;
               var10003[1] = 98.0f;
               var10003[2] = 6.599999904632568f;
               var10000[var10002] = var10003;
               float[][] slots = var10000;
               int baseCount = 10;
               float extraHeight = Math.max(
                  0.0f, clipH - 24.0f
               );
               float desiredCount = Math.min((float)slots.length, (float)baseCount + extraHeight / 10.0f);
               float panelAlpha = Math.max(
                  0.0f,
                  Math.min(
                     1.0f,
                     (clipH - 10.0f)
                        / 16.0f
                  )
               );
               panelAlpha = panelAlpha
                  * panelAlpha
                  * (3.0f - 2.0f * panelAlpha);
               int outlineColorBase = ObsidianComponent_olorUtil.setAlphaColor(
                  ObsidianComponent_olorUtil.darken(themeColor, 0.7200000286102295f),
                  Math.max(
                     0,
                     Math.min(255, (int)(40.0f * themeAlphaMul))
                  )
               );
               ObsidianComponent_343.push();
               ObsidianComponent_343.setFromComponentCoordinates((double)clipX, (double)clipY, (double)clipW, (double)clipH);

               try {
                  for (int ix = 0; ix < slots.length; ix++) {
                     float reveal = desiredCount - (float)ix;
                     if (!(reveal <= 0.0f)) {
                        float alphaMul = Math.max(
                           0.0f, Math.min(1.0f, reveal)
                        );
                        alphaMul = alphaMul
                           * alphaMul
                           * (
                              3.0f
                                 - 2.0f * alphaMul
                           );
                        alphaMul *= panelAlpha;
                        if (!(alphaMul <= 0.014999999664723873f)) {
                           float size = slots[ix][2];
                           float px = clipX
                              + 2.0F
                              + slots[ix][0]
                                 * Math.max(
                                    1.0f, clipW - size - 4.0f
                                 );
                           float py = clipY + slots[ix][1];
                           float bottomLimit = clipY + clipH - 1.0f;
                           if (!(py >= bottomLimit)) {
                              if (py + size > bottomLimit) {
                                 float visible = Math.max(
                                    0.0f,
                                    Math.min(
                                       1.0f,
                                       (bottomLimit - py) / Math.max(1.0f, size)
                                    )
                                 );
                                 visible = visible
                                    * visible
                                    * (
                                       3.0f
                                          - 2.0f * visible
                                    );
                                 alphaMul *= visible;
                                 if (alphaMul <= 0.014999999664723873f) {
                                    continue;
                                 }
                              }

                              int outlineAlpha = Math.max(
                                 0, Math.min(255, (int)(58.0F * alphaMul * themeAlphaMul))
                              );
                              if (outlineAlpha > 0) {
                                 int outlineColor = ObsidianComponent_olorUtil.setAlphaColor(outlineColorBase, outlineAlpha);
                                 drawRoundedRectOutline(
                                    matrices,
                                    px,
                                    py,
                                    size,
                                    size,
                                    0.0f,
                                    0.550000011920929f,
                                    outlineColor,
                                    outlineColor,
                                    outlineColor,
                                    outlineColor
                                 );
                              }
                           }
                        }
                     }
                  }
               } finally {
                  ObsidianComponent_343.unset();
                  ObsidianComponent_343.pop();
               }
            }
         }
      }
   }

   public static void drawDefaultHudInfoBox(MatrixStack matrices, float x, float y, float width, int outerColor, int innerColor) {
      drawRoundedRect(
         matrices,
         x - 0.25f,
         y - 1.25f,
         width + 0.5f,
         9.0f,
         1.2999999523162842f,
         outerColor
      );
      drawRoundedRect(
         matrices,
         x,
         y - 1.0f,
         width,
         8.5f,
         1.0f,
         innerColor
      );
   }

   public static void drawDefaultHudPanel(
      MatrixStack matrices,
      float x,
      float y,
      float width,
      float height,
      float gradientRadius,
      float borderRadius,
      int borderColor,
      int topColor,
      int bottomColor
   ) {
      drawRoundedRect(
         matrices,
         x - 0.5f,
         y - 0.5f,
         width + 1.0f,
         height + 1.0f,
         borderRadius,
         borderColor
      );
      drawGradientRect(matrices, x, y, width, height, gradientRadius, topColor, bottomColor);
   }

   public static void drawDefaultHudThemedPanel(
      MatrixStack matrices, float x, float y, float width, float height, float gradientRadius, float borderRadius, int themeColor
   ) {
      drawDefaultHudPanel(
         matrices,
         x,
         y,
         width,
         height,
         gradientRadius,
         borderRadius,
         ObsidianComponent_olorUtil.rgba(
            50,
            50,
            50,
            255
         ),
         ObsidianComponent_olorUtil.darken(themeColor, 0.15000000596046448f),
         ObsidianComponent_olorUtil.darken(themeColor, 0.05000000074505806f)
      );
   }

   public static void drawWaveHudHeader(
      MatrixStack matrices,
      float x,
      float y,
      float width,
      float height,
      float radius,
      float shadowRadius,
      float shadowSoftness,
      int leftTop,
      int leftBottom,
      int centerTop,
      int centerBottom,
      int rightTop,
      int rightBottom
   ) {
      drawShadow6(matrices, x, y, width, height, shadowRadius, shadowSoftness, leftTop, leftBottom, centerTop, centerBottom, rightTop, rightBottom);
      drawGradient6Rect(matrices, x, y, width, height, radius, leftTop, leftBottom, centerTop, centerBottom, rightTop, rightBottom);
   }

   public static void drawWaveHudPanel(
      MatrixStack matrices,
      float x,
      float y,
      float width,
      float height,
      int bgColor,
      float headerHeight,
      float headerRadius,
      float shadowRadius,
      float shadowSoftness,
      int leftTop,
      int leftBottom,
      int centerTop,
      int centerBottom,
      int rightTop,
      int rightBottom
   ) {
      drawRoundedRect(matrices, x, y, width, height, 0.0f, bgColor);
      drawWaveHudHeader(
         matrices, x, y, width, headerHeight, headerRadius, shadowRadius, shadowSoftness, leftTop, leftBottom, centerTop, centerBottom, rightTop, rightBottom
      );
   }

   public static void drawTargetHudWaveFrame(MatrixStack matrices, float x, float y, float width, float height, float padding, float entityBoxSize, float alpha) {
      drawRoundedRect(
         matrices,
         x,
         y,
         width,
         height,
         0.0f,
         ObsidianComponent_olorUtil.applyAlpha(
            ObsidianComponent_olorUtil.rgba(
               40,
               40,
               40,
               255
            ),
            alpha
         )
      );
      drawRoundedRect(
         matrices,
         x + padding,
         y + padding,
         width - padding * 2.0f,
         height - padding * 2.0f,
         0.0f,
         ObsidianComponent_olorUtil.applyAlpha(
            ObsidianComponent_olorUtil.rgba(
               20, 20, 20, 255
            ),
            alpha
         )
      );
      drawRoundedRect(
         matrices,
         x + padding + 2.0f,
         y + padding + 2.0f,
         entityBoxSize,
         entityBoxSize,
         0.0f,
         ObsidianComponent_olorUtil.applyAlpha(
            ObsidianComponent_olorUtil.rgba(
               40,
               40,
               40,
               255
            ),
            alpha
         )
      );
      drawRoundedRect(
         matrices,
         x + padding + 3.0f,
         y + padding + 3.0f,
         entityBoxSize - 2.0f,
         entityBoxSize - 2.0f,
         0.0f,
         ObsidianComponent_olorUtil.applyAlpha(
            ObsidianComponent_olorUtil.rgba(
               25,
               25,
               25,
               255
            ),
            alpha
         )
      );
   }

   public static void drawTargetHudDefaultPlaceholder(MatrixStack matrices, float x, float y, float alpha) {
      drawRoundedRect(
         matrices,
         x - 1.0f,
         y - 1.0f,
         22.0f,
         22.0f,
         1.0f,
         ObsidianComponent_olorUtil.applyAlpha(
            ObsidianComponent_olorUtil.rgba(
               21,
               21,
               21,
               255
            ),
            alpha
         )
      );
   }

   public static void drawTargetHudHealthBars(
      MatrixStack matrices, float x, float y, float width, float trailProgress, float progress, int themeColor, int themecolor2, float alpha
   ) {
      drawRoundedRect(
         matrices,
         x,
         y,
         width,
         5.5f,
         1.25f,
         ObsidianComponent_olorUtil.applyAlpha(
            ObsidianComponent_olorUtil.darken(themeColor, 0.5f),
            alpha * 0.800000011920929f
         )
      );
      drawRoundedRect(
         matrices,
         x,
         y,
         width * trailProgress,
         5.5f,
         1.25f,
         ObsidianComponent_olorUtil.applyAlpha(
            ObsidianComponent_olorUtil.darken(themeColor, 0.800000011920929f),
            alpha * 0.800000011920929f
         )
      );
      drawGradientRect(
         matrices,
         x,
         y,
         width * progress,
         5.5f,
         1.25f,
         ObsidianComponent_olorUtil.applyAlpha(themeColor, alpha),
         ObsidianComponent_olorUtil.applyAlpha(themecolor2, alpha),
         true
      );
   }

   public static void drawTargetHudGoldenBars(
      MatrixStack matrices, float x, float y, float width, float height, float trailProgress, float progress, float alpha, float goldenAlpha
   ) {
      int goldenColor = ObsidianComponent_olorUtil.rgba(
         255,
         215,
         0,
         255
      );
      drawRoundedRect(
         matrices,
         x,
         y,
         width * trailProgress,
         height,
         1.25f,
         ObsidianComponent_olorUtil.applyAlpha(
            ObsidianComponent_olorUtil.darken(goldenColor, 0.6499999761581421f),
            alpha * goldenAlpha * 0.800000011920929f
         )
      );
      drawGradientRect(
         matrices,
         x,
         y,
         width * progress,
         height,
         1.25f,
         ObsidianComponent_olorUtil.applyAlpha(ObsidianComponent_olorUtil.darken(goldenColor, 0.550000011920929f), alpha * goldenAlpha),
         ObsidianComponent_olorUtil.applyAlpha(goldenColor, alpha * goldenAlpha),
         true
      );
   }

   public static void drawTargetHudHeartBase(MatrixStack matrices, float x, float y, float alpha) {
      drawRoundedRect(
         matrices,
         x,
         y,
         6.199999809265137f,
         4.5f,
         0.0f,
         ObsidianComponent_olorUtil.applyAlpha(
            ObsidianComponent_olorUtil.rgba(
               0,
               0,
               0,
               255
            ),
            alpha
         )
      );
   }

   public static void drawTargetHudHeartFill(MatrixStack matrices, float x, float y, float width, int heartColor, int shadowColor) {
      drawShadow(
         matrices,
         x + 1.0f,
         y + 1.0f,
         width,
         2.0f,
         0.0f,
         8.0f,
         shadowColor
      );
      drawRoundedRect(
         matrices,
         x,
         y,
         width + 1.2000000476837158f,
         4.5f,
         0.0f,
         heartColor
      );
   }

   public static void drawKeyStrokeRect(MatrixStack matrices, float x, float y, float width, float height, float radius, int color) {
      drawRoundedRect(matrices, x, y, width, height, radius, color);
   }

   public static void drawRoundedRect(
      MatrixStack matrices, float x, float y, float width, float height, float topLeft, float topRight, float bottomRight, float bottomLeft, int color
   ) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      ShaderProgram shader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.roundedRect);
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      GlUniform sizeUniform = shader.getUniform("Size");
      GlUniform radiusUniform = shader.getUniform("Radius");
      if (sizeUniform != null) {
         sizeUniform.set(width, height);
      }

      if (radiusUniform != null) {
         radiusUniform.set(topLeft, topRight, bottomRight, bottomLeft);
      }

      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      int alpha = color >> (24) & 255;
      if (alpha == 0) {
         alpha = 255;
      }

      float r = (float)(color >> (16) & 255)
         / 255.0f;
      float g = (float)(color >> (8) & (255)) / 255.0f;
      float b = (float)(color & (255)) / 255.0f;
      float a = (float)alpha / 255.0f;
      buffer.vertex(matrix, x, y, 0.0f).color(r, g, b, a);
      buffer.vertex(matrix, x, y + height, 0.0f).color(r, g, b, a);
      buffer.vertex(matrix, x + width, y + height, 0.0f).color(r, g, b, a);
      buffer.vertex(matrix, x + width, y, 0.0f).color(r, g, b, a);
      RenderSystem.setShader(ObsidianComponent_257.roundedRect);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      RenderSystem.disableBlend();
   }

   public static void drawRoundCircle(MatrixStack matrices, float x, float y, float radius, int color) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      drawRoundedRect(
         matrices,
         x - radius / 2.0f,
         y - radius / 2.0f,
         radius,
         radius,
         radius / 2.0f - 0.5f,
         color
      );
   }

   public static void drawRingArc(MatrixStack matrices, float x, float y, float size, float thickness, float startDeg, float endDeg, int color) {
      if (!(size <= 0.0f) && !(thickness <= 0.0f)) {
         float radius = size / 2.0f;
         float start = (float)Math.toRadians((double)startDeg);
         float end = (float)Math.toRadians((double)endDeg);
         float twoPi = 6.2831854820251465f;
         if (start < 0.0f) {
            start += twoPi;
         }

         if (end < 0.0f) {
            end += twoPi;
         }

         while (end < start) {
            end += twoPi;
         }

         if (end - start <= 9.999999747378752e-05f) {
            end = start + twoPi;
         }

         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         ShaderProgram shader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.ringArc);
         GlUniform sizeUniform = shader.getUniform("Size");
         GlUniform radiusUniform = shader.getUniform("Radius");
         GlUniform thicknessUniform = shader.getUniform("Thickness");
         GlUniform startUniform = shader.getUniform("StartAngle");
         GlUniform endUniform = shader.getUniform("EndAngle");
         GlUniform smoothnessUniform = shader.getUniform("Smoothness");
         GlUniform colorModulatorUniform = shader.getUniform("ColorModulator");
         if (sizeUniform != null) {
            sizeUniform.set(size, size);
         }

         if (radiusUniform != null) {
            radiusUniform.set(radius);
         }

         if (thicknessUniform != null) {
            thicknessUniform.set(thickness);
         }

         if (startUniform != null) {
            startUniform.set(start);
         }

         if (endUniform != null) {
            endUniform.set(end);
         }

         if (smoothnessUniform != null) {
            smoothnessUniform.set(
               Math.min(1.0f, thickness * 0.5f)
            );
         }

         if (colorModulatorUniform != null) {
            colorModulatorUniform.set(
               1.0f,
               1.0f,
               1.0f,
               1.0f
            );
         }

         Matrix4f matrix = matrices.peek().getPositionMatrix();
         BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
         int alpha = color >> 24 & 255;
         if (alpha == 0) {
            alpha = 255;
         }

         float r = (float)(color >> 16 & 255)
            / 255.0f;
         float g = (float)(color >> 8 & (255))
            / 255.0f;
         float b = (float)(color & 255) / 255.0f;
         float a = (float)alpha / 255.0f;
         buffer.vertex(matrix, x, y, 0.0f).color(r, g, b, a);
         buffer.vertex(matrix, x, y + size, 0.0f).color(r, g, b, a);
         buffer.vertex(matrix, x + size, y + size, 0.0f).color(r, g, b, a);
         buffer.vertex(matrix, x + size, y, 0.0f).color(r, g, b, a);
         RenderSystem.setShader(ObsidianComponent_257.ringArc);
         BufferRenderer.drawWithGlobalProgram(buffer.end());
         RenderSystem.disableBlend();
      }
   }

   public static void drawGradientRect(
      MatrixStack matrices,
      float x,
      float y,
      float width,
      float height,
      float topLeft,
      float topRight,
      float bottomRight,
      float bottomLeft,
      int topLeftColor,
      int topRightColor,
      int bottomLeftColor,
      int bottomRightColor
   ) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      ShaderProgram shader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.gradientRect);
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      GlUniform sizeUniform = shader.getUniform("Size");
      GlUniform radiusUniform = shader.getUniform("Radius");
      GlUniform smoothnessUniform = shader.getUniform("Smoothness");
      GlUniform colorModulatorUniform = shader.getUniform("ColorModulator");
      GlUniform topLeftColorUniform = shader.getUniform("TopLeftColor");
      GlUniform bottomLeftColorUniform = shader.getUniform("BottomLeftColor");
      GlUniform topRightColorUniform = shader.getUniform("TopRightColor");
      GlUniform bottomRightColorUniform = shader.getUniform("BottomRightColor");
      if (sizeUniform != null) {
         sizeUniform.set(width, height);
      }

      if (radiusUniform != null) {
         radiusUniform.set(topLeft, topRight, bottomRight, bottomLeft);
      }

      if (smoothnessUniform != null) {
         smoothnessUniform.set(1.0f);
      }

      if (colorModulatorUniform != null) {
         colorModulatorUniform.set(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
      }

      int tlAlpha = topLeftColor >> (24) & 255;
      if (tlAlpha == 0) {
         tlAlpha = 255;
      }

      if (topLeftColorUniform != null) {
         topLeftColorUniform.set(
            (float)(topLeftColor >> 16 & 255)
               / 255.0f,
            (float)(topLeftColor >> 8 & 255)
               / 255.0f,
            (float)(topLeftColor & (255)) / 255.0f,
            (float)tlAlpha / 255.0f
         );
      }

      int blAlpha = bottomLeftColor >> 24 & 255;
      if (blAlpha == 0) {
         blAlpha = 255;
      }

      if (bottomLeftColorUniform != null) {
         bottomLeftColorUniform.set(
            (float)(bottomLeftColor >> (16) & 255)
               / 255.0f,
            (float)(bottomLeftColor >> 8 & 255)
               / 255.0f,
            (float)(bottomLeftColor & 255) / 255.0f,
            (float)blAlpha / 255.0f
         );
      }

      int trAlpha = topRightColor >> (24) & (255);
      if (trAlpha == 0) {
         trAlpha = 255;
      }

      if (topRightColorUniform != null) {
         topRightColorUniform.set(
            (float)(topRightColor >> 16 & 255)
               / 255.0f,
            (float)(topRightColor >> 8 & (255))
               / 255.0f,
            (float)(topRightColor & (255)) / 255.0f,
            (float)trAlpha / 255.0f
         );
      }

      int brAlpha = bottomRightColor >> 24 & 0xFF;
      if (brAlpha == 0) {
         brAlpha = 255;
      }

      if (bottomRightColorUniform != null) {
         bottomRightColorUniform.set(
            (float)(bottomRightColor >> (16) & 255)
               / 255.0f,
            (float)(bottomRightColor >> (8) & 255)
               / 255.0f,
            (float)(bottomRightColor & (255)) / 255.0f,
            (float)brAlpha / 255.0f
         );
      }

      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      buffer.vertex(matrix, x, y, 0.0f)
         .texture(0.0f, 0.0f)
         .color(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
      buffer.vertex(matrix, x, y + height, 0.0f)
         .texture(0.0f, 1.0f)
         .color(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
      buffer.vertex(matrix, x + width, y + height, 0.0f)
         .texture(1.0f, 1.0f)
         .color(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
      buffer.vertex(matrix, x + width, y, 0.0f)
         .texture(1.0f, 0.0f)
         .color(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
      RenderSystem.setShader(ObsidianComponent_257.gradientRect);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      RenderSystem.disableBlend();
   }

   public static void drawGradientRect(
      MatrixStack matrices,
      float x,
      float y,
      float width,
      float height,
      float radius,
      int topLeftColor,
      int topRightColor,
      int bottomLeftColor,
      int bottomRightColor
   ) {
      drawGradientRect(matrices, x, y, width, height, radius, radius, radius, radius, topLeftColor, topRightColor, bottomLeftColor, bottomRightColor);
   }

   public static void drawGradientRect(MatrixStack matrices, float x, float y, float width, float height, float radius, int topColor, int bottomColor) {
      drawGradientRect(matrices, x, y, width, height, radius, radius, radius, radius, topColor, topColor, bottomColor, bottomColor);
   }

   public static void drawGradientRect(MatrixStack matrices, float x, float y, float width, float height, int topColor, int bottomColor) {
      drawGradientRect(
         matrices,
         x,
         y,
         width,
         height,
         0.0f,
         0.0f,
         0.0f,
         0.0f,
         topColor,
         topColor,
         bottomColor,
         bottomColor
      );
   }

   public static void drawGradientRect(
      MatrixStack matrices, float x, float y, float width, float height, float radius, int leftColor, int rightColor, boolean horizontal
   ) {
      if (horizontal) {
         drawGradientRect(matrices, x, y, width, height, radius, radius, radius, radius, leftColor, rightColor, leftColor, rightColor);
      } else {
         drawGradientRect(matrices, x, y, width, height, radius, radius, radius, radius, leftColor, leftColor, rightColor, rightColor);
      }
   }

   public static void drawRoundedRectOutline(
      MatrixStack matrices,
      float x,
      float y,
      float width,
      float height,
      float topLeft,
      float topRight,
      float bottomRight,
      float bottomLeft,
      float outline,
      int outlineColor
   ) {
      drawRoundedRectOutline(
         matrices, x, y, width, height, topLeft, topRight, bottomRight, bottomLeft, outline, outlineColor, outlineColor, outlineColor, outlineColor
      );
   }

   public static void drawRoundedRectOutline(
      MatrixStack matrices,
      float x,
      float y,
      float width,
      float height,
      float radius,
      float outline,
      int topLeftColor,
      int topRightColor,
      int bottomLeftColor,
      int bottomRightColor
   ) {
      drawRoundedRectOutline(
         matrices, x, y, width, height, radius, radius, radius, radius, outline, topLeftColor, topRightColor, bottomLeftColor, bottomRightColor
      );
   }

   public static void drawRoundedRectOutline(
      MatrixStack matrices,
      float x,
      float y,
      float width,
      float height,
      float topLeft,
      float topRight,
      float bottomRight,
      float bottomLeft,
      float outline,
      int topLeftColor,
      int topRightColor,
      int bottomLeftColor,
      int bottomRightColor
   ) {
      if (!(outline <= 0.0F)) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         ShaderProgram shader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.roundedRectOutline);
         Matrix4f matrix = matrices.peek().getPositionMatrix();
         GlUniform sizeUniform = shader.getUniform("Size");
         GlUniform radiusUniform = shader.getUniform("Radius");
         GlUniform smoothnessUniform = shader.getUniform("Smoothness");
         GlUniform colorModulatorUniform = shader.getUniform("ColorModulator");
         GlUniform outlineUniform = shader.getUniform("Outline");
         GlUniform topLeftColorUniform = shader.getUniform("TopLeftColor");
         GlUniform bottomLeftColorUniform = shader.getUniform("BottomLeftColor");
         GlUniform topRightColorUniform = shader.getUniform("TopRightColor");
         GlUniform bottomRightColorUniform = shader.getUniform("BottomRightColor");
         if (sizeUniform != null) {
            sizeUniform.set(width, height);
         }

         if (radiusUniform != null) {
            radiusUniform.set(topLeft, topRight, bottomRight, bottomLeft);
         }

         if (smoothnessUniform != null) {
            smoothnessUniform.set(1.0f);
         }

         if (colorModulatorUniform != null) {
            colorModulatorUniform.set(
               1.0f,
               1.0f,
               1.0f,
               1.0f
            );
         }

         if (outlineUniform != null) {
            outlineUniform.set(outline);
         }

         if (topLeftColorUniform != null) {
            int a = topLeftColor >> (24) & 255;
            if (a == 0) {
               a = 255;
            }

            topLeftColorUniform.set(
               (float)(topLeftColor >> 16 & 255)
                  / 255.0f,
               (float)(topLeftColor >> (8) & (255)) / 255.0f,
               (float)(topLeftColor & (255)) / 255.0f,
               (float)a / 255.0F
            );
         }

         if (bottomLeftColorUniform != null) {
            int a = bottomLeftColor >> 24 & 255;
            if (a == 0) {
               a = 255;
            }

            bottomLeftColorUniform.set(
               (float)(bottomLeftColor >> (16) & (255)) / 255.0f,
               (float)(bottomLeftColor >> 8 & 255)
                  / 255.0f,
               (float)(bottomLeftColor & (255)) / 255.0f,
               (float)a / 255.0f
            );
         }

         if (topRightColorUniform != null) {
            int a = topRightColor >> (24) & 255;
            if (a == 0) {
               a = 255;
            }

            topRightColorUniform.set(
               (float)(topRightColor >> (16) & (255))
                  / 255.0f,
               (float)(topRightColor >> (8) & 255)
                  / 255.0f,
               (float)(topRightColor & 255) / 255.0f,
               (float)a / 255.0f
            );
         }

         if (bottomRightColorUniform != null) {
            int a = bottomRightColor >> (24) & 255;
            if (a == 0) {
               a = 255;
            }

            bottomRightColorUniform.set(
               (float)(bottomRightColor >> 16 & 255)
                  / 255.0f,
               (float)(bottomRightColor >> 8 & 255)
                  / 255.0f,
               (float)(bottomRightColor & 255) / 255.0f,
               (float)a / 255.0f
            );
         }

         BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
         buffer.vertex(matrix, x, y, 0.0f)
            .color(
               1.0f,
               1.0f,
               1.0f,
               1.0f
            );
         buffer.vertex(matrix, x, y + height, 0.0f)
            .color(
               1.0f,
               1.0f,
               1.0f,
               1.0f
            );
         buffer.vertex(matrix, x + width, y + height, 0.0f)
            .color(
               1.0f,
               1.0f,
               1.0f,
               1.0F
            );
         buffer.vertex(matrix, x + width, y, 0.0f)
            .color(
               1.0f,
               1.0f,
               1.0f,
               1.0f
            );
         RenderSystem.setShader(ObsidianComponent_257.roundedRectOutline);
         BufferRenderer.drawWithGlobalProgram(buffer.end());
         RenderSystem.disableBlend();
      }
   }

   public static void drawBlur(
      MatrixStack matrices, float x, float y, float width, float height, float topLeft, float topRight, float bottomRight, float bottomLeft, int color
   ) {
      if (ProjectionUtil.getBuffer2() != null) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         Matrix4f matrix = matrices.peek().getPositionMatrix();
         ShaderProgram shader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.roundedTexture);
         GlUniform sizeUniform = shader.getUniform("Size");
         GlUniform radiusUniform = shader.getUniform("Radius");
         GlUniform smoothnessUniform = shader.getUniform("Smoothness");
         GlUniform colorModulatorUniform = shader.getUniform("ColorModulator");
         if (sizeUniform != null) {
            sizeUniform.set(width, height);
         }

         if (radiusUniform != null) {
            radiusUniform.set(topLeft, topRight, bottomRight, bottomLeft);
         }

         if (smoothnessUniform != null) {
            smoothnessUniform.set(0.5f);
         }

         if (colorModulatorUniform != null) {
            colorModulatorUniform.set(
               1.0f,
               1.0f,
               1.0f,
               1.0f
            );
         }

         RenderSystem.setShaderTexture(0, ProjectionUtil.getTexture());
         RenderSystem.setShader(ObsidianComponent_257.roundedTexture);
         int screenWidth = mc.getWindow().getScaledWidth();
         int screenHeight = mc.getWindow().getScaledHeight();
         float u1 = x / (float)screenWidth;
         float v1 = ((float)screenHeight - y) / (float)screenHeight;
         float u2 = (x + width) / (float)screenWidth;
         float v2 = ((float)screenHeight - y - height) / (float)screenHeight;
         int alpha = color >> (24) & 255;
         if (alpha == 0) {
            alpha = 255;
         }

         float r = (float)(color >> (16) & (255)) / 255.0f;
         float g = (float)(color >> (8) & 255)
            / 255.0f;
         float b = (float)(color & 255) / 255.0F;
         float a = (float)alpha / 255.0f;
         BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
         builder.vertex(matrix, x, y, 0.0f).texture(u1, v1).color(r, g, b, a);
         builder.vertex(matrix, x, y + height, 0.0f).texture(u1, v2).color(r, g, b, a);
         builder.vertex(matrix, x + width, y + height, 0.0f).texture(u2, v2).color(r, g, b, a);
         builder.vertex(matrix, x + width, y, 0.0f).texture(u2, v1).color(r, g, b, a);
         BufferRenderer.drawWithGlobalProgram(builder.end());
         RenderSystem.setShaderTexture(0, 0);
         RenderSystem.disableBlend();
      }
   }

   public static void drawBlur(MatrixStack matrices, float x, float y, float width, float height, float radius, int color) {
      drawBlur(matrices, x, y, width, height, radius, radius, radius, radius, color);
   }

   public static void startGlow(float radius, int color, ObsidianComponent_69 callback, MatrixStack matrices) {
      int a = color >> 24 & (255);
      int r = color >> (16) & (255);
      int g = color >> 8 & (255);
      int b = color & (255);
      if (a == 0) {
         a = 255;
      }

      ObsidianComponent_254.getInstance().begin(radius, new Color(r, g, b, a));
      callback.render();
      ObsidianComponent_254.getInstance().end(matrices, callback);
   }

   public static void startGlow(float radius, float intensity, int color, ObsidianComponent_69 callback, MatrixStack matrices) {
      int a = color >> 24 & 255;
      int r = color >> 16 & 255;
      int g = color >> 8 & 255;
      int b = color & (255);
      if (a == 0) {
         a = 255;
      }

      ObsidianComponent_254.getInstance().begin(radius, intensity, new Color(r, g, b, a));
      callback.render();
      ObsidianComponent_254.getInstance().end(matrices, callback);
   }

   public static void drawBlur(
      MatrixStack matrices,
      float x,
      float y,
      float width,
      float height,
      float topLeft,
      float topRight,
      float bottomRight,
      float bottomLeft,
      float blurStrength,
      int color
   ) {
      ProjectionUtil.getInstance().request();
      if (ProjectionUtil.getBuffer2() != null) {
         ProjectionUtil.getInstance().setBlurOffset(blurStrength);
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         Matrix4f matrix = matrices.peek().getPositionMatrix();
         ShaderProgram shader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.roundedTexture);
         GlUniform sizeUniform = shader.getUniform("Size");
         GlUniform radiusUniform = shader.getUniform("Radius");
         GlUniform smoothnessUniform = shader.getUniform("Smoothness");
         GlUniform colorModulatorUniform = shader.getUniform("ColorModulator");
         if (sizeUniform != null) {
            sizeUniform.set(width, height);
         }

         if (radiusUniform != null) {
            radiusUniform.set(topLeft, topRight, bottomRight, bottomLeft);
         }

         if (smoothnessUniform != null) {
            smoothnessUniform.set(0.5f);
         }

         if (colorModulatorUniform != null) {
            colorModulatorUniform.set(
               1.0f,
               1.0f,
               1.0f,
               1.0f
            );
         }

         RenderSystem.setShaderTexture(0, ProjectionUtil.getTexture());
         RenderSystem.setShader(ObsidianComponent_257.roundedTexture);
         int screenWidth = mc.getWindow().getScaledWidth();
         int screenHeight = mc.getWindow().getScaledHeight();
         float u1 = x / (float)screenWidth;
         float v1 = ((float)screenHeight - y) / (float)screenHeight;
         float u2 = (x + width) / (float)screenWidth;
         float v2 = ((float)screenHeight - y - height) / (float)screenHeight;
         int alpha = color >> 24 & 255;
         if (alpha == 0) {
            alpha = 255;
         }

         float r = (float)(color >> (16) & 255)
            / 255.0f;
         float g = (float)(color >> 8 & (255))
            / 255.0f;
         float b = (float)(color & (255)) / 255.0f;
         float a = (float)alpha / 255.0f;
         BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
         builder.vertex(matrix, x, y, 0.0f).texture(u1, v1).color(r, g, b, a);
         builder.vertex(matrix, x, y + height, 0.0f).texture(u1, v2).color(r, g, b, a);
         builder.vertex(matrix, x + width, y + height, 0.0f).texture(u2, v2).color(r, g, b, a);
         builder.vertex(matrix, x + width, y, 0.0f).texture(u2, v1).color(r, g, b, a);
         BufferRenderer.drawWithGlobalProgram(builder.end());
         RenderSystem.setShaderTexture(0, 0);
         RenderSystem.disableBlend();
      }
   }

   public static void drawBlur(MatrixStack matrices, float x, float y, float width, float height, float radius, float blurStrength, int color) {
      drawBlur(matrices, x, y, width, height, radius, radius, radius, radius, blurStrength, color);
   }

   public static void drawLiquidGlass(
      MatrixStack matrices,
      float x,
      float y,
      float width,
      float height,
      float topLeft,
      float topRight,
      float bottomRight,
      float bottomLeft,
      int color,
      float globalAlpha,
      float fresnelPower,
      int fresnelColor,
      float baseAlpha,
      boolean fresnelInvert,
      float fresnelMix,
      float distortStrength,
      float squirt,
      boolean clean
   ) {
      int textureId;
      if (clean) {
         textureId = mc.getFramebuffer().getColorAttachment();
      } else {
         ProjectionUtil.getInstance().request();
         if (ProjectionUtil.getBuffer1() == null) {
            return;
         }

         textureId = ProjectionUtil.getTexture();
         if (textureId == 0) {
            return;
         }
      }

      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.setShaderTexture(0, textureId);
      ShaderProgram shader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.liquidGlass);
      GlUniform globalAlphaUniform = shader.getUniform("GlobalAlpha");
      GlUniform sizeUniform = shader.getUniform("Size");
      GlUniform radiusUniform = shader.getUniform("Radius");
      GlUniform smoothnessUniform = shader.getUniform("Smoothness");
      GlUniform fresnelPowerUniform = shader.getUniform("FresnelPower");
      GlUniform fresnelColorUniform = shader.getUniform("FresnelColor");
      GlUniform fresnelAlphaUniform = shader.getUniform("FresnelAlpha");
      GlUniform baseAlphaUniform = shader.getUniform("BaseAlpha");
      GlUniform fresnelInvertUniform = shader.getUniform("FresnelInvert");
      GlUniform fresnelMixUniform = shader.getUniform("FresnelMix");
      GlUniform distortStrengthUniform = shader.getUniform("DistortStrength");
      GlUniform cornerSmoothnessUniform = shader.getUniform("CornerSmoothness");
      if (globalAlphaUniform != null) {
         globalAlphaUniform.set(globalAlpha);
      }

      if (sizeUniform != null) {
         sizeUniform.set(width, height);
      }

      if (radiusUniform != null) {
         radiusUniform.set(topLeft, topRight, bottomRight, bottomLeft);
      }

      if (smoothnessUniform != null) {
         smoothnessUniform.set(0.5f);
      }

      if (fresnelPowerUniform != null) {
         fresnelPowerUniform.set(fresnelPower);
      }

      int fAlpha = fresnelColor >> (24) & (255);
      if (fAlpha == 0) {
         fAlpha = 255;
      }

      if (fresnelColorUniform != null) {
         fresnelColorUniform.set(
            (float)(fresnelColor >> 16 & (255))
               / 255.0f,
            (float)(fresnelColor >> 8 & 255) / 255.0F,
            (float)(fresnelColor & (255)) / 255.0f
         );
      }

      if (fresnelAlphaUniform != null) {
         fresnelAlphaUniform.set((float)fAlpha / 255.0f);
      }

      if (baseAlphaUniform != null) {
         baseAlphaUniform.set(baseAlpha);
      }

      if (fresnelInvertUniform != null) {
         fresnelInvertUniform.set(fresnelInvert ? 1 : 0);
      }

      if (fresnelMixUniform != null) {
         fresnelMixUniform.set(fresnelMix);
      }

      if (distortStrengthUniform != null) {
         distortStrengthUniform.set(distortStrength);
      }

      if (cornerSmoothnessUniform != null) {
         cornerSmoothnessUniform.set(squirt);
      }

      Matrix4f matrix = matrices.peek().getPositionMatrix();
      float screenWidth = (float)mc.getWindow().getFramebufferWidth();
      float screenHeight = (float)mc.getWindow().getFramebufferHeight();
      float scaleFactor = (float)mc.getWindow().getScaleFactor();
      float scaledX = x * scaleFactor;
      float scaledY = y * scaleFactor;
      float scaledW = width * scaleFactor;
      float scaledH = height * scaleFactor;
      float u1 = scaledX / screenWidth;
      float v1 = 1.0f - scaledY / screenHeight;
      float u2 = (scaledX + scaledW) / screenWidth;
      float v2 = 1.0f - (scaledY + scaledH) / screenHeight;
      int alpha = color >> (24) & 255;
      if (alpha == 0) {
         alpha = 255;
      }

      float r = (float)(color >> 16 & (255))
         / 255.0f;
      float g = (float)(color >> 8 & 255) / 255.0f;
      float b = (float)(color & 255) / 255.0f;
      float a = (float)alpha / 255.0f;
      RenderSystem.setShader(ObsidianComponent_257.liquidGlass);
      BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      builder.vertex(matrix, x, y, 0.0f).texture(u1, v1).color(r, g, b, a);
      builder.vertex(matrix, x, y + height, 0.0f).texture(u1, v2).color(r, g, b, a);
      builder.vertex(matrix, x + width, y + height, 0.0f).texture(u2, v2).color(r, g, b, a);
      builder.vertex(matrix, x + width, y, 0.0f).texture(u2, v1).color(r, g, b, a);
      BufferRenderer.drawWithGlobalProgram(builder.end());
      RenderSystem.setShaderTexture(0, 0);
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }

   @Generated
   public RenderUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
