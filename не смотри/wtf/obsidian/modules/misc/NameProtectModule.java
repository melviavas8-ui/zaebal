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

























import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;


public class NameProtectModule extends Module {
   public static final NameProtectModule INSTANCE = new NameProtectModule();
   private final BooleanSetting friends = new BooleanSetting("Скрывать друзей", true);
   private final BooleanSetting grief = new BooleanSetting("Скрывать гриф", false);
   private final ObsidianComponent_341 nickname = new ObsidianComponent_341("Никнейм", "obsidian", 32);
   public static final int PATCH_CACHE_LIMIT = 512;
   private final Map<String, String> patchCache = new LinkedHashMap<String, String>(
      512, 0.75f, true
   ) {
      @Override
      protected boolean removeEldestEntry(Entry<String, String> eldest) {
         return (this.size() > 512);
      }
   };

   public NameProtectModule() {
      super("NameProtect", "Скрывает никнеймы", Category.MISC);
      Setting[] var10001 = new Setting[3];
      var10001[0] = this.friends;
      var10001[1] = this.grief;
      var10001[2] = this.nickname;
      this.addSettings(var10001);
   }

   public String patch(String text) {
      if (text == null) {
         return null;
      } else if (!this.shouldPatch()) {
         return text;
      } else {
         String cacheKey = this.getPatchCacheKey(text);
         String cached = this.patchCache.get(cacheKey);
         if (cached != null) {
            return cached;
         } else {
            String replacement = this.getReplacementName();
            String out = this.replaceIgnoreCase(text, mc.getSession().getUsername(), replacement);
            if (this.friends.isState() && Obsidian.INSTANCE != null && Obsidian.INSTANCE.friendStorage != null) {
               for (String friend : Obsidian.INSTANCE.friendStorage.getFriends()) {
                  out = this.replaceIgnoreCase(out, friend, replacement);
               }
            }

            out = this.patchGrief(out);
            this.patchCache.put(cacheKey, out);
            return out;
         }
      }
   }

   public String patchIncomingText(String text) {
      return this.patch(text);
   }

   public Text patchText(Text text) {
      if (text == null) {
         return null;
      } else if (!this.shouldPatch()) {
         return text;
      } else {
         String replacement = this.getReplacementName();
         Text output = ObsidianComponent_274.replace(text, mc.getSession().getUsername(), replacement);
         if (this.friends.isState() && Obsidian.INSTANCE != null && Obsidian.INSTANCE.friendStorage != null) {
            for (String friend : Obsidian.INSTANCE.friendStorage.getFriends()) {
               output = ObsidianComponent_274.replace(output, friend, replacement);
            }
         }

         return output;
      }
   }

   public String getReplacementName() {
      String value = this.nickname.get();
      return value != null && !value.isBlank() ? value : "obsidian";
   }

   public boolean shouldHideGrief() {
      return this.grief.isState();
   }
   public String replaceIgnoreCase(String text, String target, String replacement) {
      if (text != null && target != null && !target.isEmpty()) {
         int firstIndex = this.indexOfIgnoreCase(text, target, 0);
         if (firstIndex < 0) {
            return text;
         } else {
            StringBuilder out = new StringBuilder(text.length() + replacement.length());
            int from = 0;

            for (int index = firstIndex; index >= 0; index = this.indexOfIgnoreCase(text, target, from)) {
               out.append(text, from, index).append(replacement);
               from = index + target.length();
            }

            out.append(text, from, text.length());
            return out.toString();
         }
      } else {
         return text;
      }
   }
   public int indexOfIgnoreCase(String text, String target, int from) {
      int max = text.length() - target.length();

      for (int i = Math.max(0, from); i <= max; i++) {
         if (text.regionMatches(true, i, target, 0, target.length())) {
            return i;
         }
      }

      return -1;
   }
   public String patchGrief(String text) {
      if (text != null && this.grief.isState()) {
         String out = text.replaceAll("Анархия-\\d+", "obsidianclient.fun");
         return out.replaceAll("ГРИФ #\\d+", "obsidianclient.fun");
      } else {
         return text;
      }
   }
   public String getPatchCacheKey(String text) {
      String username = mc != null && mc.getSession() != null ? mc.getSession().getUsername() : "";
      int friendsHash = 0;
      if (this.friends.isState() && Obsidian.INSTANCE != null && Obsidian.INSTANCE.friendStorage != null) {
         List<String> friendList = Obsidian.INSTANCE.friendStorage.getFriends();
         friendsHash = friendList.hashCode();
      }

      return username + this.getReplacementName() + this.friends.isState() + this.grief.isState() + friendsHash + text;
   }
   public boolean shouldPatch() {
      return (this.isEnable() && mc != null && mc.player != null && mc.world != null && !this.isFriendRemoveInputActive());
   }
   public boolean isFriendRemoveInputActive() {
      if (!(mc.currentScreen instanceof ChatScreen chatScreen)) {
         return false;
      } else {
         TextFieldWidget chatField = ((ChatScreenAccessor)chatScreen).obsidian$getChatField();
         if (chatField == null) {
            return false;
         } else {
            String input = chatField.getText();
            if (input == null) {
               return false;
            } else {
               String normalized = input.trim().toLowerCase();
               String prefix = Obsidian.INSTANCE != null && Obsidian.INSTANCE.commandStorage != null
                  ? Obsidian.INSTANCE.commandStorage.getPrefix().toLowerCase()
                  : ".";
               return normalized.startsWith(prefix + "friend remove");
            }
         }
      }
   }
}
