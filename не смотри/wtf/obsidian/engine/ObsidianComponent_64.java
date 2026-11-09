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
























import net.minecraft.client.render.VertexConsumer;
import org.joml.Matrix4f;

public final class ObsidianComponent_64 {
   public final int code;
   public final float minU;
   public final float maxU;
   public final float minV;
   public final float maxV;
   public final float advance;
   public final float topPosition;
   public final float width;
   public final float height;

   public ObsidianComponent_64(
      int unicode,
      float advance,
      float planeLeft,
      float planeTop,
      float planeRight,
      float planeBottom,
      float atlasLeft,
      float atlasTop,
      float atlasRight,
      float atlasBottom,
      float atlasWidth,
      float atlasHeight
   ) {
      this.code = unicode;
      this.advance = advance;
      if (atlasLeft == 0.0f
         && atlasRight == 0.0f
         && atlasTop == 0.0f
         && atlasBottom == 0.0f) {
         this.minU = 0.0f;
         this.maxU = 0.0f;
         this.minV = 0.0f;
         this.maxV = 0.0f;
      } else {
         this.minU = atlasLeft / atlasWidth;
         this.maxU = atlasRight / atlasWidth;
         this.minV = 1.0f - atlasTop / atlasHeight;
         this.maxV = 1.0f - atlasBottom / atlasHeight;
      }

      if (planeLeft == 0.0f
         && planeRight == 0.0f
         && planeTop == 0.0f
         && planeBottom == 0.0f) {
         this.width = 0.0f;
         this.height = 0.0f;
         this.topPosition = 0.0f;
      } else {
         this.width = planeRight - planeLeft;
         this.height = planeTop - planeBottom;
         this.topPosition = planeTop;
      }
   }

   public float apply(Matrix4f matrix, VertexConsumer consumer, float size, float x, float y, float z, int red, int green, int blue, int alpha) {
      y -= this.topPosition * size;
      y -= 1.0f;
      float w = this.width * size;
      float h = this.height * size;
      consumer.vertex(matrix, x, y, z).color(red, green, blue, alpha).texture(this.minU, this.minV);
      consumer.vertex(matrix, x, y + h, z).color(red, green, blue, alpha).texture(this.minU, this.maxV);
      consumer.vertex(matrix, x + w, y + h, z).color(red, green, blue, alpha).texture(this.maxU, this.maxV);
      consumer.vertex(matrix, x + w, y, z).color(red, green, blue, alpha).texture(this.maxU, this.minV);
      return this.width * (size - 1.0f)
         + (Character.isSpaceChar(this.code) ? this.advance * size : 0.0f);
   }

   public float getWidth(float size) {
      return this.width * (size - 1.0f)
         + (Character.isSpaceChar(this.code) ? this.advance * size : 0.0f);
   }

   public int getCharCode() {
      return this.code;
   }
}
