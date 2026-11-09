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
























import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.stream.Collectors;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public final class ObsidianComponent_81 implements IInstance {
   public final String name;
   public final AbstractTexture texture;
   public final float atlasWidth;
   public final float atlasHeight;
   public final float range;
   public final float lineHeight;
   public final float ascender;
   public final float descender;
   public final HashMap<Integer, ObsidianComponent_64> glyphs;
   private boolean filtered = false;

   public ObsidianComponent_81(
      String name,
      AbstractTexture texture,
      float atlasWidth,
      float atlasHeight,
      float range,
      float lineHeight,
      float ascender,
      float descender,
      HashMap<Integer, ObsidianComponent_64> glyphs
   ) {
      this.name = name;
      this.texture = texture;
      this.atlasWidth = atlasWidth;
      this.atlasHeight = atlasHeight;
      this.range = range;
      this.lineHeight = lineHeight;
      this.ascender = ascender;
      this.descender = descender;
      this.glyphs = glyphs;
   }

   public void setFiltered() {
      if (!this.filtered) {
         this.texture.setFilter(true, false);
         this.filtered = true;
      }
   }

   public int getTextureId() {
      return this.texture.getGlId();
   }

   public float getAtlasWidth() {
      return this.atlasWidth;
   }

   public float getAtlasHeight() {
      return this.atlasHeight;
   }

   public float getRange() {
      return this.range;
   }

   public float getLineHeight() {
      return this.lineHeight;
   }

   public float getBaselineHeight() {
      return this.lineHeight + this.descender;
   }

   public String getName() {
      return this.name;
   }

   public void applyGlyphs(
      Matrix4f matrix, VertexConsumer consumer, float size, String text, float thickness, float x, float y, float z, int red, int green, int blue, int alpha
   ) {
      text = replaceSymbols(text);

      for (int i = 0; i < text.length(); i++) {
         char c = text.charAt(i);
         if (c == 167 && i + 1 < text.length()) {
            i++;
         } else {
            ObsidianComponent_64 glyph = this.glyphs.get(Integer.valueOf(c));
            if (glyph != null) {
               x += glyph.apply(matrix, consumer, size, x, y, z, red, green, blue, alpha) + thickness;
            }
         }
      }
   }

   public float getWidth(String text, float size) {
      text = replaceSymbols(text);
      float width = 0.0f;

      for (int i = 0; i < text.length(); i++) {
         char c = text.charAt(i);
         if (c == (167) && i + 1 < text.length()) {
            i++;
         } else {
            ObsidianComponent_64 glyph = this.glyphs.get(Integer.valueOf(c));
            if (glyph != null) {
               width += glyph.getWidth(size);
            }
         }
      }

      return width;
   }
   public static String replaceSymbols(String text) {
      return text == null
         ? ""
         : text.replace("ᴀ", "A")
            .replace("ʙ", "B")
            .replace("ᴄ", "C")
            .replace("ᴅ", "D")
            .replace("ᴇ", "E")
            .replace("ғ", "F")
            .replace("ɢ", "G")
            .replace("ʜ", "H")
            .replace("ɪ", "I")
            .replace("ᴊ", "J")
            .replace("ᴋ", "K")
            .replace("ʟ", "L")
            .replace("ᴍ", "M")
            .replace("ɴ", "N")
            .replace("ᴏ", "O")
            .replace("ᴘ", "P")
            .replace("ǫ", "Q")
            .replace("ʀ", "R")
            .replace("ꜱ", "S")
            .replace("ᴛ", "T")
            .replace("ᴜ", "U")
            .replace("ᴠ", "V")
            .replace("ᴡ", "W")
            .replace("ʏ", "Y")
            .replace("ᴢ", "Z")
            .replace("ꜰ", "F");
   }
   public static String readResource(Identifier identifier) {
      try {
         InputStream inputStream = mc.getResourceManager().open(identifier);
         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
         String result = reader.lines().collect(Collectors.joining("\n"));
         reader.close();
         inputStream.close();
         return result;
      } catch (Exception var4) {
         throw new RuntimeException("Failed to read resource: " + identifier, var4);
      }
   }

   public static ObsidianComponent_296 builder() {
      return new ObsidianComponent_296();
   }
}
