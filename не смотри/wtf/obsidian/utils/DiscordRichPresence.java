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

























import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class DiscordRichPresence extends Structure {
   public String largeImageKey;
   public String largeImageText;
   public String smallImageText;
   public String partyPrivacy;
   public long startTimestamp;
   public int instance;
   public String partyId;
   public int partySize;
   public long endTimestamp;
   public String details;
   public String joinSecret;
   public String spectateSecret;
   public String smallImageKey;
   public String matchSecret;
   public String state;
   public int partyMax;
   public String button_url_1;
   public String button_label_1;
   public String button_url_2;
   public String button_label_2;

   public DiscordRichPresence() {
      this.setStringEncoding("UTF-8");
   }

   @Override
   protected List<String> getFieldOrder() {
      String[] var10000 = new String[20];
      var10000[0] = "state";
      var10000[1] = "details";
      var10000[2] = "startTimestamp";
      var10000[3] = "endTimestamp";
      var10000[4] = "largeImageKey";
      var10000[5] = "largeImageText";
      var10000[6] = "smallImageKey";
      var10000[7] = "smallImageText";
      var10000[8] = "partyId";
      var10000[9] = "partySize";
      var10000[10] = "partyMax";
      var10000[11] = "partyPrivacy";
      var10000[12] = "matchSecret";
      var10000[13] = "joinSecret";
      var10000[14] = "spectateSecret";
      var10000[15] = "button_label_1";
      var10000[16] = "button_url_1";
      var10000[17] = "button_label_2";
      var10000[18] = "button_url_2";
      var10000[19] = "instance";
      return Arrays.asList(var10000);
   }
}
