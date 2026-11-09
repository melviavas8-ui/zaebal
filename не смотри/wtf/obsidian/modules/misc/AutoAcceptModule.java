package wtf.obsidian.modules.misc;

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

























import java.util.Locale;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

public class AutoAcceptModule extends Module {
   public static AutoAcceptModule INSTANCE = new AutoAcceptModule();
   private final BooleanSetting onlyFriend = new BooleanSetting("Только друзья", false);

   public AutoAcceptModule() {
      super("AutoAccept", "Автоматически принимает телепорт", Category.MISC);
      Setting[] var10001 = new Setting[1];
      var10001[0] = this.onlyFriend;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onEvent(PacketEvent event) {
      if (mc.player != null && mc.world != null) {
         if (event.getType() == PacketType.RECEIVE) {
            if (event.getPacket() instanceof GameMessageS2CPacket messagePacket) {
               String raw = messagePacket.content().getString().toLowerCase(Locale.ROOT);
               if (raw.contains("телепортироваться") || raw.contains("has requested teleport") || raw.contains("просит к вам телепортироваться")) {
                  if (this.onlyFriend.isState()) {
                     boolean isFriend = false;
                     if (Obsidian.INSTANCE.friendStorage != null) {
                        for (String friend : Obsidian.INSTANCE.friendStorage.getFriends()) {
                           if (raw.contains(friend.toLowerCase(Locale.ROOT))) {
                              isFriend = true;
                              break;
                           }
                        }
                     }

                     if (!isFriend) {
                        return;
                     }
                  }

                  mc.player.networkHandler.sendChatCommand("tpaccept");
               }
            }
         }
      }
   }
}
