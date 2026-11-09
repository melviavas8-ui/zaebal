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
























import java.awt.Font;
import java.io.InputStream;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

public class ObsidianComponent_415 {
   public static Font getFontFromTTF(Identifier loc, float fontSize, int fontType) {
      try {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client == null) {
            return null;
         } else if (client.getResourceManager() == null) {
            return null;
         } else {
            Optional<Resource> resource = client.getResourceManager().getResource(loc);
            if (resource.isPresent()) {
               InputStream inputStream = resource.get().getInputStream();
               Font output = Font.createFont(fontType, inputStream);
               output = output.deriveFont(fontSize);
               inputStream.close();
               return output;
            } else {
               return null;
            }
         }
      } catch (Exception var7) {
         var7.printStackTrace();
         return null;
      }
   }
}
