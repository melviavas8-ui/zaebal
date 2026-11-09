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

























import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Formatting;

public class AutoPvPModule extends Module {
   public static AutoPvPModule INSTANCE = new AutoPvPModule();
   private static final Pattern SEARCH_PATTERN = Pattern.compile("Игрок\\s+(\\S+)\\s+ищет себе соперника!", 2);
   public static final long OPEN_DELAY_MS = 250L;
   public static final long RETRY_DELAY_MS = 1000L;
   public static final int SEARCH_MENU_SLOT = 20;
   public final MultiBooleanSetting donateSettings;
   public final ObsidianComponent_372 actionTimer;
   public boolean queuedJoin;
   public String queuedNickname;

   public AutoPvPModule() {
      super("AutoPvP", "Помощник в подборе пвп для сервера LonyGrief", Category.MISC);
      BooleanSetting[] var10004 = new BooleanSetting[19];
      var10004[0] = new BooleanSetting("CUSTOM", false);
      var10004[1] = new BooleanSetting("D.HELPER", false);
      var10004[2] = new BooleanSetting("FROSTINE", false);
      var10004[3] = new BooleanSetting("SPRING", false);
      var10004[4] = new BooleanSetting("AUTUMN", false);
      var10004[5] = new BooleanSetting("GLADIATOR", false);
      var10004[6] = new BooleanSetting("PALADIN", false);
      var10004[7] = new BooleanSetting("LUXE", false);
      var10004[8] = new BooleanSetting("STAFF", false);
      var10004[9] = new BooleanSetting("SUPPORT", false);
      var10004[10] = new BooleanSetting("ETERNITY", false);
      var10004[11] = new BooleanSetting("OVERLORD", false);
      var10004[12] = new BooleanSetting("D.ADMIN", false);
      var10004[13] = new BooleanSetting("POVELITEL", false);
      var10004[14] = new BooleanSetting("IMPERATOR", false);
      var10004[15] = new BooleanSetting("LEGENDA", false);
      var10004[16] = new BooleanSetting("PRAVITEL", false);
      var10004[17] = new BooleanSetting("PHOENIX", false);
      var10004[18] = new BooleanSetting("PLAYER", false);
      this.donateSettings = new MultiBooleanSetting("С кем идти...", var10004);
      this.actionTimer = new ObsidianComponent_372();
      Setting[] var10001 = new Setting[1];
      var10001[0] = this.donateSettings;
      this.addSettings(var10001);
   }

   @Override
   public void onEnable() {
      this.queuedJoin = false;
      this.queuedNickname = null;
      this.actionTimer.reset();
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.queuedJoin = false;
      this.queuedNickname = null;
      super.onDisable();
   }

   @EventTarget
   public void onPacket(PacketEvent event) {
      if (mc.player != null && mc.world != null && event.getType() == PacketType.RECEIVE) {
         if (event.getPacket() instanceof GameMessageS2CPacket packet) {
            String raw = packet.content().getString();
            if (raw != null) {
               String plain = Formatting.strip(raw);
               if (plain == null) {
                  plain = raw;
               }

               Matcher matcher = SEARCH_PATTERN.matcher(plain);
               if (matcher.find()) {
                  String nickname = matcher.group(1);
                  if (nickname != null && !nickname.isBlank()) {
                     if (Obsidian.INSTANCE == null || Obsidian.INSTANCE.friendStorage == null || !Obsidian.INSTANCE.friendStorage.isFriend(nickname)) {
                        ObsidianComponent_364 rank = this.resolveDonateRank(nickname);
                        if (this.isAllowed(rank)) {
                           this.queuedJoin = true;
                           this.queuedNickname = nickname;
                           this.actionTimer.setMillis(0L);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (this.queuedJoin && mc.player != null && mc.world != null && mc.getNetworkHandler() != null && mc.interactionManager != null) {
         if (mc.currentScreen instanceof GenericContainerScreen screen) {
            String title = screen.getTitle().getString().toLowerCase();
            if (title.contains("поиск поединка") && this.actionTimer.finished(250L)) {
               mc.interactionManager
                  .clickSlot(
                     ((GenericContainerScreenHandler)screen.getScreenHandler()).syncId,
                     20,
                     0,
                     SlotActionType.PICKUP,
                     mc.player
                  );
               this.queuedJoin = false;
               this.queuedNickname = null;
               this.actionTimer.reset();
            }
         } else if (mc.currentScreen == null) {
            if (this.actionTimer.finished(1000L)) {
               mc.getNetworkHandler().sendChatCommand("pvp");
               this.actionTimer.reset();
            }
         }
      }
   }
   public ObsidianComponent_364 resolveDonateRank(String nickname) {
      if (mc.getNetworkHandler() == null) {
         return null;
      } else {
         for (PlayerListEntry player : mc.getNetworkHandler().getPlayerList()) {
            String displayName = player.getDisplayName() != null ? player.getDisplayName().getString() : player.getProfile().getName();
            String cleanDisplayName = Formatting.strip(displayName);
            if (cleanDisplayName == null) {
               cleanDisplayName = displayName;
            }

            int nameIndex = this.indexOfIgnoreCase(cleanDisplayName, nickname);
            if (nameIndex >= 0) {
               String donatePrefix = cleanDisplayName.substring(0, nameIndex).trim();
               if (donatePrefix.isEmpty()) {
                  return null;
               }

               String cleanDonate = this.decodeDonatePrefix(donatePrefix).replace("+", "");
               return ObsidianComponent_364.fromString(cleanDonate);
            }
         }

         return null;
      }
   }
   public boolean isAllowed(ObsidianComponent_364 rank) {
      return rank == null ? this.donateSettings.is("CUSTOM") : this.donateSettings.is(rank.getName());
   }
   public String safeStrip(String text) {
      String stripped = Formatting.strip(text);
      return stripped == null ? text : stripped;
   }
   public int indexOfIgnoreCase(String text, String target) {
      return text.toLowerCase().indexOf(target.toLowerCase());
   }
   public String decodeDonatePrefix(String prefix) {
      StringBuilder decoded = new StringBuilder(prefix.length());
      int offset = 0;

      while (offset < prefix.length()) {
         int codePoint = prefix.codePointAt(offset);
         offset += Character.charCount(codePoint);
         String replacement = ObsidianComponent_404.replaceCodePoint(codePoint);
         if (replacement != null) {
            decoded.append(replacement);
         } else {
            decoded.appendCodePoint(codePoint);
         }
      }

      return this.convertStyledToNormal(decoded.toString()).trim();
   }
   public String convertStyledToNormal(String styledText) {
      String styled = "бґЂК™бґ„бґ…бґ‡књ°ЙўКњЙЄбґЉбґ‹КџбґЌЙґбґЏбґ\u0098КЂкњ±бґ›бґњбґ бґЎКЏбґўЙґ";
      String normal = "ABCDEFGHIJKLMNOPRSTUVWYZN";
      StringBuilder result = new StringBuilder();
      char[] var5 = styledText.toCharArray();
      int var6 = var5.length;

      for (int var7 = 0; var7 < var6; var7++) {
         char c = var5[var7];
         int index = styled.indexOf(c);
         if (index != -1) {
            result.append(normal.charAt(index));
         } else {
            result.append(c);
         }
      }

      return result.toString();
   }
}
