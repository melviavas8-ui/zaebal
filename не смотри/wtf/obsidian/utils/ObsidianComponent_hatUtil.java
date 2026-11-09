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
























import java.awt.Color;
import lombok.Generated;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

public final class ObsidianComponent_hatUtil {
   public static void sendMessage(Object message) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.player == null) {
         System.out.println("[obsidian] " + message);
      } else {
         MutableText text = Text.literal("");
         String prefix = "obsidian";

         for (int i = 0; i < prefix.length(); i++) {
            text.append(
               Text.literal(String.valueOf(prefix.charAt(i)))
                  .setStyle(
                     Style.EMPTY
                        .withBold(Boolean.valueOf(true))
                        .withColor(
                           TextColor.fromRgb(
                              ObsidianComponent_olorUtil.gradient(
                                 ObsidianComponent_olorUtil.getThemeColor(0),
                                 ObsidianComponent_olorUtil.getThemeColor(90),
                                 (float)i / (float)prefix.length()
                              )
                           )
                        )
                  )
            );
         }

         text.append(
            Text.literal(" ⇨ ")
               .setStyle(
                  Style.EMPTY
                     .withBold(Boolean.valueOf(false))
                     .withColor(TextColor.fromRgb(new Color(200, 200, 200).getRGB()))
               )
         );
         text.append(
            Text.literal(String.valueOf(message))
               .setStyle(
                  Style.EMPTY
                     .withBold(Boolean.valueOf(false))
                     .withColor(
                        TextColor.fromRgb(
                           new Color(200, 200, 200)
                              .getRGB()
                        )
                     )
               )
         );
         mc.player.sendMessage(text, false);
      }
   }

   @Generated
   public ObsidianComponent_hatUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
