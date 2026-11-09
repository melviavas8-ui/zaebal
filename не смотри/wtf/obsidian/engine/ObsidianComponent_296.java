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
























import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashMap;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;

public class ObsidianComponent_296 {
   public String name = "?";
   public Identifier dataIdentifier;
   public Identifier atlasIdentifier;

   public ObsidianComponent_296 name(String name) {
      this.name = name;
      return this;
   }

   public ObsidianComponent_296 data(String dataFileName) {
      this.dataIdentifier = Identifier.of("obsidian", "fonts/msdf/" + dataFileName + "/font.json");
      return this;
   }

   public ObsidianComponent_296 atlas(String atlasFileName) {
      this.atlasIdentifier = Identifier.of("obsidian", "fonts/msdf/" + atlasFileName + "/font.png");
      return this;
   }

   public ObsidianComponent_81 build() {
      String json = ObsidianComponent_81.readResource(this.dataIdentifier);
      JsonObject root = JsonParser.parseString(json).getAsJsonObject();
      JsonObject atlasObj = root.getAsJsonObject("atlas");
      float atlasWidth = atlasObj.get("width").getAsFloat();
      float atlasHeight = atlasObj.get("height").getAsFloat();
      float range = atlasObj.get("distanceRange").getAsFloat();
      JsonObject metricsObj = root.getAsJsonObject("metrics");
      float lineHeight = metricsObj.get("lineHeight").getAsFloat();
      float ascender = metricsObj.get("ascender").getAsFloat();
      float descender = metricsObj.get("descender").getAsFloat();
      HashMap<Integer, ObsidianComponent_64> glyphs = new HashMap<>();

      for (JsonElement element : root.getAsJsonArray("glyphs")) {
         JsonObject glyphObj = element.getAsJsonObject();
         int unicode = glyphObj.get("unicode").getAsInt();
         float advance = glyphObj.get("advance").getAsFloat();
         float planeLeft = 0.0f;
         float planeTop = 0.0F;
         float planeRight = 0.0f;
         float planeBottom = 0.0f;
         if (glyphObj.has("planeBounds") && !glyphObj.get("planeBounds").isJsonNull()) {
            JsonObject plane = glyphObj.getAsJsonObject("planeBounds");
            planeLeft = plane.get("left").getAsFloat();
            planeTop = plane.get("top").getAsFloat();
            planeRight = plane.get("right").getAsFloat();
            planeBottom = plane.get("bottom").getAsFloat();
         }

         float atlasLeft = 0.0f;
         float atlasTop = 0.0f;
         float atlasRight = 0.0f;
         float atlasBottom = 0.0f;
         if (glyphObj.has("atlasBounds") && !glyphObj.get("atlasBounds").isJsonNull()) {
            JsonObject atlas = glyphObj.getAsJsonObject("atlasBounds");
            atlasLeft = atlas.get("left").getAsFloat();
            atlasTop = atlas.get("top").getAsFloat();
            atlasRight = atlas.get("right").getAsFloat();
            atlasBottom = atlas.get("bottom").getAsFloat();
         }

         ObsidianComponent_64 glyph = new ObsidianComponent_64(
            unicode, advance, planeLeft, planeTop, planeRight, planeBottom, atlasLeft, atlasTop, atlasRight, atlasBottom, atlasWidth, atlasHeight
         );
         glyphs.put(unicode, glyph);
      }

      AbstractTexture texture = IInstance.mc.getTextureManager().getTexture(this.atlasIdentifier);
      return new ObsidianComponent_81(this.name, texture, atlasWidth, atlasHeight, range, lineHeight, ascender, descender, glyphs);
   }
}
